import tiktoken
import time
from adapters.reddit_client import get_instance
import praw
import os

enc = tiktoken.encoding_for_model(os.getenv("OPENAI_MODEL", "gpt-4o-mini"))


def crawl_submission_metadata(reddit, url: str):
    post_token = 0
    comments_token = 0

    post_cnt = 0
    comments_cnt = 0

    start = time.time()

    # 특정 URL의 글 가져오기
    submission = reddit.submission(url=url)
    post_cnt += 1

    # 게시글 길이 (제목 + 본문)
    post_token += len(enc.encode(submission.title)) + \
        len(enc.encode(submission.selftext or ""))

    # 댓글 전개
    submission.comments.replace_more(limit=None)

    # depth=1 댓글만
    for comment in submission.comments.list():
        if comment.parent_id.startswith("t3_"):
            comments_token += len(enc.encode(comment.body or ""))
            comments_cnt += 1

    print(f"게시글 URL: {url}")
    print(f"게시글 토큰 개수: {post_token}")
    print(f"댓글 개수 (depth=1): {comments_cnt}")
    print(f"댓글 토큰 개수 (depth=1): {comments_token}")
    print(f"총 토큰 개수: {post_token + comments_token}")

    end = time.time()
    print(f"응답 시간: {end - start:.3f}초")


if __name__ == "__main__":
    reddit = get_instance()

    crawl_submission_metadata(
        reddit,
        url="https://www.reddit.com/r/travel/comments/1clcqna/whats_the_worst_scam_youve_fallen_for_while/"
    )
