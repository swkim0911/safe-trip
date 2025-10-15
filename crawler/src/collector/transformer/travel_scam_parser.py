import json
import logging
import re
from pathlib import Path
from typing import Any

from adapters.openai_client import call_openai_api, call_openai_api_with_batch
from utils import prompt_utils, batch_utils


class TravelScamParser:
    """여행 사기 관련 데이터를 파싱하는 클래스"""

    def __init__(self, config):
        """
        Args:
            config: ETL 설정 객체
        """
        self.config = config
        self.logger = logging.getLogger(__name__)
        self.job_type = "parsing"

    '''
    [jsons]: text -> [jsons]: json object 변환하는 함수
    '''
    def safe_json_loads(self, text: str):
        if not text:
            raise ValueError("LLM 응답이 비어 있음")

        # 1. 코드펜스 제거 (```json ... ```)
        m = re.search(
            r"```(?:json)?\s*([\s\S]*?)\s*```", text, flags=re.IGNORECASE
        )
        if m:
            text = m.group(1).strip()

        # 2. JSON 배열/객체만 추출
        starts = [i for i in (text.find("["), text.find("{")) if i != -1]
        if starts:
            start = min(starts)
            end_char = "]" if text[start] == "[" else "}"
            end = text.rfind(end_char)
            if end != -1:
                text = text[start:end + 1].strip()

        # 3. LLM 특유의 오류 방어 (trailing comma, True/False/None → JSON 표준)
        text = re.sub(r",\s*([}\]])", r"\1", text)  # 불법 쉼표 제거
        text = text.replace("True", "true").replace("False", "false").replace("None", "null")

        # 4. 실제 파싱
        try:
            return json.loads(text)
        except json.JSONDecodeError as e:
            self.logger.error(f"safe_json_loads 실패: {e}\n원본: {text[:200]}...", exc_info=True)
            raise

    '''
    분류된 데이터에서 필요한 데이터를 추출(parsing)해서 json array로 반환
    '''
    def parse(self, text: str) -> list[dict[str, Any]]:
        system_content = prompt_utils.get_parsing_system_content()
        prompt = prompt_utils.generate_parsing_prompt(text)
        output_text = call_openai_api(system_content, prompt, temperature=0.0)

        try:
            data = self.safe_json_loads(output_text)
        except Exception as e:
            raise ValueError(f"JSON 디코딩 실패: {e}\n원본 응답: {output_text}")

        return data

    def submit_parsing_batch(self, batch_docs: list[dict[str, str]]):
        # 1. list -> jsonl 파일
        filename = batch_utils.write_jsonl(batch_docs, self.job_type)

        # 2. jsonl 파일을 openai api에 요청
        batch_metadata = call_openai_api_with_batch(filename)
        batch_metadata["job_type"] = self.job_type
        self.logger.info("(parsing) OpenAI API batch 요청 완료 (batch_id=%s)", batch_metadata["batch_id"])

        Path(filename).unlink(missing_ok=True)
        self.logger.info("(parsing)jsonl 파일 삭제 (filename=%s)", filename)

        return batch_metadata

