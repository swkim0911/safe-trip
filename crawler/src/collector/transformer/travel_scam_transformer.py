from datetime import datetime, timedelta, UTC
from pymongo import InsertOne
from utils.token_utils import estimate_classification_request_tokens, estimate_parsing_request_tokens
from adapters.openai_client import get_completed_batch_result
import logging, json


class TravelScamTransformer:
    def __init__(self, travel_scam_classifier, travel_scam_parser, reddit_repository, world_repository):
        self.travel_scam_classifier = travel_scam_classifier
        self.travel_scam_parser = travel_scam_parser
        self.reddit_repository = reddit_repository
        self.world_repository = world_repository
        self.TOKEN_LIMIT = 500_000 # 원래는 200만이지만 보수적으로 LIMIT 설정
        self.BATCH_SIZE = 1000
        self.logger = logging.getLogger(__name__)

    '''
    batch classify
    '''
    def classify_raw_documents_in_batch(self):
        unclassified_docs = self.reddit_repository.find_raw_documents({"classification": {"$exists": False}})

        batch_docs = []
        expected_tokens = 0

        for unclassified_doc in unclassified_docs:
            reddit_id = unclassified_doc["reddit_id"]
            body = unclassified_doc["body"]
            token_count = estimate_classification_request_tokens(body)

            # 단일 문서가 토큰 한도를 넘으면 스킵 (그럴일은 없지만 혹시 모를 경우 무한 루프 에러 발생)
            if token_count > self.TOKEN_LIMIT:
                self.logger.warning("(classification) 문서 %s 가 토큰 제한 초과, 스킵함",reddit_id)
                continue

            batch_doc = {"reddit_id": reddit_id, "body": body}

            # 이번 문서를 넣으면 초과 → 지금까지 배치 flush
            if expected_tokens + token_count > self.TOKEN_LIMIT:
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
        # 비동기로 요청한 batch결과를 mongo에 저장
        self.travel_scam_classifier.process_batch_results()

    '''
    batch parsing
    '''
    def parse_classified_documents_in_batch(self):

        # paring 비동기 요청
        find_travel_scam_docs = self.reddit_repository.find_raw_documents({"classification.is_travel_scam": True})

        parsed_ids = {doc["reddit_id"] for doc in self.reddit_repository.find_parsed_documents({}, projection={"reddit_id": 1})}

        batch_docs = []
        expected_tokens = 0

        for find_travel_scam_doc in find_travel_scam_docs:
            reddit_id = find_travel_scam_doc["reddit_id"]
            body = find_travel_scam_doc["body"]

            if reddit_id in parsed_ids:
                continue

            token_count = estimate_parsing_request_tokens(body)

            # 단일 문서가 토큰 한도를 넘으면 스킵 (그럴일은 없지만 혹시 모를 경우 무한 루프 에러 발생)
            if token_count > self.TOKEN_LIMIT:
                self.logger.warning("(parsing) 문서 %s 가 토큰 제한 초과, 스킵함", reddit_id)
                continue

            batch_doc = {"reddit_id": reddit_id, "body": body}

            # 이번 문서를 넣으면 초과 → 지금까지 배치 flush
            if expected_tokens + token_count > self.TOKEN_LIMIT:
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

    def process_parsing_batch_results(self):
        batch_jobs = self.reddit_repository.find_batch_job_documents({"job_type": "parsing"})

        for batch_job in batch_jobs:
            # 2. batch_id로부터 content(JSONL 결과) 읽어오기
            batch_id = batch_job["batch_id"]
            content = get_completed_batch_result(batch_id)
            if not content:
                continue  # 혹시 실패했거나 아직 결과가 없으면 스킵

            # 3. 결과 가공
            parsing_results = []
            for line in content.splitlines():
                if not line.strip():
                    continue

                record = json.loads(line)

                reddit_id = record["custom_id"]
                text = record["response"]["body"]["output"][0]["content"][0]["text"]

                parsed_body_results = self.travel_scam_parser.safe_json_loads(text)
                for parsed_body_result in parsed_body_results:
                    location_info = self.world_repository.lookup_location(parsed_body_result.get("country"),parsed_body_result.get("state"),parsed_body_result.get("city"))
                    if location_info is None:
                        continue
                    # 완전체 저장
                    now = datetime.now(UTC)
                    raw_json = self.reddit_repository.find_one_raw_document({"reddit_id": reddit_id})
                    doc = {
                        "reddit_id": reddit_id,
                        "source": "reddit",
                        "url": raw_json.get("url"),
                        "author": raw_json.get("author"),
                        "title": parsed_body_result.get("title"),
                        "action": parsed_body_result.get("action"),
                        "context": parsed_body_result.get("context"),
                        "country_id": location_info.get("country_id"),
                        "state_id": location_info.get("state_id"),
                        "city_id": location_info.get("city_id"),
                        "summary": parsed_body_result.get("summary"),
                        "posted_at": raw_json.get("posted_at"),
                        "modified_at": now,
                        "created_at": now,
                    }
                    parsing_results.append(doc)

                # BATCH_SIZE 단위로 저장
                if len(parsing_results) >= self.BATCH_SIZE:
                    self.reddit_repository.flush_parsing_results(parsing_results)


            # 남은 parsing_results 처리
            if parsing_results:
                self.reddit_repository.flush_parsing_results(parsing_results)




    '''
        매일 실행되는 job
        MongoDB에서 Reddit raw 데이터를 가져와서 travel scam으로 분류 후
        keyword를 추출하고 정제된 결과를 MongoDB에 저장한다.

        Args:
            time_filter (str): "all" 또는 "7d" (최근 7일)
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
            if len(classification_results) >= self.BATCH_SIZE:
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
        } # 최근 7일에 classification 서브 도큐먼트의 is_travel_scam = true인 도큐먼트
        find_travel_scam_docs = self.reddit_repository.find_raw_documents(query)
        parsing_results = []

        for find_travel_scam_doc in find_travel_scam_docs:
            if find_travel_scam_doc["reddit_id"] in reddit_ids: continue
            parsed_body_results = self.travel_scam_parser.parse(find_travel_scam_doc["body"])
            for parsed_body_result in parsed_body_results:
                location_info = self.world_repository.lookup_location(parsed_body_result.get("country"),
                                                                      parsed_body_result.get("state"),
                                                                      parsed_body_result.get("city"))
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
                    "modified_at": now,
                    "created_at": now,
                }
                parsing_results.append(doc)

                # BATCH_SIZE 단위로 저장
            if len(parsing_results) >= self.BATCH_SIZE:
                self.reddit_repository.flush_parsing_results(parsing_results)

        # 남은 parsing_results 처리
        if parsing_results:
            self.reddit_repository.flush_parsing_results(parsing_results)
