import time
import praw
from collector import travel_scam_classifier
from collector import travel_scam_extractor
from datetime import datetime

KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


'''
[
  {
      "external_id": str,
      "source": str(reddit),
      "url": str,
      "author": str,
      "title": str,
      "scam_type": str,
      "country": str,
      "state": str,
      "city": str,
      "summary": str,
      "posted_at":
  }
]
'''


def crawl_travel_scams(reddit: praw.Reddit, limit: int) -> None:
    collected_results = []

    subreddit = reddit.subreddit('travel')

    for post in subreddit.search(' OR '.join(KEYWORDS), sort='relevance', time_filter='all', limit=limit):
        post_text = clean_text(post.selftext)
        is_post_scam = travel_scam_classifier.classify(post_text)

        if is_post_scam:

            extracted_post_scams = travel_scam_extractor.extract(post_text)

            for scam_record in extracted_post_scams:
                collected_results.append({
                    "external_id": post.id,
                    "source": "reddit",
                    "url": f"https://reddit.com{post.permalink}",
                    "author": str(post.author) if post.author else None,
                    "title": scam_record.get("title"),
                    "scam_type": scam_record.get("scam_type"),
                    "country": scam_record.get("country"),
                    "state": scam_record.get("state"),
                    "city": scam_record.get("city"),
                    "summary": scam_record.get("summary"),
                    "posted_at": datetime.utcfromtimestamp(post.created_utc).isoformat()
                })

        post.comments.replace_more(limit=0)

        # depth=1 댓글만 선택 (top-level comments)
        for comment in post.comments:
            comment_text = clean_text(comment.body)
            is_comment_scam = travel_scam_classifier.classify(comment_text)

            if is_comment_scam:
                extracted_comment_scams = travel_scam_extractor.extract(
                    comment_text)
                for scam_record in extracted_comment_scams:
                    collected_results.append({
                        "external_id": comment.id,
                        "source": "reddit",
                        "url": f"https://reddit.com{comment.permalink}",
                        "author": str(comment.author) if comment.author else None,
                        "title": scam_record.get("title"),
                        "scam_type": scam_record.get("scam_type"),
                        "country": scam_record.get("country"),
                        "state": scam_record.get("state"),
                        "city": scam_record.get("city"),
                        "summary": scam_record.get("summary"),
                        "posted_at": datetime.utcfromtimestamp(comment.created_utc).isoformat()
                    })
