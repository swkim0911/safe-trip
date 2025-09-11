import os
from dotenv import load_dotenv
from openai import OpenAI
from functools import lru_cache

load_dotenv()

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
API_KEY = os.getenv("OPENAI_API_KEY")

@lru_cache(maxsize=1)
def get_openai_client() -> OpenAI:
    return OpenAI(api_key=API_KEY) # OpenAI 클라이언트를 캐싱(싱글톤)해서 재사용

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