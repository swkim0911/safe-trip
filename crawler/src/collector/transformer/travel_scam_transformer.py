import json
import logging
from datetime import datetime, timedelta, UTC

from adapters.openai_client import get_completed_batch_result
from utils.token_utils import estimate_classification_request_tokens, estimate_parsing_request_tokens


class TravelScamTransformer:
    """여행 사기 데이터 변환을 담당하는 클래스 (분류 및 파싱)"""
    
    def __init__(self, travel_scam_classifier, travel_scam_parser, travel_scam_enricher, reddit_repository, etl_config):
        self.travel_scam_classifier = travel_scam_classifier
        self.travel_scam_parser = travel_scam_parser
        self.travel_scam_enricher = travel_scam_enricher
        self.reddit_repository = reddit_repository
        self.etl_config = etl_config
        self.logger = logging.getLogger(__name__)

    '''
    raw 도큐먼트를 batch 작업으로 분류한다.
    
    Args:
        limit: 처리할 최대 문서 수 (None이면 전체)
    '''
    def classify_raw_documents_in_batch(self, limit: int | None = None):
        current_version = self.etl_config.CLASSIFICATION_PROMPT_VERSION
        
        # 미분류 OR 버전 불일치(이전 버전) 문서 조회
        query = {
            "$or": [
                {"classification": {"$exists": False}},  # 미분류 문서
                {"classification.classification_prompt_version": {"$ne": current_version}}  # 버전 다른 문서
            ]
        }
        
        unclassified_docs = self.reddit_repository.find_raw_documents(query, limit=limit)
        
        self.logger.info(
            "분류 대상 조회 완료: 미분류 OR 버전 불일치 (현재 버전: %s)", 
            current_version
        )

        batch_docs = []
        expected_tokens = 0

        for unclassified_doc in unclassified_docs:
            reddit_id = unclassified_doc["reddit_id"]
            body = unclassified_doc["body"]
            token_count = estimate_classification_request_tokens(body)

            # 단일 문서가 토큰 한도를 넘으면 스킵 (그럴일은 없지만 혹시 모를 경우 무한 루프 에러 발생)
            if token_count > self.etl_config.TOKEN_LIMIT:
                self.logger.warning("(classification) 문서 %s 가 토큰 제한 초과, 스킵함",reddit_id)
                continue

            batch_doc = {"reddit_id": reddit_id, "body": body}

            # 이번 문서를 넣으면 초과 → 지금까지 배치 flush
            if expected_tokens + token_count > self.etl_config.TOKEN_LIMIT:
                self.logger.info("(classification) Batch %d개 문서 (%d tokens) 분류 요청",len(batch_docs), expected_tokens)
                batch_metadata = self.travel_scam_classifier.submit_classification_batch(batch_docs)
                self.reddit_repository.save_batch_job(batch_metadata)

                # 새 배치 시작
                batch_docs = [batch_doc]
                expected_tokens = token_count
            else:
                batch_docs.append(batch_doc)
                expected_tokens += token_count

        # 마지막 배치 처리
        if batch_docs:
            self.logger.info("(classification) 마지막 Batch %d개 문서 (%d tokens) 분류 요청",len(batch_docs), expected_tokens)
            batch_metadata = self.travel_scam_classifier.submit_classification_batch(batch_docs)
            self.reddit_repository.save_batch_job(batch_metadata)

    def process_classification_batch_results(self):
        return self.travel_scam_classifier.process_batch_results()
    
    def get_unprocessed_batch_count(self, job_type: str) -> int:
        unprocessed_batches = list(self.reddit_repository.find_unprocessed_batches(job_type))
        return len(unprocessed_batches)

    def parse_classified_documents_in_batch(self, limit: int | None = None):

        find_travel_scam_docs = self.reddit_repository.find_raw_documents(
            {"classification.is_travel_scam": True},
            limit=limit
        )

        parsed_ids = {doc["reddit_id"] for doc in self.reddit_repository.find_parsed_documents({}, projection={"reddit_id": 1})}

        batch_docs = []
        expected_tokens = 0

        for find_travel_scam_doc in find_travel_scam_docs:
            reddit_id = find_travel_scam_doc["reddit_id"]
            body = find_travel_scam_doc["body"]

            # 이미 parsed된 데이터는 pass
            if reddit_id in parsed_ids:
                continue

            token_count = estimate_parsing_request_tokens(body)

            # 단일 문서가 토큰 한도를 넘으면 스킵 (그럴일은 없지만 혹시 모를 경우 무한 루프 에러 발생)
            if token_count > self.etl_config.TOKEN_LIMIT:
                self.logger.warning("(parsing) 문서 %s 가 토큰 제한 초과, 스킵함", reddit_id)
                continue

            batch_doc = {"reddit_id": reddit_id, "body": body}

            # 이번 문서를 넣으면 초과 → 지금까지 배치 flush
            if expected_tokens + token_count > self.etl_config.TOKEN_LIMIT:
                self.logger.info("(parsing) Batch %d개 문서 (%d tokens) 파싱 요청", len(batch_docs), expected_tokens)
                batch_metadata = self.travel_scam_parser.submit_parsing_batch(batch_docs)
                self.reddit_repository.save_batch_job(batch_metadata)

                # 새 배치 시작
                batch_docs = [batch_doc]
                expected_tokens = token_count
            else:
                batch_docs.append(batch_doc)
                expected_tokens += token_count

            # 마지막 배치 처리
        if batch_docs:
            self.logger.info("마지막 Batch %d개 문서 (%d tokens) 파싱 요청", len(batch_docs), expected_tokens)
            batch_metadata = self.travel_scam_parser.submit_parsing_batch(batch_docs)
            self.reddit_repository.save_batch_job(batch_metadata)

    '''
    비동기로 요청한 parsing batch 결과를 mongo에 저장 (1단계:LLM 결과 저장)
    
    Returns:
        int: 처리된 배치 수
    '''
    def process_parsing_batch_results(self):
        batch_jobs = self.reddit_repository.find_unprocessed_batches("parsing")
        
        processed_count = 0

        for batch_job in batch_jobs:
            batch_id = batch_job["batch_id"]
            
            # 2. batch_id로부터 결과 및 상태 확인
            content, status = get_completed_batch_result(batch_id)
            
            # 3. 배치 상태별 처리
            if status == "completed" and content:
                # 결과 가공 및 저장 (Raw LLM 출력 + 메타데이터)
                parsing_results = []
                for line in content.splitlines():
                    if not line.strip():
                        continue

                    record = json.loads(line)

                    reddit_id = record["custom_id"]
                    text = record["response"]["body"]["output"][0]["content"][0]["text"]

                    parsed_body_results = self.travel_scam_parser.safe_json_loads(text)
                    for parsed_body_result in parsed_body_results:
                        now = datetime.now(UTC)
                        raw_json = self.reddit_repository.find_raw_document_by_reddit_id(reddit_id)

                        doc = {
                            # 기본 메타데이터
                            "reddit_id": reddit_id,
                            "source": "reddit",
                            "url": raw_json.get("url"),
                            "author": raw_json.get("author"),
                            "posted_at": raw_json.get("posted_at"),
                            "created_at": now,
                            "updated_at": now,

                            "title": parsed_body_result.get("title"),
                            "action": parsed_body_result.get("action"),
                            "context": parsed_body_result.get("context"),
                            "summary": parsed_body_result.get("summary"),
                            "country_name": parsed_body_result.get("country"),
                            "state_name": parsed_body_result.get("state"),
                            "city_name": parsed_body_result.get("city"),
                            
                            # DB Enrichment 결과 (초기값 None, 별도 enrichment 단계에서 채움)
                            "country_id": None,
                            "state_id": None,
                            "city_id": None,
                            
                            # 파싱 메타데이터
                            "parsing": {
                                "model_name": self.etl_config.MODEL_NAME,
                                "parsing_prompt_version": self.etl_config.PARSING_PROMPT_VERSION,
                                "parsed_at": now,
                                "location_enriched": False  # Location DB lookup 완료 여부
                            }
                        }
                        parsing_results.append(doc)

                    # BATCH_SIZE 단위로 저장
                    if len(parsing_results) >= self.etl_config.BATCH_SIZE:
                        self.reddit_repository.flush_parsing_results(parsing_results)

                # 남은 parsing_results 처리
                if parsing_results:
                    self.reddit_repository.flush_parsing_results(parsing_results)
                
                # 처리 완료 표시
                self.reddit_repository.mark_batch_as_processed(batch_id)
                processed_count += 1
                self.logger.info(f"✅ Batch {batch_id} 처리 완료 (Raw LLM 결과 저장)")
                
            elif status in ["failed", "expired", "cancelled"]:
                # 실패한 배치는 실패로 표시하고 건너뜀
                self.reddit_repository.mark_batch_as_failed(batch_id, status)
                self.logger.warning(f"⚠️ Batch {batch_id} {status} 상태로 건너뜀")
                
            else:
                # 아직 진행 중인 배치 (in_progress)는 로그만 남김
                self.logger.debug(f"⏳ Batch {batch_id} 아직 {status} 상태")
        
        return processed_count

    '''
    Location Enrichment - parsed_documents의 location 정보를 DB lookup하여 enrichment
    
    Returns:
        int: 처리된 문서 수
    '''
    def enrich_parsed_locations(self):
        query = {
            "parsing.location_enriched": False
        }
        
        unenriched_docs = self.reddit_repository.find_parsed_documents(query)
        
        enriched_count = 0
        
        for doc in unenriched_docs:
            reddit_id = doc["reddit_id"]
            country_name = doc.get("country_name")
            state_name = doc.get("state_name")
            city_name = doc.get("city_name")
            
            self.logger.debug(f"Processing {reddit_id}: country_name={country_name}, state_name={state_name}, city_name={city_name}")
            
            # Enricher를 통해 location 정보 조회
            location = self.travel_scam_enricher.enrich_location(country_name, state_name, city_name)
            
            if location:
                # 성공: country_name, state_name, city_name 이름과 각각의 id, 그리고 location_enriched=True로 업데이트
                self.reddit_repository.update_parsed_document_location(
                    reddit_id=reddit_id,
                    country_id=location.get("country_id"),
                    state_id=location.get("state_id"),
                    city_id=location.get("city_id"),
                    location_enriched=True
                )
                enriched_count += 1
                self.logger.info(f"✅ {reddit_id} enrichment 성공")
            else:
                # 실패: location_enriched는 여전히 False로 남음 (나중에 재시도 가능)
                self.logger.warning(f"⚠️ {reddit_id} enrichment 실패: country_name={country_name}, state_name={state_name}, city_name={city_name}")
        
        return enriched_count


    '''
        매일 실행되는 job
        MongoDB에서 Reddit raw 데이터를 가져와서 travel scam으로 분류 후
        keyword를 추출하고 정제된 결과를 MongoDB에 저장한다.
    '''
    def daily_transform(self):

        one_week_ago = datetime.now(UTC) - timedelta(days=7)
        query = {
            "posted_at": {"$gte": one_week_ago},
            "classification": {"$exists": False}
        } # 최근 1주일 + classification 서브 도큐먼트가 없는 도큐먼트

        classification_results = []
        # MongoDB에서 데이터 가져오기
        raw_docs = self.reddit_repository.find_raw_documents(query)
        for raw_doc in raw_docs:
            body = raw_doc.get("body")

            # scam 분류
            is_travel_scam = self.travel_scam_classifier.classify(body)

            # db저장
            classification_results.append({"reddit_id": raw_doc["reddit_id"], "is_travel_scam": is_travel_scam})
            if len(classification_results) >= self.etl_config.BATCH_SIZE:
                self.reddit_repository.flush_classification_results(classification_results)

        if classification_results:
            self.reddit_repository.flush_classification_results(classification_results)

        query = {
            "posted_at": {"$gte": one_week_ago}
        } # parsed_documents에 있는 최근 7일 모든 reddit_id 조회. projection reddit_id만
        projection = {
            "reddit_id": 1,
            "_id": 0
        }
        reddit_ids = {doc["reddit_id"] for doc in self.reddit_repository.find_parsed_documents(query, projection)}

        query = {
            "posted_at": {"$gte": one_week_ago},
            "classification.is_travel_scam": True
        } # 최근 7일에 classification 서브 도큐먼트의 is_travel_scam = true인 도큐먼트 + updated_at != created_at (이 조건은 이전에 삽입된 데이터는 제외)
        find_travel_scam_docs = self.reddit_repository.find_raw_documents(query)
        parsing_results = []

        for find_travel_scam_doc in find_travel_scam_docs:
            if find_travel_scam_doc["reddit_id"] in reddit_ids: continue
            parsed_body_results = self.travel_scam_parser.parse(find_travel_scam_doc["body"])
            for parsed_body_result in parsed_body_results:
                location_info = self.travel_scam_enricher.enrich_location(
                    parsed_body_result.get("country"),
                    parsed_body_result.get("state"),
                    parsed_body_result.get("city")
                )
                if location_info is None:
                    continue
                # 완전체 저장
                now = datetime.now(UTC)

                doc = {
                    "reddit_id": find_travel_scam_doc["reddit_id"],
                    "source": "reddit",
                    "url": find_travel_scam_doc.get("url"),
                    "author": find_travel_scam_doc.get("author"),
                    "title": parsed_body_result.get("title"),
                    "action": parsed_body_result.get("action"),
                    "context": parsed_body_result.get("context"),
                    "country_id": location_info.get("country_id"),
                    "state_id": location_info.get("state_id"),
                    "city_id": location_info.get("city_id"),
                    "summary": parsed_body_result.get("summary"),
                    "posted_at": find_travel_scam_doc.get("posted_at"),
                    "updated_at": now,
                    "created_at": now,
                }
                parsing_results.append(doc)

                # BATCH_SIZE 단위로 저장
            if len(parsing_results) >= self.etl_config.BATCH_SIZE:
                self.reddit_repository.flush_parsing_results(parsing_results)
                parsing_results.clear()

        # 남은 parsing_results 처리
        if parsing_results:
            self.reddit_repository.flush_parsing_results(parsing_results)
            parsing_results.clear()
