import tiktoken
import time
from adapters.reddit_client import get_instance
import praw
import os

keywords = ["travel scam"]
enc = tiktoken.encoding_for_model(os.getenv("OPENAI_MODEL", "gpt-4o-mini"))


def crawl_subreddit_metadata(reddit):

    post_token = 0
    comments_token = 0

    post_cnt = 0
    comments_cnt = 0

    subreddit = reddit.subreddit('travel')

    start = time.time()
    last_log_time = start
    idx = 0

    for post in subreddit.search(' OR '.join(keywords), sort="relevance", time_filter="all", limit=500):
        post_cnt += 1

        # 게시글 길이(제목 + 본문)
        post_token += len(enc.encode(post.title)) + \
            len(enc.encode(post.selftext or ""))

        post.comments.replace_more(limit=0)

        # depth=1 댓글만 선택 (top-level comments)
        for comment in post.comments:
            if comment.parent_id.startswith('t3_'):  # t3_는 post를 의미 (depth=1)
                comments_token += len(enc.encode(comment.body or ""))
                comments_cnt += 1

        # 로그
        now = time.time()
        if now - last_log_time >= 20:
            elapsed = now - start
            print(f"[{elapsed:.1f}초 경과] 현재 {idx+1}번째 항목 처리 중")
            print(f"중간 post 개수: {post_cnt}")
            last_log_time = now

        idx += 1

    print(f"총 게시글 개수: {post_cnt}")
    print(f"총 게시글 토큰 개수: {post_token}")

    print(f"총 depth=1 댓글 개수: {comments_cnt}")
    print(f"총 depth=1 댓글 토큰 개수: {comments_token}")

    print(f"총 토큰 개수: {post_token + comments_token}")

    end = time.time()
    print(f"응답 시간: {end - start:.3f}초")


if __name__ == "__main__":

    reddit = get_instance()
    crawl_subreddit_metadata(reddit)
