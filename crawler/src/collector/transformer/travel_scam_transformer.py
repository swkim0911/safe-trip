from collector.transformer import travel_scam_classifier
from collector.transformer import travel_scam_parser
from datetime import datetime, timedelta, UTC

from adapters.mongo_client import get_parsed_collection, get_raw_collection
from pymongo import InsertOne

BATCH_SIZE = 100

def clean_text(text: str) -> str:
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
    raw_collection = get_raw_collection()
    parsed_collection = get_parsed_collection()
    operations = []
    
    def flush_ops(ops):
        # operations를 bulk_write 실행 후 비움
        if ops:
            result = parsed_collection.bulk_write(ops, ordered=False)
            print(f"Inserted {len(ops)} docs into parsed_collection")
            ops.clear() 
            
    # time_filter 조건 설정
    query = {}
    if time_filter == "7d":
        one_week_ago = datetime.utcnow() - timedelta(days=7)
        query = {"posted_at": {"$gte": one_week_ago}}

    # MongoDB에서 데이터 가져오기
    raw_jsons = raw_collection.find(query)
    
    existing_ids = set(parsed_collection.distinct("reddit_id"))

    for raw_json in raw_jsons:
        # raw_json의 id가 이미 parsed 컬랙션에 있으면 continue (llm api 비용 감면을 위해)
        if raw_json.get("reddit_id") in existing_ids:
            continue
        
        body = clean_text(raw_json.get("body", ""))
        if not body:
            continue

        # scam 분류
        is_scam = travel_scam_classifier.classify(body)
        if not is_scam:
            continue

        parsed_scams = travel_scam_parser.parse(body) or []

        for scam_record in parsed_scams:
            
            now = datetime.now(UTC)

            doc = {
                "reddit_id": raw_json.get("reddit_id"),  # raw id (unique 아님)
                "source": "reddit",
                "url": raw_json.get("url"),
                "author": raw_json.get("author"),
                "title": scam_record.get("title"),
                "scam_type": scam_record.get("scam_type"),
                "country": scam_record.get("country"),
                "state": scam_record.get("state"),
                "city": scam_record.get("city"),
                "summary": scam_record.get("summary"),
                "posted_at": raw_json.get("posted_at"),
                "created_at": now,
                "modified_at": now
            }
        

            # data insert (단순 insert)
            operations.append(InsertOne(doc))


            # batch 저장
            if len(operations) >= BATCH_SIZE:
                flush_ops(operations)

    # 남은 operations 처리
    flush_ops(operations)
