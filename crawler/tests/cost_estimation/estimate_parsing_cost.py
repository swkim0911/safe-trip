import sys
from pathlib import Path

# 프로젝트 루트 추가
project_root = Path(__file__).parent.parent.parent / "src"
sys.path.insert(0, str(project_root))

import os
import tiktoken
import random

from repository.reddit_repository import RedditRepository
from adapters import mongo_client
from config.etl_config import ETLConfig
from utils.prompt_utils import get_parsing_system_content, generate_parsing_prompt

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
ENC = tiktoken.encoding_for_model(MODEL)

def estimate_parsing_cost():
    """
    is_travel_scam=true인 문서 중 하나를 랜덤하게 선택하여
    파싱 시 소모될 토큰 수와 비용을 추정합니다.
    """
    etl_config = ETLConfig()
    reddit_repository = RedditRepository(
        mongo_client.get_raw_collection(),
        mongo_client.get_parsed_collection(),
        mongo_client.get_batch_job_collection(),
        etl_config
    )

    # is_travel_scam=true인 문서들 조회
    query = {"classification.is_travel_scam": True}
    documents = list(reddit_repository.find_raw_documents(query))
    
    if not documents:
        print("No documents found with classification.is_travel_scam = true")
        return
    
    # 랜덤하게 하나 선택
    document = random.choice(documents)
    
    print(f"Total documents with is_travel_scam=true: {len(documents)}")
    print(f"Selected document reddit_id: {document.get('reddit_id', 'N/A')}")
    print(f"Document body length: {len(document['body'])} characters\n")
    print("-" * 80)
    print(f"Body preview:\n{document['body'][:200]}...")
    print("-" * 80)
    print()

    # 토큰 계산
    body = document['body']
    system_content = get_parsing_system_content()
    prompt = generate_parsing_prompt(body)
    
    system_tokens = len(ENC.encode(system_content))
    prompt_tokens = len(ENC.encode(prompt))
    input_tokens = system_tokens + prompt_tokens
    
    # v2 프롬프트는 더 상세한 출력 예상
    estimated_output_tokens = 250
    
    total_tokens = input_tokens + estimated_output_tokens

    # GPT-4o-mini 가격
    input_cost = input_tokens / 1_000_000 * 0.15
    output_cost = estimated_output_tokens / 1_000_000 * 0.60
    total_cost = input_cost + output_cost

    print("Token Breakdown:")
    print(f"  System content: {system_tokens:,} tokens")
    print(f"  Prompt (body):  {prompt_tokens:,} tokens")
    print(f"  Input total:    {input_tokens:,} tokens")
    print(f"  Output (est):   {estimated_output_tokens:,} tokens")
    print(f"  Total:          {total_tokens:,} tokens")
    print()
    print("Cost Estimation (GPT-4o-mini):")
    print(f"  Input cost:  ${input_cost:.6f}")
    print(f"  Output cost: ${output_cost:.6f}")
    print(f"  Total cost:  ${total_cost:.6f}")
    print()
    print(f"For {len(documents)} documents: ~${total_cost * len(documents):.4f}")

if __name__ == "__main__":
    estimate_parsing_cost()
