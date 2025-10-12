"""Extract Job - Reddit에서 여행 사기 관련 데이터를 추출하는 Job"""

import sys
import time

from config.dependency_factory import DependencyFactory
from config.etl_config import ETLConfig
from config.logging_config import setup_logging


def main():
    """Extract Job 메인 함수"""
    # 인자 파싱
    if len(sys.argv) < 2:
        print("Usage: extract_job.py <time_filter> [limit]")
        print("  time_filter: week/all")
        print("  limit: 가져올 최대 게시글 수 (optional)")
        sys.exit(1)
    
    time_filter = sys.argv[1]
    limit = int(sys.argv[2]) if len(sys.argv) > 2 else None
    
    # 로깅 설정
    logger = setup_logging("extract_job")
    
    try:
        start = time.time()
        logger.info("Extract Job 시작 (time_filter=%s, limit=%s)", time_filter, limit)
        
        # 의존성 주입
        config = ETLConfig.create_default()
        factory = DependencyFactory()
        extractor = factory.create_extractor()
        
        # 데이터 추출
        logger.info("Reddit에서 Raw 데이터 수집 시작")
        extractor.extract(time_filter=time_filter, limit=limit)
        logger.info("Reddit에서 Raw 데이터 수집 완료")
        
        end = time.time()
        logger.info("Extract Job 완료 (실행 시간: %.2f 초)", end - start)
        
    except Exception as e:
        logger.error("Extract Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()