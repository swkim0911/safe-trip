from adapters.reddit_client import get_instance
from collector import extractor
import time

if __name__ == "__main__":
    start = time.time()
    reddit = get_instance()
    extractor.extract(reddit, 300)
    end = time.time()

    print(f"실행 시간: {end - start:.2f} 초")
