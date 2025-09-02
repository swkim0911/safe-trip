from adapters.reddit_client import get_instance
from collector.extractor import travel_scam_extractor
from collector.transformer import travel_scam_transformer

if __name__ == "__main__":
    reddit = get_instance()
    # 1. extract (전체 데이터)
    travel_scam_extractor.extract(reddit, "all", 300)
    # 2. transform
    travel_scam_transformer.transform("all")
    # 3. load -> mysql