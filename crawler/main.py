from reddit_client import get_instance
import metadata_crawler

if __name__ == "__main__":
    reddit = get_instance()
    metadata_crawler.crawl_subreddit_metadata(reddit)
