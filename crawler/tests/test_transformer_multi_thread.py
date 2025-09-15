from collector.transformer.travel_scam_classifier import TravelScamClassifier
from collector.transformer.travel_scam_parser import TravelScamParser
from repository.world_repository import WorldRepository
from repository.reddit_repository import RedditRepository

from adapters import mongo_client


from adapters.reddit_client import get_instance
from datetime import datetime, timedelta, UTC
import time

from concurrent.futures import ThreadPoolExecutor, as_completed

def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()

def process_raw_json(raw_json, travel_scam_classifier, travel_scam_parser, world_repository):
    body = clean_text(raw_json.get("body", ""))
    if not body:
        return None

    # scam 분류
    is_scam = travel_scam_classifier.classify(body)
    if not is_scam:
        return None

    parsed_scams = travel_scam_parser.parse(body) or []
    results = []

    for scam_record in parsed_scams:
        location_info = world_repository.lookup_location(
            scam_record.get("country"), scam_record.get("state"), scam_record.get("city")
        )
        if location_info is None:
            continue

        now = datetime.now(UTC)
        doc = {
            "reddit_id": raw_json.get("reddit_id"),
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
        results.append(doc)
    return results

def transform():
    raw_collection = mongo_client.get_raw_collection()
    parsed_collection = mongo_client.get_parsed_collection()
    country_collection = mongo_client.get_country_collection()
    state_collection = mongo_client.get_state_collection()
    city_collection = mongo_client.get_city_collection()
    
    reddit_repository = RedditRepository(raw_collection, parsed_collection)
    world_repository = WorldRepository(country_collection, state_collection, city_collection)
    travel_scam_classifier = TravelScamClassifier()
    travel_scam_parser = TravelScamParser()

    results = []            
    query = {}

    # MongoDB에서 데이터 가져오기
    raw_jsons = raw_collection.find(query).limit(10)
    
    with ThreadPoolExecutor(max_workers=8) as executor:  # 워커 스레드 8개
        futures = [executor.submit(process_raw_json, raw_json, travel_scam_classifier, travel_scam_parser, world_repository) for raw_json in raw_jsons]
        for future in as_completed(futures):
            docs = future.result()
            if docs:
                results.extend(docs)

    # 결과 출력
    for doc in results:
        print(doc)
    print(f"총 결과 수: {len(results)}")

if __name__ == "__main__":
    reddit = get_instance()
    start = time.time()

    transform()


    end = time.time()

    print(f"테스트 실행 시간: {end - start:.2f} 초")
