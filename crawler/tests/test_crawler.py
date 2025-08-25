from src.adapters.reddit_client import get_instance
from dotenv import load_dotenv
from datetime import datetime
import time
import praw


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
            # todo: db에 저장

        post.comments.replace_more(limit=0)

        # depth=1 댓글만 선택 (top-level comments)
        for i, comment in enumerate(post.comments):

            comment_body = clean_text(comment.body)
            is_travel_scam_cm = travel_scam_classifier.classify(comment_body)
            if is_travl_scam_cm:
                travel_scam_data = travel_scam_extractor.extract(commnet_body)
                # todo: db에 저장
