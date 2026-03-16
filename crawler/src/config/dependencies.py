from functools import cached_property

from dotenv import load_dotenv
load_dotenv()

from adapters import mongo_client
from adapters.reddit_client import get_instance as get_reddit_instance
from collector.extractor.travel_scam_extractor import TravelScamExtractor
from collector.loader.travel_scam_loader import TravelScamLoader
from collector.transformer.travel_scam_classifier import TravelScamClassifier
from collector.transformer.travel_scam_enricher import TravelScamEnricher
from collector.transformer.travel_scam_parser import TravelScamParser
from collector.transformer.travel_scam_transformer import TravelScamTransformer
from config.etl_config import ETLConfig
from repository.reddit_repository import RedditRepository
from repository.world_repository import WorldRepository


class Container:
    """Pythonic Dependency Container
    - 모듈 레벨 싱글톤
    - lazy property 기반 (cached_property)
    """

    def __init__(self):
        self.etl_config = ETLConfig.create_default()

    # ---- Repositories ----
    @cached_property
    def reddit_repository(self) -> RedditRepository:
        return RedditRepository(
            raw_collection=mongo_client.get_raw_collection(),
            parsed_collection=mongo_client.get_parsed_collection(),
            batch_job_collection=mongo_client.get_batch_job_collection(),
            etl_config=self.etl_config,
        )

    @cached_property
    def world_repository(self) -> WorldRepository:
        return WorldRepository(
            country_collection=mongo_client.get_country_collection(),
            state_collection=mongo_client.get_state_collection(),
            city_collection=mongo_client.get_city_collection(),
        )

    @cached_property
    def reddit_client(self):
        return get_reddit_instance()

    @cached_property
    def extractor(self) -> TravelScamExtractor:
        return TravelScamExtractor(
            reddit_client=self.reddit_client,
            reddit_repository=self.reddit_repository,
            etl_config=self.etl_config,
        )

    @cached_property
    def classifier(self) -> TravelScamClassifier:
        return TravelScamClassifier(
            reddit_repository=self.reddit_repository,
            etl_config=self.etl_config,
        )

    @cached_property
    def enricher(self) -> TravelScamEnricher:
        return TravelScamEnricher(
            world_repository=self.world_repository,
        )

    @cached_property
    def transformer(self) -> TravelScamTransformer:
        return TravelScamTransformer(
            travel_scam_classifier=self.classifier,
            travel_scam_parser=self.parser,
            travel_scam_enricher=self.enricher,
            reddit_repository=self.reddit_repository,
            etl_config=self.etl_config,
        )

    @cached_property
    def parser(self) -> TravelScamParser:
        return TravelScamParser(config=self.etl_config)

    @cached_property
    def loader(self) -> TravelScamLoader:
        return TravelScamLoader(
            reddit_repository=self.reddit_repository,
            etl_config=self.etl_config,
        )

# ---- 모듈 레벨에서 싱글톤 Container 인스턴스 생성 ----
container = Container()
