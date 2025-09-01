from pymongo import UpdateOne
from pymongo import MongoClient
from datetime import datetime
from adapters.mongo_client import get_raw_collection
import praw

KEYWORDS = ["travel scam"]
BATCH_SIZE = 100

'''
reddit에 있는 travel scam raw data -> mongoDB에 json 형태로 저장
'''
def extract(reddit: praw.Reddit, limit: int):
    raw_collection = get_raw_collection()
    subreddit = reddit.subreddit("travel")
    operations = []

    def flush_ops(ops):
        # operations를 bulk_write 실행 후 비움
        if ops:
            result = raw_collection.bulk_write(ops, ordered=False)
            print(f"Matched: {result.matched_count}, "
                  f"Modified: {result.modified_count}, "
                  f"Upserted: {len(result.upserted_ids)}")
            ops.clear()

    for post in subreddit.search(' OR '.join(KEYWORDS), sort="relevance", time_filter="all", limit=limit):

        post_doc = {
            "id": f"t3_{post.id}",
            "source": "reddit",
            "author": str(post.author) if post.author else None,
            "body": post.selftext,
            "url": f"https://reddit.com{post.permalink}",
            "created_at": datetime.utcfromtimestamp(post.created_utc),
            "type": "post"
        }
        operations.append(
            UpdateOne(
                {"id": f"t3_{post.id}"},
                {"$set": post_doc},
                upsert=True
            )
        )

        post.comments.replace_more(limit=0)
        for comment in post.comments:
            if comment.parent_id.startswith("t3_"):
                comment_doc = {
                    "id": f"t1_{comment.id}",
                    "source": "reddit",
                    "author": str(comment.author) if comment.author else None,
                    "body": comment.body,
                    "url": f"https://reddit.com{comment.permalink}",
                    "created_at": datetime.utcfromtimestamp(comment.created_utc),
                    "type": "comment"
                }
                operations.append(
                    UpdateOne(
                        {"id": f"t1_{comment.id}"},
                        {"$set": comment_doc},
                        upsert=True
                    )
                )

            if len(operations) >= BATCH_SIZE:
                flush_ops(operations)

    # 남은 operations 처리
    flush_ops(operations)
