import json
import logging
import re
from datetime import datetime, UTC
from pathlib import Path
from typing import Any

from adapters.openai_client import call_openai_api, call_openai_api_with_batch, get_completed_batch_result
from utils import prompt_utils, batch_utils
from utils.batch_utils import submit_batches_in_chunks
from utils.token_utils import estimate_parsing_request_tokens


class TravelScamParser:
    """여행 사기 관련 데이터를 파싱하는 클래스"""

    def __init__(self, config, reddit_repository):
        self.config = config
        self.reddit_repository = reddit_repository
        self.logger = logging.getLogger(__name__)
        self.job_type = "parsing"

    def submit_documents_in_batch(self, limit: int | None = None, capacity_fn=None):
        """분류된 travel scam 문서 중 미파싱 문서를 배치 제출한다.

        Args:
            limit: 최대 처리 문서 수
            capacity_fn: 배치 제출 전 슬롯 확보를 대기하는 함수 (OpenAI 동시 배치 한도 초과 방지용)
        """
        all_travel_scam_docs = self.reddit_repository.find_raw_documents(
            {"classification.is_travel_scam": True}, limit=limit
        )
        parsed_ids = {
            doc["reddit_id"]
            for doc in self.reddit_repository.find_parsed_documents({}, projection={"reddit_id": 1})
        }
        unparsed_docs = (doc for doc in all_travel_scam_docs if doc["reddit_id"] not in parsed_ids)

        submit_batches_in_chunks(
            docs=unparsed_docs,
            token_estimator=estimate_parsing_request_tokens,
            token_limit=self.config.TOKEN_LIMIT,
            submit_fn=self.submit_parsing_batch,
            save_batch_fn=self.reddit_repository.save_batch_job,
            batch_label="parsing",
            logger=self.logger,
            capacity_fn=capacity_fn,
        )

    def process_batch_results(self) -> int:
        """완료된 배치 결과를 처리해 parsed_documents에 저장한다.

        Returns:
            int: 처리된 배치 수
        """
        batch_jobs = self.reddit_repository.find_unprocessed_batches(self.job_type)
        processed_count = 0

        for batch_job in batch_jobs:
            batch_id = batch_job["batch_id"]
            content, status = get_completed_batch_result(batch_id)

            if status == "completed" and content:
                parsing_results = []
                for line in content.splitlines():
                    if not line.strip():
                        continue

                    record = json.loads(line)
                    reddit_id = record["custom_id"]
                    # Responses API가 메시지를 여러 개 emit하는 경우(중간에 멈췄다 다시 출력 등)
                    # 첫 번째는 불완전할 수 있으므로 마지막 메시지를 최종 응답으로 사용한다.
                    text = record["response"]["body"]["output"][-1]["content"][0]["text"]

                    try:
                        parsed_items = self.safe_json_loads(text)
                    except (json.JSONDecodeError, ValueError) as e:
                        # 한 게시글의 응답이 깨졌어도 전체 파이프라인을 죽이지 않는다.
                        # 진단용 컨텍스트(reddit_id / batch_id / head / tail)를 남기고
                        # 빈 결과로 취급해 호출 다음 분기에서 empty_result 마킹으로 흘려보낸다.
                        self.logger.error(
                            "Parse 응답 처리 실패 — empty_result로 마킹하고 계속 진행\n"
                            "  reddit_id: %s\n"
                            "  batch_id: %s\n"
                            "  text_len: %d\n"
                            "  text_head(300): %r\n"
                            "  text_tail(300): %r\n"
                            "  error: %s",
                            reddit_id, batch_id, len(text), text[:300], text[-300:], e,
                        )
                        parsed_items = []
                    raw_doc = self.reddit_repository.find_raw_document_by_reddit_id(reddit_id)
                    now = datetime.now(UTC)

                    if not parsed_items:
                        # OpenAI가 빈 배열 반환 → 재제출 방지용 마커 저장
                        self.logger.info("(parsing) %s 빈 결과 — empty_result 마킹", reddit_id)
                        parsing_results.append({
                            "reddit_id": reddit_id,
                            "source": "reddit",
                            "url": raw_doc.get("url"),
                            "author": raw_doc.get("author"),
                            "posted_at": raw_doc.get("posted_at"),
                            "created_at": now,
                            "updated_at": now,
                            "empty_result": True,
                            "parsing": {
                                "model_name": self.config.MODEL_NAME,
                                "parsing_prompt_version": self.config.PARSING_PROMPT_VERSION,
                                "parsed_at": now,
                                "location_enriched": False,
                            },
                        })
                    else:
                        for item in parsed_items:
                            parsing_results.append({
                                "reddit_id": reddit_id,
                                "source": "reddit",
                                "url": raw_doc.get("url"),
                                "author": raw_doc.get("author"),
                                "posted_at": raw_doc.get("posted_at"),
                                "created_at": now,
                                "updated_at": now,
                                "title": item.get("title"),
                                "action": item.get("action"),
                                "context": item.get("context"),
                                "summary": item.get("summary"),
                                "country_name": item.get("country"),
                                "state_name": item.get("state"),
                                "city_name": item.get("city"),
                                "country_id": None,
                                "state_id": None,
                                "city_id": None,
                                "parsing": {
                                    "model_name": self.config.MODEL_NAME,
                                    "parsing_prompt_version": self.config.PARSING_PROMPT_VERSION,
                                    "parsed_at": now,
                                    "location_enriched": False,
                                },
                            })

                    if len(parsing_results) >= self.config.BATCH_SIZE:
                        self.reddit_repository.flush_parsing_results(parsing_results)

                if parsing_results:
                    self.reddit_repository.flush_parsing_results(parsing_results)

                self.reddit_repository.mark_batch_as_processed(batch_id)
                processed_count += 1
                self.logger.info("Batch %s 처리 완료", batch_id)

            elif status in ["failed", "expired", "cancelled"]:
                self.reddit_repository.mark_batch_as_failed(batch_id, status)
                self.logger.warning("Batch %s %s 상태로 건너뜀", batch_id, status)

            else:
                self.logger.debug("Batch %s 아직 %s 상태", batch_id, status)

        return processed_count

    def get_unprocessed_batch_count(self) -> int:
        return len(list(self.reddit_repository.find_unprocessed_batches(self.job_type)))

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
            # 호출자가 reddit_id/batch_id까지 추가 로깅하도록 둠 — 여기선 처리 후 head/tail만 남긴다.
            self.logger.error(
                "safe_json_loads 실패: %s\n  processed_text_len: %d\n  head: %r\n  tail: %r",
                e, len(text), text[:300], text[-300:],
            )
            raise

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

