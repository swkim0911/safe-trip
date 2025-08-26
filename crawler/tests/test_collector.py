from collector import travel_scam_extractor
from collector import travel_scam_classifier
from adapters.reddit_client import get_instance
import praw


KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


def crawl_travel_scam_with_url(reddit: praw.Reddit, url: str) -> None:

    submission = reddit.submission(url=url)  # url로 직접 게시글 가져오기
    post_body = clean_text(submission.selftext)

    is_travel_scam = travel_scam_classifier.classify(post_body)
    print(is_travel_scam)


if __name__ == "__main__":
    reddit = get_instance()
    url = 'https://www.reddit.com/r/travel/comments/13y9dqk/weird_new_travel_scam/'
    crawl_travel_scam_with_url(reddit, url)
