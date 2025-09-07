from collector.extractor.travel_scam_extractor import TravelScamExtractor
from collector.transformer.travel_scam_transformer import TravelScamTransformer
from collector.transformer.travel_scam_classifier import TravelScamClassifier
from collector.transformer.travel_scam_parser import TravelScamParser
from collector.loader.travel_scam_loader import TravelScamLoader

from repository.reddit_repository import RedditRepository
from repository.world_repository import WorldRepository
from adapters import mongo_client, openai_client, reddit_client

import time

def init_reddit_repository():
    return RedditRepository(
        mongo_client.get_raw_collection(),
        mongo_client.get_parsed_collection()
    )

def init_world_repository():
    return WorldRepository(
        mongo_client.get_country_collection(),
        mongo_client.get_state_collection(),
        mongo_client.get_city_collection()
    )


if __name__ == "__main__":
    start = time.time()
    reddit = reddit_client.get_instance()
    reddit_repository = init_reddit_repository()
    world_repository = init_world_repository()
    
    # 1. extract (전체 데이터)
    travel_scam_extractor = TravelScamExtractor(reddit, reddit_repository)
    travel_scam_extractor.extract("all", 500)
    

    # 2. transform
    travel_scam_transformer = TravelScamTransformer(TravelScamClassifier(), TravelScamParser(), reddit_repository, world_repository)
    travel_scam_transformer.transform("all")
    # 3. load -> mysql
    travel_scam_loader = TravelScamLoader(mongo_client.get_parsed_collection())
    
    end = time.time()

    print(f"실행 시간: {end - start:.2f} 초")
