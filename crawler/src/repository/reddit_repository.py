import logging
from datetime import datetime, UTC

from pymongo import UpdateOne, InsertOne


class RedditRepository:
    """Reddit 데이터에 대한 MongoDB 접근을 담당하는 Repository 클래스"""
    
    def __init__(self, raw_collection, parsed_collection, batch_job_collection, etl_config):
        """
        Args:
            raw_collection: MongoDB raw 데이터 컬렉션
            parsed_collection: MongoDB parsed 데이터 컬렉션
            batch_job_collection: MongoDB batch job 컬렉션
            etl_config: ETL 설정 객체
        """
        self.raw_collection = raw_collection
        self.parsed_collection = parsed_collection
        self.batch_job_collection = batch_job_collection
        self.etl_config = etl_config
        self.logger = logging.getLogger(__name__)

    def find_raw_documents(self, query):
        return self.raw_collection.find(query)
    
    def flush_raw_records(self, records: list[dict]):
        if not records:
            return

        ops = []
        now = datetime.now(UTC)

        for record in records:

            ops.append(
                UpdateOne(
                    {"reddit_id": record["reddit_id"]},
                    {
                        "$set": {**record, "updated_at": now},
                        "$setOnInsert": {"created_at": now}
                    },
                    upsert=True
                )
            )
        try:
            result = self.raw_collection.bulk_write(ops, ordered=False)
        except Exception as e:
            self.logger.error(
                f"[flush_raw_records] Bulk write failed (records={len(records)}): {e}",
                exc_info=True
            )
            # 실패 시 clear하지 않음 → 재시도 가능
        else:
            self.logger.info(
                f"[flush_raw_records] Matched={result.matched_count}, "
                f"Modified={result.modified_count}, Upserted={len(result.upserted_ids)}"
            )
            records.clear()

    """
        records: [{"reddit_id": "xxx", "is_travel_scam": True}, ...]
    """
    def flush_classification_results(self, records: list[dict]):
        if not records:
            return
        
        ops = []

        for record in records:
            reddit_id = record["reddit_id"]
            is_travel_scam = record["is_travel_scam"]

            ops.append(
                UpdateOne(
                    {"reddit_id": reddit_id},
                    {
                        "$set": {
                            "classification": {
                                "is_travel_scam": is_travel_scam,
                                "model_name": self.etl_config.MODEL_NAME,
                                "classification_prompt_version": self.etl_config.CLASSIFICATION_PROMPT_VERSION,
                                "classified_at": datetime.now(UTC)
                            }
                        }
                    }
                )
            )

        try:
            result = self.raw_collection.bulk_write(ops, ordered=False)
        except Exception as e:
            self.logger.error(
                f"[flush_classification_results] Bulk write failed (records={len(records)}): {e}",
                exc_info=True
            )
        else:
            self.logger.info(
                f"[flush_classification_results] Matched={result.matched_count}, "
                f"Modified={result.modified_count}"
            )
            records.clear()

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

    """
    Flush parsing results to MongoDB.
    
    Args:
        records (list[dict]): [{"reddit_id": str, "parsed_result": dict}, ...]
    """
    def flush_parsing_results(self, records: list[dict]):
        if not records:
            return

        ops = []

        for record in records:
            ops.append(InsertOne(record))

        try:
            self.parsed_collection.bulk_write(ops, ordered=False)
        except Exception as e:
            self.logger.error(
                "Bulk write failed on parsed_collection "
                f"(records={len(records)}): {e}",
                exc_info=True
            )
        else:
            self.logger.info(
                f"Flushed {len(records)} parsing results to parsed_collection "
            )
            records.clear()