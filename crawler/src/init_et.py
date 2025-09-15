from collector.extractor.travel_scam_extractor import TravelScamExtractor
from collector.transformer.travel_scam_transformer import TravelScamTransformer
from collector.transformer.travel_scam_classifier import TravelScamClassifier
from collector.transformer.travel_scam_parser import TravelScamParser

from repository.reddit_repository import RedditRepository
from repository.world_repository import WorldRepository
from adapters import mongo_client

from config.logging_config import setup_logging

import time

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
    logger = setup_logging("init_collector")

    start = time.time()
    logger.info("init_collector 시작")

    reddit_repository = init_reddit_repository()
    world_repository = init_world_repository()
    
    # 1. extract
    travel_scam_extractor = TravelScamExtractor(reddit_repository)
    logger.info("TravelScamExtractor 시작")
    travel_scam_extractor.extract("all", None)
    logger.info("TravelScamExtractor 완료")

    # 2.1 transform (classify with batch)
    travel_scam_transformer = TravelScamTransformer(TravelScamClassifier(reddit_repository), TravelScamParser(), reddit_repository, world_repository)
    logger.info("TravelScamTransformer(classify) 시작")
    travel_scam_transformer.classify_raw_documents_in_batch()
    logger.info("TravelScamTransformer(classify) 완료")

    end = time.time()

    logger.info("init_collector 실행 시간: %.2f 초", end - start)
