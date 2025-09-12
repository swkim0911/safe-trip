from adapters.openai_client import call_openai_api, call_openai_api_with_batch
from utils import prompt_utils, batch_utils
import json, os

class TravelScamClassifier:
    
    def __init__(self, reddit_repository):
        self.reddit_repository = reddit_repository
        self.BATCH_SIZE = 1000
                
    # text가 travel scam 관련 글이라면 1, 아니면 0을 반환한다
    def classify(self, text: str) -> bool:
        system_content = prompt_utils.get_classification_system_content()
        prompt = prompt_utils.generate_classification_prompt(text)
        output_text = call_openai_api(system_content, prompt, temperature=0.0)

        if not output_text:  # None, "" 방어
            return False

        # 안전장치 -> 숫자 이외가 섞여오면 첫 번째 '1' 또는 '0'만 취함
        if "1" in output_text and "0" in output_text:
            return output_text.index("1") < output_text.index("0")
        if "1" in output_text:
            return True
        if "0" in output_text:
            return False
        return False

    """
    raw data를 리스트로 받아서 비동기 배치 요청을 하고 관련 메타데이터를 db에 저장
    """
    def submit_classification_batch(self, jsons):
        # 1. list -> jsonl 파일
        filename = batch_utils.write_jsonl(jsons)

        # 2. jsonl 파일을 openai api에 요청
        batch_metadata = call_openai_api_with_batch(filename)

        # 3. batch_id 등 메타데이터를 MongoDB에 저장
        self.reddit_repository.save_batch_job(batch_metadata)

    # 24시간 후, batch요청 결과를 DB에 반영
    def process_batch_results:
        # 1. 몽고에서 batch_id 일어오기

    #     # 2. batch_id로 부터 content 읽어오기
    #     content = call_openai_api_for_content(batch_id)
    #
    #
    #     ## 결과 가공
    #     items = []
    #
    #     for line in content.splitlines():
    #         if not line.strip():
    #             continue
    #
    #         record = json.loads(line)
    #
    #         reddit_id = record["custom_id"]
    #         text = record["response"]["body"]["output"][0]["content"][0]["text"]
    #
    #         is_travel_scam = text.strip() == "1"
    #         items.append({"reddit_id": reddit_id, "is_travel_scam": is_travel_scam})
    #         if len(items) >= self.BATCH_SIZE:
    #             self.reddit_repository.flush_classification_results(items)
    #
    #     self.reddit_repository.flush_classification_results(items)
    #
    #     # jsonl 파일 삭제
    #     # try:
    #     #     os.remove(filename)
    #     #     print(f"Deleted temp file: {filename}")
    #     # except OSError as e:
    #     #     print(f"[WARN] Failed to delete {filename}: {e}")
    #


    