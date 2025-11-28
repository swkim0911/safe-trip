from adapters import mongo_client
from repository.reddit_repository import RedditRepository
from config.etl_config import ETLConfig


def test_batch_schema():
    """배치 작업 스키마가 올바르게 저장되는지 테스트"""
    # 설정
    batch_collection = mongo_client.get_batch_job_collection()
    etl_config = ETLConfig.create_default()
    repository = RedditRepository(
        mongo_client.get_raw_collection(),
        mongo_client.get_parsed_collection(),
        batch_collection,
        etl_config
    )

    # 1. 배치 저장 테스트
    test_batch = {
        "batch_id": "test_batch_001",
        "input_file_id": "test_file_001",
        "job_type": "classification"
    }
    repository.save_batch_job(test_batch)

    # 2. 저장 확인
    saved = batch_collection.find_one({"batch_id": "test_batch_001"})
    assert saved is not None, "배치가 저장되지 않음"
    assert saved["processed"] == False, "processed 필드 오류"
    assert saved["status"] == "submitted", "status 필드 오류"

    # 3. 미처리 배치 조회 테스트
    unprocessed = list(repository.find_unprocessed_batches("classification"))
    assert len(unprocessed) >= 1, "미처리 배치 조회 실패"

    # 4. 완료 처리 테스트
    repository.mark_batch_as_processed("test_batch_001")
    processed = batch_collection.find_one({"batch_id": "test_batch_001"})
    assert processed["processed"] == True, "완료 처리 실패"
    assert processed["status"] == "completed", "상태 업데이트 실패"

    # 5. 미처리 배치 재조회 (이제 없어야 함)
    unprocessed_after = list(repository.find_unprocessed_batches("classification"))
    assert len(unprocessed_after) < len(unprocessed), "처리 후에도 미처리로 남아있음"

    # 정리
    batch_collection.delete_one({"batch_id": "test_batch_001"})


if __name__ == "__main__":
    test_batch_schema()
