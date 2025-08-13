from dotenv import load_dotenv
import os
import praw

keywords = ["travel scam"]

cnt = 0

if __name__ == '__main__':
    reddit = get_instance()
    subreddit = reddit.subreddit("travel")

    for post in subreddit.search(" OR ".join(keywords), sort="relevance", time_filter="all", limit=500):
        if post.title == 'What’s the worst scam you’ve fallen for while travelling?':
            print(post.title)
