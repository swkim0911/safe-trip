import os
from dotenv import load_dotenv
from openai import OpenAI
from prompt import prompt_manager
import json

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")


def analyze(text: str):
    prompt = prompt_manager.generate_extraction_prompt(text)

    response = client.responses.create(
        model=MODEL,
        input=prompt,
        temperature=0.0
    )

    # 결과물의 순수 텍스트
    output_text = response.output_text.strip()

    try:
        data = json.loads(output_text)
    except json.JSONDecodeError as e:
        raise ValueError(f"JSON 디코딩 실패: {e}\n원본 응답: {output_text}")

    return data
