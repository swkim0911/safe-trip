import os
from dotenv import load_dotenv
from openai import OpenAI
from prompt import prompt_manager
import json
import re

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")


def safe_json_loads(text: str):
    # 1. 코드펜스 제거 (```json ... ```)
    m = re.search(
        r"```(?:json)?\s*([\s\S]*?)\s*```", text, flags=re.IGNORECASE)
    if m:
        text = m.group(1).strip()

    # 2. JSON 배열/객체만 추출
    starts = [i for i in (text.find('['), text.find('{')) if i != -1]
    if starts:
        start = min(starts)
        end_char = ']' if text[start] == '[' else '}'
        end = text.rfind(end_char)
        if end != -1:
            text = text[start:end+1].strip()

    # 3. 실제 파싱
    return json.loads(text)



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
        data = safe_json_loads(output_text)
    except json.JSONDecodeError as e:
        raise ValueError(f"JSON 디코딩 실패: {e}\n원본 응답: {output_text}")

    return data
