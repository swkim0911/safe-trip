import time, sys

from config.dependencies import container
from config.logging_config import setup_logging
from etl_jobs.cli.job_argparsers import create_parse_job_parser
from utils.batch_utils import wait_for_batch

"""Parse Job - 분류된 데이터를 파싱하고 결과를 저장하는 Job"""
def main():
    parser = create_parse_job_parser()
    args = parser.parse_args()
    
    # 로깅 설정
    logger = setup_logging("parse_job")
    
    try:
        start = time.time()
        logger.info("Parse Job 시작 (limit=%s)", args.limit)
        
        # 의존성 주입
        transformer = container.transformer
        etl_config = container.etl_config
        
        # 1. 파싱 배치 요청
        logger.info("분류된 문서 배치 파싱 작업 시작")
        transformer.parse_classified_documents_in_batch(limit=args.limit)
        logger.info("분류된 문서 배치 파싱 작업 완료 - 배치 요청 제출됨")
        
        # 2. Polling으로 완료 대기
        wait_for_batch(
            process_fn=transformer.process_parsing_batch_results,
            count_fn=transformer.get_unprocessed_batch_count,
            batch_type="parsing",
            poll_interval=etl_config.BATCH_POLL_INTERVAL,
            max_wait_time=etl_config.BATCH_MAX_WAIT_TIME,
            logger=logger,
        )
        
        end = time.time()
        logger.info("Parse Job 완료 (실행 시간: %.2f 초)", end - start)
        
    except Exception as e:
        logger.error("Parse Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()