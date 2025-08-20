from reddit_client import get_instance
import travel_scam_crawler

if __name__ == "__main__":
    reddit = get_instance()
    travel_scam_crawler.crawl_travel_scam(reddit, 1)
