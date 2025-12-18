import time, sys

from config.dependencies import container
from config.logging_config import setup_logging

"""Enrich Location Job - LLM으로 부터 location 정보를 DB lookup하여 enrichment"""
def main():
    logger = setup_logging("enrich_location_job")

    try:
        start = time.time()
        logger.info("Enrich Location Job 시작")

        transformer = container.transformer

        logger.info("Location enrichment 작업 시작")
        enriched_count = transformer.enrich_parsed_locations()
        logger.info("✅ Location enrichment 완료: %d개 문서", enriched_count)

        end = time.time()
        logger.info("Enrich Location Job 완료 (실행 시간: %.2f 초)", end - start)

    except Exception as e:
        logger.error("Enrich Location Job 실패: %s", e, exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()



