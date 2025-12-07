import logging
import os
from functools import lru_cache

from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
API_KEY = os.getenv("OPENAI_API_KEY")

logger = logging.getLogger(__name__)

def get_openai_client() -> OpenAI:
    return OpenAI(api_key=API_KEY)

def call_openai_api(system_content:str, prompt: str, temperature: float = 0.0) -> str:
    
    client = get_openai_client()
    response = client.responses.create(
        model=MODEL,
        input=[
            {"role": "system", "content": system_content},
            {"role": "user", "content": prompt},
        ],
        temperature=temperature,
    )
    return (response.output_text or "").strip()

def call_openai_api_with_batch(filename: str):
    client = get_openai_client()
    
    # 1. 파일 업로드
    input_file = client.files.create(
        file=open(filename, "rb"),
        purpose="batch"
    )
    input_file_id = input_file.id

    # 2. 배치 생성
    response = client.batches.create(
        input_file_id=input_file_id,
        endpoint="/v1/responses",
        completion_window="24h",
    )
    batch_id = response.id

    return {"input_file_id": input_file_id, "batch_id": batch_id}

def get_completed_batch_result(batch_id: str):
    """
        Returns (content, status). content is None if not completed.
    """
    client = get_openai_client()

    batch = client.batches.retrieve(batch_id)
    status = batch.status
    
    if status == "completed":
        output_file_id = batch.output_file_id
        file_obj = client.files.content(output_file_id)

        # JSONL 콘텐츠 반환
        logger.info(f"✅ Batch {batch_id} completed. Output file_id={output_file_id}")
        content = file_obj.content.decode("utf-8")
        return content, status

    elif status in ["failed", "expired", "cancelled"]:
        logger.error(f"❌ Batch {batch_id} ended with status {status}")
    else:
        logger.info(f"⏳ Batch {batch_id} still not ready, status={status}")

    return None, status
