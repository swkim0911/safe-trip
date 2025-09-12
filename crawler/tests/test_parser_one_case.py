from collector.transformer.travel_scam_parser import TravelScamParser
from adapters.reddit_client import get_instance
import time
import praw


KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


def parse_travel_scam_with_url(reddit: praw.Reddit, url: str):
    submission = reddit.submission(url=url)  # url로 직접 게시글 가져오기
    post_body = clean_text(submission.selftext)
    travel_scam_parser = TravelScamParser()

    return travel_scam_parser.parse(post_body)


if __name__ == "__main__":
    start = time.time()
    url = 'https://www.reddit.com/r/travel/comments/sm49hc/i_fell_for_a_tourist_scam_and_feel_really_stupid/'

    reddit = get_instance()

    result = parse_travel_scam_with_url(reddit, url)

    print(result)

    end = time.time()

    print(f"테스트 실행 시간: {end - start:.2f} 초")
