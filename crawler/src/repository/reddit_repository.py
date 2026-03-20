import logging
from datetime import datetime, UTC

from pymongo import UpdateOne, InsertOne


class RedditRepository:
    """Reddit 데이터에 대한 MongoDB 접근을 담당하는 Repository 클래스"""
    
    def __init__(self, raw_collection, parsed_collection, batch_job_collection, etl_config):
        self.raw_collection = raw_collection
        self.parsed_collection = parsed_collection
        self.batch_job_collection = batch_job_collection
        self.etl_config = etl_config
        self.logger = logging.getLogger(__name__)

    def find_raw_documents(self, query, limit: int | None = None):
        cursor = self.raw_collection.find(query)
        if limit is not None:
            cursor = cursor.limit(limit)
        return cursor
    
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

    def flush_classification_results(self, records: list[dict]):
        """
        Args:
            records: [{"reddit_id": "xxx", "is_travel_scam": True}, ...]
        """
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
            "processed": False,  # 처리 여부
            "processed_at": None,  # 처리 완료 시각
            "status": "submitted"  # submitted, completed, failed, expired, cancelled
        }
        self.batch_job_collection.insert_one(doc)

    def find_unprocessed_batches(self, job_type: str):
        """미처리 배치 조회"""
        query = {
            "job_type": job_type,
            "processed": False
        }
        return self.batch_job_collection.find(query)
    
    def mark_batch_as_processed(self, batch_id: str):
        """배치를 처리 완료로 표시"""
        self.batch_job_collection.update_one(
            {"batch_id": batch_id},
            {
                "$set": {
                    "processed": True,
                    "processed_at": datetime.now(UTC),
                    "status": "completed"
                }
            }
        )
        self.logger.info("Batch %s marked as processed", batch_id)
    
    def mark_batch_as_failed(self, batch_id: str, status: str):
        """실패/만료 배치 표시"""
        self.batch_job_collection.update_one(
            {"batch_id": batch_id},
            {
                "$set": {
                    "processed": True,
                    "processed_at": datetime.now(UTC),
                    "status": status
                }
            }
        )
        self.logger.warning("Batch %s marked as %s", batch_id, status)

    def find_parsed_documents(self, query, projection=None):
        return self.parsed_collection.find(query, projection)

    def find_unenriched_parsed_documents(self):
        query = {
            "parsing.location_enriched": False
        }
        return self.parsed_collection.find(query)

    def find_raw_document_by_reddit_id(self, reddit_id: str):
        return self.raw_collection.find_one({"reddit_id": reddit_id})

    def find_raw_documents_by_reddit_ids(self, reddit_ids: list[str]) -> dict[str, dict]:
        """reddit_id 목록으로 raw documents를 한 번에 조회"""
        docs = self.raw_collection.find(
            {"reddit_id": {"$in": reddit_ids}},
            {"reddit_id": 1, "url": 1, "author": 1, "posted_at": 1}
        )
        return {doc["reddit_id"]: doc for doc in docs}

    def update_parsed_document_location(self, reddit_id: str, country_id, state_id, city_id, location_enriched: bool):
        """
        Parsed document의 location 정보 업데이트
        
        Args:
            reddit_id: Reddit ID
            country_id: Country ID (또는 None)
            state_id: State ID (또는 None)
            city_id: City ID (또는 None)
            location_enriched: Location enrichment 완료 여부
        """
        self.parsed_collection.update_one(
            {"reddit_id": reddit_id},
            {
                "$set": {
                    "country_id": country_id,
                    "state_id": state_id,
                    "city_id": city_id,
                    "parsing.location_enriched": location_enriched,
                    "updated_at": datetime.now(UTC)
                }
            }
        )

    def flush_parsing_results(self, records: list[dict]):
        """Flush parsing results to MongoDB.

        Args:
            records: [{"reddit_id": str, ...}, ...]
        """
        if not records:
            return

        ops = []
        for record in records:
            ops.append(
                UpdateOne(
                    {"reddit_id": record["reddit_id"]},
                    {"$set": record},
                    upsert=True
                )
            )

        try:
            result = self.parsed_collection.bulk_write(ops, ordered=False)
        except Exception as e:
            self.logger.error(
                "Bulk write failed on parsed_collection "
                f"(records={len(records)}): {e}",
                exc_info=True
            )
        else:
            self.logger.info(
                f"Flushed {len(records)} parsing results to parsed_collection "
                f"(Matched={result.matched_count}, Modified={result.modified_count}, "
                f"Upserted={len(result.upserted_ids)})"
            )
            records.clear()