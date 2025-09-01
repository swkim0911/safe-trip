from adapters.reddit_client import get_instance

if __name__ == "__main__":
    reddit = get_instance()
    # 1. extract (전체 데이터)
    # 2. transform
    # 3. load -> mysql