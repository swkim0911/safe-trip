from pymongo import UpdateOne
from datetime import datetime, UTC
import os

class RedditRepository:
    def __init__(self, raw_collection, parsed_collection):
        self.raw_collection = raw_collection
        self.parsed_collection = parsed_collection
        
    def find_raw_documents(self, query):
        return self.raw_collection.find(query)
    
    def find_raw_documents_with_limit(self, query, limit):
        return self.raw_collection.find(query).limit(limit)
    
    def flush_raw_docs(self, docs: list[dict]):
        if not docs:
            return

        ops = []
        now = datetime.now(UTC)

        for doc in docs:
            ops.append(
                UpdateOne(
                    {"reddit_id": doc["reddit_id"]},
                    {
                        "$set": {**doc, "modified_at": now},
                        "$setOnInsert": {"created_at": now}
                    },
                    upsert=True
                )
            )

        result = self.raw_collection.bulk_write(ops, ordered=False)
        print(f"Matched: {result.matched_count}, "  # 조건에 걸린 docuement 수
              f"Modified: {result.modified_count}, "  # 실제 값이 변경된 document 수
              f"Upserted: {len(result.upserted_ids)}")  # upsert로 새로 추가된 document 수
        
        docs.clear()
        
    """
        items: [{"reddit_id": "xxx", "is_travel_scam": True}, ...]
    """
    def flush_classification_results(self, items: list[dict]):
        if not items:
            return
        
        ops = []
        MODEL_NAME = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
        PROMPT_VERSION = os.getenv("CLASSIFICATION_PROMPT_VERSION", "v0")
    

        for item in items:
            reddit_id = item["reddit_id"]
            is_travel_scam = item["is_travel_scam"]

            ops.append(
                UpdateOne(
                    {"reddit_id": reddit_id},
                    {
                        "$set": {
                            "classification": {
                                "reddit_id": reddit_id,
                                "is_travel_scam": is_travel_scam,
                                "model_name": MODEL_NAME,
                                "classification_prompt_version": PROMPT_VERSION,
                                "classified_at": datetime.now(UTC)
                            }
                        }
                    }
                )
            )
        result = self.raw_collection.bulk_write(ops, ordered=False)
        print(f"Matched: {result.matched_count}, "  # 조건에 걸린 docuement 수
              f"Modified: {result.modified_count}")  # 실제 값이 변경된 document 수

        items.clear()
            
    def flush_parsed_ops(self, operations):
        # operations를 bulk_write 실행 후 비움
        if operations:
            try:
                result = self.parsed_collection.bulk_write(operations, ordered=False)
                print(f"Inserted {len(operations)} docs into parsed_collection")
            except Exception as e:
                print(f"[ERROR] Bulk write failed: {e}")
            finally:
                operations.clear()
