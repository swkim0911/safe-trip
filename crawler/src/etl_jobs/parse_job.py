"""Parse Job - 분류된 데이터를 파싱하고 결과를 저장하는 Job."""
import time, sys

from config.dependencies import container
from config.logging_config import setup_logging
from etl_jobs.cli.job_argparsers import create_parse_job_parser
from utils.batch_utils import wait_for_batch, wait_for_capacity


def main():
    parser = create_parse_job_parser()
    args = parser.parse_args()

    logger = setup_logging("parse_job")

    try:
        start = time.time()
        logger.info("Parse Job 시작 (limit=%s)", args.limit)

        parser_obj = container.parser
        etl_config = container.etl_config

        def capacity_fn():
            wait_for_capacity(
                process_fn=parser_obj.process_batch_results,
                count_fn=parser_obj.get_unprocessed_batch_count,
                max_concurrent=20,
                poll_interval=etl_config.BATCH_POLL_INTERVAL,
                logger=logger,
            )

        logger.info("분류된 문서 배치 파싱 작업 시작")
        parser_obj.submit_documents_in_batch(limit=args.limit, capacity_fn=capacity_fn)
        logger.info("분류된 문서 배치 파싱 작업 완료")

        wait_for_batch(
            process_fn=parser_obj.process_batch_results,
            count_fn=lambda _: parser_obj.get_unprocessed_batch_count(),
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
