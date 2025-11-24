"""Load Job - MongoDB에서 MySQL로 데이터를 적재하는 Job"""

import time

from config.dependencies import container
from config.logging_config import setup_logging
from etl_jobs.job_argparsers import build_load_parser


def main():
    """Load Job 메인 함수"""
    parser = build_load_parser()
    args = parser.parse_args()  # {load_scope="all"}이 담긴 객체로 변환

    logger = setup_logging("load_job")

    try:
        start = time.time()
        logger.info("Load Job 시작 (load_scope=%s)", args.load_scope)

        loader = container.loader

        logger.info("파싱된 데이터 → MySQL 적재 시작")
        loader.mongo_to_mysql(load_scope=args.load_scope)
        logger.info("파싱된 데이터 → MySQL 적재 완료")

        end = time.time()
        logger.info("Load Job 완료 (실행 시간: %.2f 초)", end - start)

    except Exception as e:
        logger.error("Load Job 실패: %s", e, exc_info=True)
        raise SystemExit(1)


if __name__ == "__main__":
    main()