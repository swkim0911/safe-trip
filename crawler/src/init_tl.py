import time

from adapters import mongo_client
from collector.loader.travel_scam_loader import TravelScamLoader
from collector.transformer.travel_scam_classifier import TravelScamClassifier
from collector.transformer.travel_scam_parser import TravelScamParser
from collector.transformer.travel_scam_transformer import TravelScamTransformer
from config.logging_config import setup_logging
from repository.reddit_repository import RedditRepository
from repository.world_repository import WorldRepository


def init_reddit_repository():
    return RedditRepository(
        mongo_client.get_raw_collection(),
        mongo_client.get_parsed_collection(),
        mongo_client.get_batch_job_collection(),
    )


def init_world_repository():
    return WorldRepository(
        mongo_client.get_country_collection(),
        mongo_client.get_state_collection(),
        mongo_client.get_city_collection()
    )

def init_object():
    reddit_repository = init_reddit_repository()
    world_repository = init_world_repository()

    travel_scam_transformer = TravelScamTransformer(TravelScamClassifier(reddit_repository), TravelScamParser(),
                          reddit_repository, world_repository)

    travel_scam_loader = TravelScamLoader(travel_scam_transformer)

    return travel_scam_transformer, travel_scam_loader


if __name__ == "__main__":
    start = time.time()

    logger = setup_logging("init_collector")
    logger.info("init_collector 실행 시작")

    travel_scam_transformer, travel_scam_loader = init_object()

    # 2. transform
    # 2.1.1 transform (classification)
    logger.info("분류된 데이터 db 저장 시작")
    travel_scam_transformer.process_classification_batch_results()
    logger.info("분류된 데이터 db 저장 완료")
    # 2.2 transform (paring)
    logger.info("분류된 문서 파싱 시작")
    travel_scam_transformer.parse_classified_documents_in_batch()
    logger.info("분류된 문서 파싱 완료")

    logger.info("파싱된 데이터 db 저장 시작")
    travel_scam_transformer.process_parsing_batch_results()
    logger.info("파싱된 데이터 db 저장 완료")

    logger.info("파싱된 데이터 -> MySQL 적재 시작")
    travel_scam_loader.mongo_to_mysql()
    logger.info("파싱된 데이터 -> MySQL 적재 완료")
    end = time.time()

    print(f"실행 시간: {end - start:.2f} 초")
