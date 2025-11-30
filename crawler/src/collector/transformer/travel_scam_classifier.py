import json
import logging
from pathlib import Path

from adapters.openai_client import call_openai_api, call_openai_api_with_batch, get_completed_batch_result
from utils import prompt_utils, batch_utils


class TravelScamClassifier:
    """여행 사기 여부를 분류하는 클래스"""
    
    """
    Args:
        reddit_repository: Reddit 데이터 저장소
        config: ETL 설정 객체
    """
    def __init__(self, reddit_repository, etl_config):
        self.reddit_repository = reddit_repository
        self.etl_config = etl_config
        self.job_type = "classification"
        self.logger = logging.getLogger(__name__)
                
    '''
    text가 travel scam 관련 글이라면 True, 아니면 False를 반환한다
    '''
    def classify(self, text: str) -> bool:
        system_content = prompt_utils.get_classification_system_content()
        prompt = prompt_utils.generate_classification_prompt(text)
        output_text = call_openai_api(system_content, prompt, temperature=0.0)

        if not output_text:  # None, "" 방어
            return False

        return self.extract_label_from_output(output_text)
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
    batch 요청 결과를 확인하고 완료된 배치를 처리
    
    Returns:
        int: 처리된 배치 수
    '''
    def process_batch_results(self):
        # 1. 미처리 배치만 조회
        batch_jobs = self.reddit_repository.find_unprocessed_batches(self.job_type)
        
        processed_count = 0

        for batch_job in batch_jobs:
            batch_id = batch_job["batch_id"]
            
            # 2. batch_id로부터 결과 및 상태 확인
            content, status = get_completed_batch_result(batch_id)
            
            # 3. 배치 상태별 처리
            if status == "completed" and content:
                # 결과 가공 및 저장
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
                    if len(classification_results) >= self.etl_config.BATCH_SIZE:
                        self.reddit_repository.flush_classification_results(classification_results)

                # 남은 classification_results 처리
                if classification_results:
                    self.reddit_repository.flush_classification_results(classification_results)
                
                # 처리 완료 표시
                self.reddit_repository.mark_batch_as_processed(batch_id)
                processed_count += 1
                self.logger.info(f"✅ Batch {batch_id} 처리 완료")
                
            elif status in ["failed", "expired", "cancelled"]:
                # 실패한 배치는 실패로 표시하고 건너뜀
                self.reddit_repository.mark_batch_as_failed(batch_id, status)
                self.logger.warning(f"⚠️ Batch {batch_id} {status} 상태로 건너뜀")
                
            else:
                # 아직 진행 중인 배치(in_process)는 로그만 남김
                self.logger.debug(f"⏳ Batch {batch_id} 아직 {status} 상태")
        
        return processed_count


    '''
    output_text의 결과를 bool 타입으로 치환한다
    숫자 이외가 섞여오면 첫 번째 '1' 또는 '0'만 취함 (안전장치)
    '''
    @staticmethod
    def extract_label_from_output(output_text):
        if "1" in output_text and "0" in output_text:
            return output_text.index("1") < output_text.index("0")
        if "1" in output_text:
            return True
        if "0" in output_text:
            return False
        return False

    