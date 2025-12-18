import time

from config.dependencies import container
from config.logging_config import setup_logging


def main():
    logger = setup_logging("daily_collector")
    start = time.time()
    logger.info("daily_collector 시작")

    # 1. extract (최근 일주일 데이터)
    travel_scam_extractor = container.extractor
    logger.info("TravelScamExtractor 시작")
    travel_scam_extractor.extract("week", None)
    logger.info("TravelScamExtractor 완료")

    # 2. transform
    travel_scam_transformer = container.transformer
    logger.info("TravelScamTransformer(classify) 시작")
    travel_scam_transformer.daily_transform()
    logger.info("TravelScamTransformer(classify) 완료")

    # 3. load -> mysql
    travel_scam_loader = container.loader
    logger.info("파싱된 데이터 -> MySQL 적재 시작")
    travel_scam_loader.mongo_to_mysql("daily")
    logger.info("파싱된 데이터 -> MySQL 적재 완료")

    end = time.time()
    print(f"실행 시간: {end - start:.2f} 초")


if __name__ == "__main__":
    main()
