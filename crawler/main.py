from reddit_client import get_instance
import metadata_crawler
from prompt import prompt_manager


if __name__ == "__main__":
    reddit = get_instance()
    result = prompt_manager.generate_classification_prompt("hello")
