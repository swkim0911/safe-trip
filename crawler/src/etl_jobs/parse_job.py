import time, sys

from config.dependencies import container
from config.logging_config import setup_logging
from etl_jobs.cli.job_argparsers import create_parse_job_parser

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
        poll_interval = etl_config.BATCH_POLL_INTERVAL
        max_wait_time = etl_config.BATCH_MAX_WAIT_TIME
        elapsed_time = 0
        
        logger.info("배치 완료 대기 시작 (폴링 주기: %d초, 최대 대기: %d초)", poll_interval, max_wait_time)
        
        while elapsed_time < max_wait_time:
            # 배치 결과 처리 시도
            processed_count = transformer.process_parsing_batch_results()
            
            if processed_count > 0:
                logger.info("✅ %d개 배치 처리 완료", processed_count)
                # 추가로 처리할 배치가 있는지 확인
                unprocessed = transformer.get_unprocessed_batch_count("parsing")
                if unprocessed == 0:
                    logger.info("✅ 모든 배치 처리 완료")
                    break
                else:
                    logger.info("⏳ 아직 %d개 배치 대기 중", unprocessed)
            else:
                # 미완료 배치가 있는지 확인
                unprocessed = transformer.get_unprocessed_batch_count("parsing")
                if unprocessed == 0:
                    logger.info("✅ 처리할 배치가 없습니다")
                    break
                
                logger.info("⏳ %d개 배치 대기 중... %d분 후 재확인", unprocessed, poll_interval // 60)
            
            # 대기
            time.sleep(poll_interval)
            elapsed_time += poll_interval
        
        if elapsed_time >= max_wait_time:
            logger.warning("⚠️ 타임아웃: 일부 배치가 완료되지 않았습니다 (최대 대기 시간 %d시간 초과)", max_wait_time // 3600)
        
        end = time.time()
        logger.info("Parse Job 완료 (실행 시간: %.2f 초)", end - start)
        
    except Exception as e:
        logger.error("Parse Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()