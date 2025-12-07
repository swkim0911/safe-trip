import os

import praw
from dotenv import load_dotenv

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
