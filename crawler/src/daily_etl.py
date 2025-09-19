import time

from adapters import mongo_client, reddit_client
from collector.extractor.travel_scam_extractor import TravelScamExtractor
from collector.loader.travel_scam_loader import TravelScamLoader
from collector.transformer.travel_scam_classifier import TravelScamClassifier
from collector.transformer.travel_scam_parser import TravelScamParser
from collector.transformer.travel_scam_transformer import TravelScamTransformer
from repository.reddit_repository import RedditRepository
from repository.world_repository import WorldRepository
from config.logging_config import setup_logging

def init_reddit_repository():
    return RedditRepository(
        mongo_client.get_raw_collection(),
        mongo_client.get_parsed_collection(),
        mongo_client.get_batch_job_collection()
    )


def init_world_repository():
    return WorldRepository(
        mongo_client.get_country_collection(),
        mongo_client.get_state_collection(),
        mongo_client.get_city_collection()
    )


if __name__ == "__main__":
    logger = setup_logging("daily_collector")

    start = time.time()
    logger.info("daily_collector 시작")
    reddit_repository = init_reddit_repository()
    world_repository = init_world_repository()

    # 1. extract (최근 일주일 데이터)
    travel_scam_extractor = TravelScamExtractor(reddit_repository)
    logger.info("TravelScamExtractor 시작")
    travel_scam_extractor.extract("week", None)
    logger.info("TravelScamExtractor 완료")

    # 2. transform
    travel_scam_transformer = TravelScamTransformer(
        TravelScamClassifier(reddit_repository), TravelScamParser(), reddit_repository, world_repository)
    logger.info("TravelScamTransformer(classify) 시작")
    travel_scam_transformer.daily_transform()
    logger.info("TravelScamTransformer(classify) 완료")

    # 3. load -> mysql
    travel_scam_loader = TravelScamLoader(reddit_repository)
    logger.info("파싱된 데이터 -> MySQL 적재 시작")
    travel_scam_loader.mongo_to_mysql("daily")
    logger.info("파싱된 데이터 -> MySQL 적재 완료")

    end = time.time()

    print(f"실행 시간: {end - start:.2f} 초")
