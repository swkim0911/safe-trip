from repository.reddit_repository import RedditRepository
from adapters import mongo_client
import os, tiktoken

from utils.prompt_utils import get_parsing_system_content, generate_parsing_prompt

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
ENC = tiktoken.encoding_for_model(MODEL)

def estimate_parsing_cost():

    reddit_repository = RedditRepository(mongo_client.get_raw_collection(), mongo_client.get_parsed_collection(),
                                         mongo_client.get_batch_job_collection())

    documents = reddit_repository.find_raw_documents({"classification.is_travel_scam": True})

    input_total_tokens = 0
    output_total_tokens = 0

    for document in documents:
        body = document['body']
        system_content = get_parsing_system_content()
        prompt = generate_parsing_prompt(body)
        input_total_tokens += len(ENC.encode(system_content)) + len(ENC.encode(prompt))
        output_total_tokens += 800 # Json 배열 형태의 응답의 평균 토큰수


    input_cost = input_total_tokens / 1_000_000 * 0.15
    output_cost = output_total_tokens / 1_000_000 * 0.60
    total_cost = input_cost + output_cost

    print(f"input tokens: {input_total_tokens}, cost: ${input_cost:.4f}")
    print(f"output tokens: {output_total_tokens}, cost: ${output_cost:.4f}")
    print(f"total cost: ${total_cost:.4f}")

if __name__ == "__main__":
    estimate_parsing_cost()
