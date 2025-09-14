from datetime import datetime, UTC
from pymongo import UpdateOne
import logging
import os

class RedditRepository:
    MODEL_NAME = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
    CLASSIFICATION_PROMPT_VERSION = os.getenv("CLASSIFICATION_PROMPT_VERSION", "v0")
    PARSING_PROMPT_VERSION = os.getenv("PARSING_PROMPT_VERSION", "v0")

    def __init__(self, raw_collection, parsed_collection, batch_job_collection):
        self.raw_collection = raw_collection
        self.parsed_collection = parsed_collection
        self.batch_job_collection = batch_job_collection
        self.logger = logging.getLogger(__name__)

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
        self.logger.info(f"Matched: {result.matched_count}, "  # 조건에 걸린 docuement 수
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

        for item in items:
            reddit_id = item["reddit_id"]
            is_travel_scam = item["is_travel_scam"]

            ops.append(
                UpdateOne(
                    {"reddit_id": reddit_id},
                    {
                        "$set": {
                            "classification": {
                                "is_travel_scam": is_travel_scam,
                                "model_name": self.MODEL_NAME,
                                "classification_prompt_version": self.CLASSIFICATION_PROMPT_VERSION,
                                "classified_at": datetime.now(UTC)
                            }
                        }
                    }
                )
            )
        result = self.raw_collection.bulk_write(ops, ordered=False)
        self.logger.info(f"Matched: {result.matched_count}, "  # 조건에 걸린 document 수
              f"Modified: {result.modified_count}")  # 실제 값이 변경된 document 수

        items.clear()

    def save_batch_job(self, batch_metadata):
        doc = {
            "batch_id": batch_metadata["batch_id"],
            "input_file_id": batch_metadata["input_file_id"],
            "job_type": batch_metadata["job_type"],
            "submitted_at": datetime.now(UTC),
        }
        self.batch_job_collection.insert_one(doc)

    def find_batch_job_documents(self, query):
        return self.batch_job_collection.find(query)

    def find_parsed_documents(self, query, projection=None):
        return self.parsed_collection.find(query, projection)

    def find_one_raw_document(self, query):
        return self.raw_collection.find_one(query)
    # """
    # dict: {"reddit_id": reddit_id, "parsed_result": json}
    # parsed_result:
    # {
    #     "title": "",
    #     "action": "",
    #     "context": "",
    #     "country": "", nullable
    #     "state": "", nullable
    #     "city": "", nullable
    #     "summary": ""
    # }
    # """
    # def flush_parsing_results(self, items: list[dict]):
    #     return

    def flush_parsed_ops(self, operations):
        # operations를 bulk_write 실행 후 비움
        if operations:
            try:
                result = self.parsed_collection.bulk_write(operations, ordered=False)
                self.logger.info(f"Inserted {len(operations)} docs into parsed_collection")
            except Exception as e:
                self.logger.error(f"[ERROR] Bulk write failed: {e}")
            finally:
                operations.clear()
