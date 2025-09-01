from collector.transformer import travel_scam_parser
from adapters.reddit_client import get_instance
import time
import praw


KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


def crawl_travel_scam_with_url(reddit: praw.Reddit, url: str):

    submission = reddit.submission(url=url)  # url로 직접 게시글 가져오기
    post_body = clean_text(submission.selftext)
    result = travel_scam_parser.parse(post_body)
    print(result)


if __name__ == "__main__":
    start = time.time()
    url = 'https://www.reddit.com/r/travel/comments/1n4mdi1/weird_experience_in_hurghada_scam_or_not/'

    reddit = get_instance()

    result = crawl_travel_scam_with_url(reddit, url)

    end = time.time()

    print(f"테스트 실행 시간: {end - start:.2f} 초")
