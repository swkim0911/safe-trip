"""현재 키워드/서브레딧 설정으로 검색되는 총 포스트 수 확인."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent / "src"))

from config.dependencies import container


def count_posts(time_filter: str = "all"):
    reddit = container.reddit_client
    config = container.etl_config

    subreddit_str = "+".join(config.REDDIT_SUBREDDITS)
    query = " OR ".join(config.REDDIT_KEYWORDS)

    print(f"\nsubreddits : {subreddit_str}")
    print(f"keywords   : {query}")
    print(f"time_filter: {time_filter}")
    print("\n검색 중...")

    count = 0
    for _ in reddit.subreddit(subreddit_str).search(query, sort="relevance", time_filter=time_filter, limit=None):
        count += 1
        if count % 100 == 0:
            print(f"  {count}개 확인 중...")

    print(f"\n총 포스트 수: {count}개")


if __name__ == "__main__":
    count_posts()
