from collector import travel_scam_classifier
from collector import travel_scam_extractor
from adapters.reddit_client import get_instance
import time
import praw


def crawl_travel_scam(reddit: praw.Reddit, limit: int) -> None:
    KEYWORDS = ["'travel scam'"]
    subreddit = reddit.subreddit('travel')

    for post in subreddit.search(' OR '.join(KEYWORDS), sort="relevance", time_filter="all", limit=limit):
        post_body = post.selftext

        post.comments.replace_more(limit=0)

        # depth=1 댓글만 선택 (top-level comments)
        sum = 0
        for i, comment in enumerate(post.comments):
            sum += 1

        print("sum:", sum)


if __name__ == "__main__":
    start = time.time()
    reddit = get_instance()

    crawl_travel_scam(reddit, 20)

    end = time.time()

    print(f"테스트 실행 시간: {end - start:.2f} 초")
