import logging
import os
from functools import lru_cache

from openai import OpenAI

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

    input_file = client.files.create(
        file=open(filename, "rb"),
        purpose="batch"
    )
    input_file_id = input_file.id

    response = client.batches.create(
        input_file_id=input_file_id,
        endpoint="/v1/responses",
        completion_window="24h",
    )
    batch_id = response.id

    return {"input_file_id": input_file_id, "batch_id": batch_id}

def get_completed_batch_result(batch_id: str):
    """배치 결과를 조회한다.

    Returns:
        tuple: (content, status). 미완료 시 content는 None.
    """
    client = get_openai_client()

    batch = client.batches.retrieve(batch_id)
    status = batch.status

    if status == "completed":
        output_file_id = batch.output_file_id
        file_obj = client.files.content(output_file_id)

        logger.info("Batch %s completed. Output file_id=%s", batch_id, output_file_id)
        content = file_obj.content.decode("utf-8")
        return content, status

    elif status in ["failed", "expired", "cancelled"]:
        logger.error("Batch %s ended with status %s", batch_id, status)
    else:
        logger.info("Batch %s still not ready, status=%s", batch_id, status)

    return None, status
