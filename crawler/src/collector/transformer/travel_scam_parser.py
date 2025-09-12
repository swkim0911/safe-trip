from adapters.openai_client import call_openai_api
from utils import prompt_utils
from typing import Any
import json, re

class TravelScamParser:

    def __safe_json_loads(self, text: str):
        
        if not text:
            raise ValueError("LLM 응답이 비어 있음")
        
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
    def parse(self, text: str) -> list[dict[str, Any]]:
        system_content = prompt_utils.get_parsing_system_content()
        prompt = prompt_utils.generate_parsing_prompt(text)
        output_text = call_openai_api(system_content, prompt, temperature=0.0)

        try:
            data = self.__safe_json_loads(output_text)
        except Exception as e:
            raise ValueError(f"JSON 디코딩 실패: {e}\n원본 응답: {output_text}")

        return data
