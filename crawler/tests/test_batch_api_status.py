from adapters.openai_client import get_openai_client
from repository.reddit_repository import RedditRepository
from adapters import mongo_client



if __name__ == "__main__":

    client = get_openai_client()

    reddit_repository = RedditRepository(mongo_client.get_raw_collection(), mongo_client.get_parsed_collection(),
                                         mongo_client.get_batch_job_collection())

    documents = reddit_repository.find_batch_job_documents({})

    for document in documents:
        batch_id = document['batch_id']

        batch = client.batches.retrieve(batch_id)

        print(batch)

