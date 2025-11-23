"""Load Job - MongoDB에서 MySQL로 데이터를 적재하는 Job"""

import sys
import time

from config.dependencies import container
from config.logging_config import setup_logging


def main():
    """Load Job 메인 함수"""
    
    if len(sys.argv) < 2:
        print("Usage:load_job.py <load_scope>")
        print("  load_scope: daily/all")
        sys.exit(1)

    load_scope = sys.argv[1]
    # 로깅 설정
    logger = setup_logging("load_job")
    
    try:
        start = time.time()
        logger.info("Load Job 시작")
        
        # 의존성 주입
        loader = container.loader
        
        # MongoDB → MySQL 데이터 적재
        logger.info("파싱된 데이터 → MySQL 적재 시작")
        loader.mongo_to_mysql(load_scope=load_scope)
        logger.info("파싱된 데이터 → MySQL 적재 완료")
        
        end = time.time()
        logger.info("Load Job 완료 (실행 시간: %.2f 초)", end - start)
        
    except Exception as e:
        logger.error("Load Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()