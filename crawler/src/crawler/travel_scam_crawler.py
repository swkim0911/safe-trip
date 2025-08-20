import time
import praw
import travel_scam_classifier
import travel_scam_extractor
from datetime import datetime

KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


def crawl_travel_scam(reddit: praw.Reddit, limit: int) -> None:

    subreddit = reddit.subreddit('travel')

    for post in subreddit.search(' OR '.join(KEYWORDS), sort="relevance", time_filter="all", limit=limit):
        post_body = clean_text(post.selftext)

        is_travel_scam = travel_scam_classifier.classify(post_body)
        if is_travel_scam:
            travel_scam_data = travel_scam_extractor.extract(post_body)
            print(travel_scam_data)
