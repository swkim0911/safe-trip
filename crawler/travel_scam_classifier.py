import os
from dotenv import load_dotenv
from openai import OpenAI
from prompt import prompt_manager

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")


def classify(text: str) -> str:
    prompt = prompt_manager.generate_classification_prompt(text)

    response = client.responses.create(
        model=MODEL,
        input=prompt,
        temperature=0.0,  # 창의성 없이, 최대한 규칙에 충실하게 응답하도록 설정 -> 분류 작업에 적절
    )

    out = (response.output_text or "").strip()

    # 안전장치 -> 숫자 이외가 섞여오면 첫 번째 '1' 또는 '0'만 취함
    if "1" in out and "0" in out:
        return "1" if out.index("1") < out.index("0") else "0"
    if "1" in out:
        return "1"
    if "0" in out:
        return "0"
    return "0"
