from dotenv import load_dotenv
import os
import praw

load_dotenv()  # .env 파일 불러오기


def get_instance():
    reddit = praw.Reddit(
        client_id=os.getenv("CLIENT_ID"),
        client_secret=os.getenv("CLIENT_SECRET"),
        username=os.getenv("USERNAME"),
        password=os.getenv("PASSWORD"),
        user_agent=os.getenv('USER_AGENT')
    )

    return reddit


if __name__ == '__main__':
    reddit = get_instance()
    subreddit = reddit.subreddit("travel")

    for post in subreddit.search("scam", sort="relevance", time_filter="all", limit=1):
        print("제목:", post.title)
        print("작성자:", post.author)
        print("본문:", post.selftext)
        print("URL:", post.url)
        print("생성 시각:", post.created_utc)
        print("댓글 수:", post.num_comments)
        print("소속 서브레딧:", post.subreddit.display_name)
