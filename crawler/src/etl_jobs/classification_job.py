"""Classification Job - Raw 데이터를 분류하고 결과를 저장하는 Job"""

import sys
import time

from config.dependencies import container
from config.logging_config import setup_logging


def main():
    """Classification Job 메인 함수"""
    # 로깅 설정
    logger = setup_logging("classification_job")
    
    try:
        start = time.time()
        logger.info("Classification Job 시작")
        
        # 의존성 주입
        transformer = container.transformer
        
        # 1. 분류 배치 요청
        logger.info("Raw 데이터 배치 분류 작업 시작")
        transformer.classify_raw_documents_in_batch()
        logger.info("Raw 데이터 배치 분류 작업 완료")
        # todo: 배치 결과  스케줄러로 확인
        # 2. 배치 결과 저장
        logger.info("분류된 데이터 DB 저장 시작")
        transformer.process_classification_batch_results()
        logger.info("분류된 데이터 DB 저장 완료")
        
        end = time.time()
        logger.info("Classification Job 완료 (실행 시간: %.2f 초)", end - start)
        
    except Exception as e:
        logger.error("Classification Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()

