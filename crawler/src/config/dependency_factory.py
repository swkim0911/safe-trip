
from adapters import mongo_client
from adapters.reddit_client import get_instance as get_reddit_instance
from collector.extractor.travel_scam_extractor import TravelScamExtractor
from collector.loader.travel_scam_loader import TravelScamLoader
from collector.transformer.travel_scam_classifier import TravelScamClassifier
from collector.transformer.travel_scam_parser import TravelScamParser
from collector.transformer.travel_scam_transformer import TravelScamTransformer
from config.etl_config import ETLConfig
from repository.reddit_repository import RedditRepository
from repository.world_repository import WorldRepository


class DependencyFactory:
    """의존성 객체를 한 곳에서 생성하고 관리하는 Factory 클래스 (Singleton 패턴)"""
    """의존성 객체는 lazy loading 방식으로 생성된다."""
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._initialized = False
        return cls._instance

    def __init__(self):
        if self._initialized:
            return

        self.config = ETLConfig.create_default()
        self._reddit_repository = None
        self._world_repository = None
        self._reddit_client = None
        self._initialized = True

    def get_reddit_repository(self) -> RedditRepository:
        if self._reddit_repository is None:
            self._reddit_repository = RedditRepository(
                raw_collection=mongo_client.get_raw_collection(),
                parsed_collection=mongo_client.get_parsed_collection(),
                batch_job_collection=mongo_client.get_batch_job_collection(),
                config=self.config
            )
        return self._reddit_repository

    def get_world_repository(self) -> WorldRepository:
        if self._world_repository is None:
            self._world_repository = WorldRepository(
                country_collection=mongo_client.get_country_collection(),
                state_collection=mongo_client.get_state_collection(),
                city_collection=mongo_client.get_city_collection()
            )
        return self._world_repository

    def get_reddit_client(self):
        if self._reddit_client is None:
            self._reddit_client = get_reddit_instance()
        return self._reddit_client

    def create_extractor(self) -> TravelScamExtractor:
        return TravelScamExtractor(
            reddit_client=self.get_reddit_client(),
            reddit_repository=self.get_reddit_repository(),
            config=self.config
        )

    def create_classifier(self) -> TravelScamClassifier:
        return TravelScamClassifier(
            reddit_repository=self.get_reddit_repository(),
            config=self.config
        )

    def create_parser(self) -> TravelScamParser:
        return TravelScamParser(config=self.config)

    def create_transformer(self) -> TravelScamTransformer:
        return TravelScamTransformer(
            travel_scam_classifier=self.create_classifier(),
            travel_scam_parser=self.create_parser(),
            reddit_repository=self.get_reddit_repository(),
            world_repository=self.get_world_repository(),
            config=self.config
        )

    def create_loader(self) -> TravelScamLoader:
        return TravelScamLoader(
            reddit_repository=self.get_reddit_repository(),
            config=self.config
        )
