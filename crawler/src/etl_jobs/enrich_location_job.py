"""Enrich Location Job - LLM이 추출한 location 문자열을 DB ID로 매핑하는 Job."""
import time, sys

from config.dependencies import container
from config.logging_config import setup_logging


def main():
    logger = setup_logging("enrich_location_job")

    try:
        start = time.time()
        logger.info("Enrich Location Job 시작")

        enricher = container.enricher

        logger.info("Location enrichment 작업 시작")
        enriched_count = enricher.enrich_all()
        logger.info("Location enrichment 완료: %d개 문서", enriched_count)

        end = time.time()
        logger.info("Enrich Location Job 완료 (실행 시간: %.2f 초)", end - start)

    except Exception as e:
        logger.error("Enrich Location Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()
