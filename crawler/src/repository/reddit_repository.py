from pymongo import UpdateOne
from datetime import datetime, UTC

class RedditRepository:
    def __init__(self, raw_collection, parsed_collection):
        self.raw_collection = raw_collection
        self.parsed_collection = parsed_collection
        
    def find_raw_documents(self, query):
        return self.raw_collection.find(query)
    
    
    def flush_raw_ops(self, docs: list[dict]):
        if not docs:
            return

        operations = []
        now = datetime.now(UTC)

        for doc in docs:
            operations.append(
                UpdateOne(
                    {"reddit_id": doc["reddit_id"]},
                    {
                        "$set": {**doc, "modified_at": now},
                        "$setOnInsert": {"created_at": now}
                    },
                    upsert=True
                )
            )

        result = self.raw_collection.bulk_write(operations, ordered=False)
        print(f"Matched: {result.matched_count}, "
              f"Modified: {result.modified_count}, "
              f"Upserted: {len(result.upserted_ids)}")
        docs.clear()
            
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
