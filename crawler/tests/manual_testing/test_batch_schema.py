# tests/manual_testing/test_batch_schema.py
from adapters import mongo_client
from repository.reddit_repository import RedditRepository
from config.etl_config import ETLConfig

def test_batch_job_schema():
    """배치 작업 스키마 확인"""
    batch_collection = mongo_client.get_batch_job_collection()
    etl_config = ETLConfig.create_default()
    repo = RedditRepository(
        mongo_client.get_raw_collection(),
        mongo_client.get_parsed_collection(),
        batch_collection,
        etl_config
    )
    
    # 테스트 배치 저장
    test_metadata = {
        "batch_id": "test_batch_123",
        "input_file_id": "test_file_123",
        "job_type": "classification"
    }
    repo.save_batch_job(test_metadata)
    
    # 조회 확인
    saved = batch_collection.find_one({"batch_id": "test_batch_123"})
    
    print("✅ 저장된 배치:")
    print(f"  - batch_id: {saved['batch_id']}")
    print(f"  - processed: {saved['processed']}")
    print(f"  - status: {saved['status']}")
    
    # 미처리 배치 조회
    unprocessed = list(repo.find_unprocessed_batches("classification"))
    print(f"\n✅ 미처리 배치 수: {len(unprocessed)}")
    
    # 정리
    batch_collection.delete_one({"batch_id": "test_batch_123"})
    print("\n✅ 테스트 완료 (정리됨)")

if __name__ == "__main__":
    test_batch_job_schema()