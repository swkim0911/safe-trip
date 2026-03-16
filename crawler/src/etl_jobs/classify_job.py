import time, sys

from config.dependencies import container
from config.logging_config import setup_logging
from etl_jobs.cli.job_argparsers import create_classify_job_parser
from utils.batch_utils import wait_for_batch

"""Classify Job - Raw 데이터를 분류하고 결과를 저장하는 Job"""
def main():
    parser = create_classify_job_parser()
    args = parser.parse_args()
    
    # 로깅 설정
    logger = setup_logging("classify_job")
    
    try:
        start = time.time()
        logger.info("Classify Job 시작 (limit=%s)", args.limit)
        
        # 의존성 주입
        transformer = container.transformer
        etl_config = container.etl_config
        
        # 1. 분류 배치 요청
        logger.info("Raw 데이터 배치 분류 작업 시작")
        transformer.classify_raw_documents_in_batch(limit=args.limit)
        logger.info("Raw 데이터 배치 분류 작업 완료 - 배치 요청 제출됨")
        
        # 2. Polling으로 완료 대기
        wait_for_batch(
            process_fn=transformer.process_classification_batch_results,
            count_fn=transformer.get_unprocessed_batch_count,
            batch_type="classification",
            poll_interval=etl_config.BATCH_POLL_INTERVAL,
            max_wait_time=etl_config.BATCH_MAX_WAIT_TIME,
            logger=logger,
        )
        
        end = time.time()
        logger.info("Classify Job 완료 (실행 시간: %.2f 초)", end - start)
        
    except Exception as e:
        logger.error("Classify Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()


