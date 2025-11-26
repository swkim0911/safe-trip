import time, sys

from config.dependencies import container
from config.logging_config import setup_logging
from etl_jobs.job_argparsers import build_extract_parser

"""Extract Job - Reddit에서 여행 사기 관련 데이터를 추출하는 Job"""
def main():
    parser = build_extract_parser()
    args = parser.parse_args()  # {time_filter="all", limit=10}이 담긴 객체로 변환

    logger = setup_logging("extract_job")

    try:
        start = time.time()
        logger.info("Extract Job 시작 (time_filter=%s, limit=%s)", args.time_filter, args.limit)

        extractor = container.extractor

        logger.info("Reddit에서 Raw 데이터 수집 시작")
        extractor.extract(time_filter=args.time_filter, limit=args.limit)
        logger.info("Reddit에서 Raw 데이터 수집 완료")

        end = time.time()
        logger.info("Extract Job 완료 (실행 시간: %.2f 초)", end - start)

    except Exception as e:
        logger.error("Extract Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()