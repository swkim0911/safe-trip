from adapters import mongo_client

class RedditRepository:
    def __init__(self):
        self.raw_collection = mongo_client.get_raw_collection()
        self.parsed_collection = mongo_client.get_parsed_collection()
    
    def flush_raw_ops(self, operations):
        # operations를 bulk_write 실행 후 비움
        if operations:
            try:
                result = self.raw_collection.bulk_write(operations, ordered=False)
                print(f"Matched: {result.matched_count}, "
                    f"Modified: {result.modified_count}, "
                    f"Upserted: {len(result.upserted_ids)}")
            except Exception as e:
                print(f"[ERROR] Bulk write failed: {e}")
            finally:
                operations.clear()
            
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
