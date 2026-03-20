import json
import logging
from pathlib import Path

from adapters.openai_client import call_openai_api, call_openai_api_with_batch, get_completed_batch_result
from utils import prompt_utils, batch_utils
from utils.batch_utils import submit_batches_in_chunks
from utils.token_utils import estimate_classification_request_tokens


class TravelScamClassifier:
    """여행 사기 여부를 분류하는 클래스"""

    def __init__(self, reddit_repository, etl_config):
        self.reddit_repository = reddit_repository
        self.etl_config = etl_config
        self.job_type = "classification"
        self.logger = logging.getLogger(__name__)

    def submit_documents_in_batch(self, limit: int | None = None, capacity_fn=None):
        """미분류 또는 버전 불일치(버전이 업데이트된 겨우) 문서를 배치 제출한다.

        Args:
            limit: 최대 처리 문서 수
            capacity_fn: 배치 제출 전 슬롯 확보를 대기하는 함수 (OpenAI 동시 배치 한도 초과 방지용)
        """
        current_version = self.etl_config.CLASSIFICATION_PROMPT_VERSION
        query = {
            "$or": [
                {"classification": {"$exists": False}},
                {"classification.classification_prompt_version": {"$ne": current_version}}
            ]
        }
        docs = self.reddit_repository.find_raw_documents(query, limit=limit)
        self.logger.info("분류 대상 조회 완료 (현재 버전: %s)", current_version)

        submit_batches_in_chunks(
            docs=docs,
            token_estimator=estimate_classification_request_tokens,
            token_limit=self.etl_config.TOKEN_LIMIT,
            submit_fn=self.submit_classification_batch,
            save_batch_fn=self.reddit_repository.save_batch_job,
            batch_label="classification",
            logger=self.logger,
            capacity_fn=capacity_fn,
        )

    def get_unprocessed_batch_count(self) -> int:
        return len(list(self.reddit_repository.find_unprocessed_batches(self.job_type)))

    def classify(self, text: str) -> bool:
        """단일 텍스트가 travel scam 관련인지 분류한다."""
        system_content = prompt_utils.get_classification_system_content()
        prompt = prompt_utils.generate_classification_prompt(text)
        output_text = call_openai_api(system_content, prompt, temperature=0.0)

        if not output_text:  # None, "" 방어
            return False

        return self.extract_label_from_output(output_text)

    def submit_classification_batch(self, batch_docs) -> dict:
        """배치 문서를 OpenAI Batch API에 제출하고 메타데이터를 반환한다."""
        filename = batch_utils.write_jsonl(batch_docs, self.job_type)

        batch_metadata = call_openai_api_with_batch(filename)
        batch_metadata["job_type"] = self.job_type
        self.logger.info("OpenAI API batch 요청 완료 (batch_id=%s)", batch_metadata["batch_id"])

        Path(filename).unlink(missing_ok=True)
        self.logger.info("jsonl 파일 삭제 (filename=%s)", filename)

        return batch_metadata

    def process_batch_results(self) -> int:
        """완료된 배치 결과를 처리하고 분류 결과를 저장한다.

        Returns:
            int: 처리된 배치 수
        """
        batch_jobs = self.reddit_repository.find_unprocessed_batches(self.job_type)
        processed_count = 0

        for batch_job in batch_jobs:
            batch_id = batch_job["batch_id"]
            content, status = get_completed_batch_result(batch_id)

            if status == "completed" and content:
                classification_results = []
                for line in content.splitlines():
                    if not line.strip():
                        continue

                    record = json.loads(line)
                    reddit_id = record["custom_id"]
                    text = record["response"]["body"]["output"][0]["content"][0]["text"]
                    is_travel_scam = self.extract_label_from_output(text)
                    classification_results.append({"reddit_id": reddit_id, "is_travel_scam": is_travel_scam})

                    if len(classification_results) >= self.etl_config.BATCH_SIZE:
                        self.reddit_repository.flush_classification_results(classification_results)

                if classification_results:
                    self.reddit_repository.flush_classification_results(classification_results)

                self.reddit_repository.mark_batch_as_processed(batch_id)
                processed_count += 1
                self.logger.info("Batch %s 처리 완료", batch_id)

            elif status in ["failed", "expired", "cancelled"]:
                self.reddit_repository.mark_batch_as_failed(batch_id, status)
                self.logger.warning("Batch %s %s 상태로 건너뜀", batch_id, status)

            else:
                self.logger.debug("Batch %s 아직 %s 상태", batch_id, status)

        return processed_count

    @staticmethod
    def extract_label_from_output(output_text) -> bool:
        """LLM 출력에서 분류 레이블을 추출한다.

        숫자 외 문자가 섞인 경우 첫 번째 '1' 또는 '0'만 사용한다.
        """
        if "1" in output_text and "0" in output_text:
            return output_text.index("1") < output_text.index("0")
        if "1" in output_text:
            return True
        if "0" in output_text:
            return False
        return False
