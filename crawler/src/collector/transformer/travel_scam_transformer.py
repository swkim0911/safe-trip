from datetime import datetime, timedelta, UTC
from pymongo import InsertOne

class TravelScamTransformer:
    def __init__(self, travel_scam_classifier, travel_scam_parser, reddit_repository, world_repository):
        self.travel_scam_classifier = travel_scam_classifier
        self.travel_scam_parser = travel_scam_parser
        self.reddit_repository = reddit_repository
        self.world_repository = world_repository
        self.BATCH_SIZE = 100
        

    def __clean_text(self, text: str) -> str:
        if not text:
            return ""
        return text.replace("\u200b", "").strip()


    '''
        MongoDB에서 Reddit raw 데이터를 가져와서
        travel scam으로 분류/추출한 뒤 정제된 결과를 MongoDB에 저장한다.

        Args:
            time_filter (str): "all" 또는 "7d" (최근 7일)
    '''
    def transform(time_filter: str):
        
        operations = []

        # time_filter 조건 설정 ("all, 7d")
        query = {}
        existing_ids = set()

        if time_filter == "7d":
            one_week_ago = datetime.utcnow() - timedelta(days=7)
            query = {"posted_at": {"$gte": one_week_ago}}
            existing_ids = set(parsed_collection.distinct(
                "reddit_id",
                {"posted_at": {"$gte": one_week_ago}}
            ))

        # MongoDB에서 데이터 가져오기
        
        raw_jsons = self.redditRepository.find_raw_documents(query)
        cnt = 0
        for raw_json in raw_jsons:
            # raw_json의 id가 이미 parsed 컬랙션에 있으면 continue (llm api 비용 감면을 위해)
            if raw_json.get("reddit_id") in existing_ids:
                continue

            body = self.__clean_text(raw_json.get("body", ""))

            # scam 분류
            is_scam = travel_scam_classifier.classify(body)
            if not is_scam:
                continue

            parsed_scams = self.travel_scam_parser.parse(body) or []

            for scam_record in parsed_scams:

                # db look up
                location_info = self.world_repository.lookup_location(scam_record.get("country"), scam_record.get("state"), scam_record.get("city"))
                if location_info is None:
                    continue

                now = datetime.now(UTC)

                doc = {
                    "reddit_id": raw_json.get("reddit_id"),  # raw id (unique 아님)
                    "source": "reddit",
                    "url": raw_json.get("url"),
                    "author": raw_json.get("author"),
                    "title": scam_record.get("title"),
                    "action": scam_record.get("action"),
                    "context": scam_record.get("context"),
                    "country_id": location_info.get("country_id"),
                    "state_id": location_info.get("state_id"),
                    "city_id": location_info.get("city_id"),
                    "summary": scam_record.get("summary"),
                    "posted_at": raw_json.get("posted_at"),
                    "created_at": now,
                    "modified_at": now
                }
                
                # data insert (단순 insert)
                operations.append(InsertOne(doc))

                # batch 저장
                if len(operations) >= BATCH_SIZE:
                    self.reddit_repository.flush_parsed_ops(operations)

        # 남은 operations 처리
        self.reddit_repository.flush_parsed_ops(operations)
