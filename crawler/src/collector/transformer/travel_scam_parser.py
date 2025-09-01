import os
from dotenv import load_dotenv
from openai import OpenAI
from prompt import prompt_manager
import json

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")


# 분류된 데이터에서 필요한 데이터를 추출해서 json array를 반환한다
def parse(text: str) -> list[dict[str, any]]:
    prompt = prompt_manager.generate_parsing_prompt(text)

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
