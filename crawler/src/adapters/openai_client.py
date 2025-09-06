import os
from dotenv import load_dotenv
from openai import OpenAI
from functools import lru_cache

load_dotenv()

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
API_KEY = os.getenv("OPENAI_API_KEY")

@lru_cache(maxsize=1)
def get_openai_client() -> OpenAI:
    """OpenAI 클라이언트를 캐싱해서 재사용"""
    return OpenAI(api_key=API_KEY)


def call_openai(prompt: str, temperature: float = 0.0) -> str:
    """OpenAI API 호출 후 텍스트 응답 반환"""
    client = get_openai_client()
    response = client.responses.create(
        model=MODEL,
        input=prompt,
        temperature=temperature,
    )
    return (response.output_text or "").strip()
