"""Classify Job - Raw 데이터를 분류하고 결과를 저장하는 Job."""
import time, sys

from config.dependencies import container
from config.logging_config import setup_logging
from etl_jobs.cli.job_argparsers import create_classify_job_parser
from utils.batch_utils import wait_for_batch, wait_for_capacity


def main():
    parser = create_classify_job_parser()
    args = parser.parse_args()

    logger = setup_logging("classify_job")

    try:
        start = time.time()
        logger.info("Classify Job 시작 (limit=%s)", args.limit)

        classifier = container.classifier
        etl_config = container.etl_config

        def capacity_fn():
            wait_for_capacity(
                process_fn=classifier.process_batch_results,
                count_fn=classifier.get_unprocessed_batch_count,
                max_concurrent=20,
                poll_interval=etl_config.BATCH_POLL_INTERVAL,
                logger=logger,
            )

        logger.info("Raw 데이터 배치 분류 작업 시작")
        classifier.submit_documents_in_batch(limit=args.limit, capacity_fn=capacity_fn)
        logger.info("Raw 데이터 배치 분류 작업 완료")

        wait_for_batch(
            process_fn=classifier.process_batch_results,
            count_fn=lambda _: classifier.get_unprocessed_batch_count(),
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
