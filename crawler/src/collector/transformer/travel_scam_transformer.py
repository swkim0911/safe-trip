from collector.transformer import travel_scam_classifier
from collector.transformer import travel_scam_parser
from datetime import datetime, timedelta

from adapters.mongo_client import get_raw_collection


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


'''
[
  {
      "id": str,
      "source": "reddit",
      "url": str,
      "author": str,
      "title": str,
      "scam_type": str,
      "country": str(nullable),
      "state": str(nullable),
      "city": str(nullable),
      "summary": str,
      "posted_at": datetime,
      "created_at": datetime,
      "updated_at": datetime
  }
]
'''

def crawl_travel_scams(time_filter: str):
    """
    MongoDB에서 Reddit raw 데이터를 가져와서
    travel scam으로 분류/추출한 뒤 정제된 결과를 반환한다.

    Args:
        time_filter (str): "all" 또는 "7d" (최근 7일)
    Returns:
        list[dict]: 정제된 scam 데이터 목록
    """

    raw_collection = get_raw_collection()

    # time_filter 조건 설정
    query = {}
    if time_filter == "7d":
        one_week_ago = datetime.utcnow() - timedelta(days=7)
        query = {"posted_at": {"$gte": one_week_ago}}

    # MongoDB에서 데이터 가져오기
    raw_posts = raw_collection.find(query)

    collected_results = []

    for raw_json in raw_posts:
        body = clean_text(raw_json.get("body", ""))
        is_scam = travel_scam_classifier.classify(body)

        if is_scam:
            parsed_scams = travel_scam_parser.parse(body) or []

            for scam_record in parsed_scams:
                collected_results.append({
                    "external_id": raw_json.get("id"),
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
                    "created_at": raw_json.get("created_at"),
                    "modified_at": raw_json.get("modified_at")
                })

    return collected_results
