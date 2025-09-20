import json
import logging
from pathlib import Path

from adapters.openai_client import call_openai_api, call_openai_api_with_batch, get_completed_batch_result
from utils import prompt_utils, batch_utils


class TravelScamClassifier:
    
    def __init__(self, reddit_repository):
        self.reddit_repository = reddit_repository
        self.BATCH_SIZE = 1000
        self.job_type = "classification"
        self.logger = logging.getLogger(__name__)
                
    '''
    text가 travel scam 관련 글이라면 1, 아니면 0을 반환한다
    '''
    def classify(self, text: str) -> bool:
        system_content = prompt_utils.get_classification_system_content()
        prompt = prompt_utils.generate_classification_prompt(text)
        output_text = call_openai_api(system_content, prompt, temperature=0.0)

        if not output_text:  # None, "" 방어
            return False

        return self.__extract_label_from_output(output_text)

    """
    raw data를 리스트로 받아서 비동기 배치 요청을 하고 관련 메타데이터를 db에 저장
    """
    def submit_classification_batch(self, batch_docs):
        # 1. list -> jsonl 파일
        filename = batch_utils.write_jsonl(batch_docs, self.job_type)

        # 2. jsonl 파일을 openai api에 요청
        batch_metadata = call_openai_api_with_batch(filename)
        batch_metadata["job_type"] = self.job_type
        self.logger.info("OpenAI API batch 요청 완료 (batch_id=%s)", batch_metadata["batch_id"])

        Path(filename).unlink(missing_ok=True)
        self.logger.info("jsonl 파일 삭제 (filename=%s)", filename)

        return batch_metadata

    '''
    batch 요청 결과(24시간 후)를 parsed_collection에 반영
    '''
    def process_batch_results(self):
        # 1. 몽고에서 batch_id 일어오기
        batch_jobs = self.reddit_repository.find_batch_job_documents({"job_type":self.job_type}) # batch_id,

        for batch_job in batch_jobs:
            # 2. batch_id로부터 content(JSONL 결과) 읽어오기
            batch_id = batch_job["batch_id"]
            content = get_completed_batch_result(batch_id)
            if not content:
                continue  # 혹시 실패했거나 아직 결과가 없으면 스킵

            # 3. 결과 가공
            classification_results = []
            for line in content.splitlines():
                if not line.strip():
                    continue

                record = json.loads(line)

                reddit_id = record["custom_id"]
                text = record["response"]["body"]["output"][0]["content"][0]["text"]

                is_travel_scam = self.__extract_label_from_output(text)

                classification_results.append({"reddit_id": reddit_id,"is_travel_scam": is_travel_scam})

                # BATCH_SIZE 단위로 저장
                if len(classification_results) >= self.BATCH_SIZE:
                    self.reddit_repository.flush_classification_results(classification_results)

            # 남은 classification_results 처리
            if classification_results:
                self.reddit_repository.flush_classification_results(classification_results)


    '''
    안전장치 -> 숫자 이외가 섞여오면 첫 번째 '1' 또는 '0'만 취함
    '''
    def __extract_label_from_output(self, output_text):
        if "1" in output_text and "0" in output_text:
            return output_text.index("1") < output_text.index("0")
        if "1" in output_text:
            return True
        if "0" in output_text:
            return False
        return False

    