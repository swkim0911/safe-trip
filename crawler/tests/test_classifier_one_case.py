from collector.classfier import travel_scam_classifier
from adapters.reddit_client import get_instance
import time
import praw


KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


def crawl_travel_scam_with_url(reddit: praw.Reddit, url: str) -> int:

    submission = reddit.submission(url=url)  # url로 직접 게시글 가져오기
    post_body = clean_text(submission.selftext)

    is_travel_scam = travel_scam_classifier.classify(post_body)
    return is_travel_scam


if __name__ == "__main__":
    start = time.time()
    url = 'http://reddit.com/r/travel/comments/1jheekn/some_advice_from_an_italian_living_in_italy_to/'

    reddit = get_instance()

    result = crawl_travel_scam_with_url(reddit, url)

    print(result)

    end = time.time()

    print(f"테스트 실행 시간: {end - start:.2f} 초")
