from collector.transformer import travel_scam_classifier
from collector.transformer import travel_scam_parser
from datetime import datetime, timedelta, UTC

from adapters import mongo_client
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
    raw_collection = mongo_client.get_raw_collection()
    parsed_collection = mongo_client.get_parsed_collection()
    country_collection = mongo_client.get_country_collection()
    state_collection = mongo_client.get_state_collection()
    city_collection = mongo_client.get_city_collection()
    
    operations = []
    
    def flush_ops(ops):
        # operations를 bulk_write 실행 후 비움
        if ops:
            try:
                result = parsed_collection.bulk_write(ops, ordered=False)
                print(f"Inserted {len(ops)} docs into parsed_collection")
            except Exception as e:
                print(f"[ERROR] Bulk write failed: {e}")
            finally:
                ops.clear()
    ## todo: module로 빼기

    def lookup_location(country_name, state_name, city_name):

        quries = {"country:state:city": {"name": city_name, "state_name": state_name, "country_name": country_name},
                  "country:city": {"name": city_name, "country_name": country_name},
                  "state:city": {"name": city_name, "state_name": state_name},
                  "country:state": {"name": state_name, "country_name": country_name},
                  "city": {"name": city_name},
                  "state": {"name": state_name},
                  "country": {"name": country_name}}

        def find_city(query):
            doc = city_collection.find_one(query, {"_id": 1, "country_id": 1, "state_id": 1}, collation={
                                            "locale": "en", "strength": 1})
            if not doc:
                return None

            doc["city_id"] = doc.pop("_id")
            return doc

        def find_state(query):
            doc = state_collection.find_one(query, {"_id": 1, "country_id": 1}, collation={
                                            "locale": "en", "strength": 1})
            if not doc:
              return None

            doc["state_id"] = doc.pop("_id")
            return doc

        def find_country(query):
            doc = country_collection.find_one(query, {"_id": 1}, collation={
                                              "locale": "en", "strength": 1})
            if not doc:
              return None

            doc["country_id"] = doc.pop("_id")
            return doc

        match (bool(country_name), bool(state_name), bool(city_name)):
            case (True, True, True):
                return find_city(quries["country:state:city"]) or find_city(quries["country:city"]) or find_city(quries["state:city"]) or find_state(quries["country:state"]) or find_city(quries["city"]) or find_state(quries["state"]) or find_country(quries["country"])
            case (True, True, False):
                return find_state(quries["country:state"]) or find_state(quries["state"]) or find_country(quries["country"])
            case (True, False, True):
                return find_city(quries["country:city"]) or find_city(quries["city"]) or find_country(quries["country"])
            case (True, False, False):
                return find_country(quries["country"])
            case (False, True, True):
                return find_city(quries["state:city"]) or find_city(quries["city"]) or find_state(quries["state"])
            case (False, True, False):

                return find_state(quries["state"])
                # 여러 개여도 저장
            case (False, False, True):
                return find_city(quries["city"])
            case _:
                return None
            
    # time_filter 조건 설정
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
    raw_jsons = raw_collection.find(query)

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
            
            # db look up
            location_info = lookup_location(scam_record.get("country"), scam_record.get("state"), scam_record.get("city"))
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
                "country": location_info.get("country"),
                "state": location_info.get("state"),
                "city": location_info.get("city"),
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
