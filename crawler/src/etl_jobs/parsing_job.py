"""Parsing Job - 분류된 데이터를 파싱하고 결과를 저장하는 Job"""

import sys
import time

from config.dependencies import container
from config.logging_config import setup_logging


def main():
    """Parsing Job 메인 함수"""
    # 로깅 설정
    logger = setup_logging("parsing_job")
    
    try:
        start = time.time()
        logger.info("Parsing Job 시작")
        
        # 의존성 주입
        transformer = container.transformer
        
        # 1. 파싱 배치 요청
        logger.info("분류된 문서 파싱 시작")
        transformer.parse_classified_documents_in_batch()
        logger.info("분류된 문서 파싱 완료")
        # todo: 배치 결과  스케줄러로 확인
        # 2. 배치 결과 저장
        logger.info("파싱된 데이터 DB 저장 시작")
        transformer.process_parsing_batch_results()
        logger.info("파싱된 데이터 DB 저장 완료")
        
        end = time.time()
        logger.info("Parsing Job 완료 (실행 시간: %.2f 초)", end - start)
        
    except Exception as e:
        logger.error("Parsing Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()