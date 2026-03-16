import logging
import time
from datetime import datetime
from pathlib import Path
import json, os

from utils import prompt_utils

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
BASE_DIR = Path(__file__).resolve().parent.parent.parent # /crawler

def get_batch_filename(folder: str = "data/batch_inputs") -> str:
    abs_folder = BASE_DIR / folder
    abs_folder.mkdir(parents=True, exist_ok=True) # 폴더 없으면 생성
    filename = f"batch_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}.jsonl"
    return str(Path(abs_folder) / filename)

"""
docs 배열을 받아 JSONL 파일로 변환한다.
jsons: [{"reddit_id": "xxx", "body": "..."}, ...]
return: 생성된 파일 경로
"""
def write_jsonl(jsons: list[dict[str, str]], job_type: str) -> str:

    system_content_map = {
        "classification": prompt_utils.get_classification_system_content,
        "parsing": prompt_utils.get_parsing_system_content,
    }
    prompt_map = {
        "classification": prompt_utils.generate_classification_prompt,
        "parsing": prompt_utils.generate_parsing_prompt,
    }

    if job_type not in system_content_map:
        raise ValueError(f"Unsupported job_type: {job_type}")

    system_content = system_content_map[job_type]()
    filename = get_batch_filename()
    
    with open(filename, "w", encoding="utf-8") as f:
    
        for item in jsons:
            reddit_id = item["reddit_id"]
            body = item["body"]
            
            prompt = prompt_map[job_type](body)

            request_payload = {
                "custom_id": reddit_id, 
                "method": "POST",
                "url": "/v1/responses",
                "body": {
                    "model": MODEL,
                    "input": [
                        {"role": "system", "content": system_content},
                        {"role": "user", "content": prompt}
                    ],
                    "temperature": 0
                }
            }
            f.write(json.dumps(request_payload, ensure_ascii=False) + "\n")

    return filename


def wait_for_batch(process_fn, count_fn, batch_type: str, poll_interval: int, max_wait_time: int, logger=None):
    """배치 완료까지 polling하며 대기한다.

    Args:
        process_fn: 배치 결과 처리 함수 () -> int (처리된 배치 수 반환)
        count_fn: 미처리 배치 수 조회 함수 (batch_type: str) -> int
        batch_type: 배치 유형 ("classification" | "parsing")
        poll_interval: 폴링 주기 (초)
        max_wait_time: 최대 대기 시간 (초)
        logger: 로거 인스턴스 (없으면 모듈 로거 사용)
    """
    if logger is None:
        logger = logging.getLogger(__name__)

    elapsed_time = 0
    logger.info("배치 완료 대기 시작 (폴링 주기: %d초, 최대 대기: %d초)", poll_interval, max_wait_time)

    while elapsed_time < max_wait_time:
        processed_count = process_fn()

        if processed_count > 0:
            logger.info("✅ %d개 배치 처리 완료", processed_count)
            unprocessed = count_fn(batch_type)
            if unprocessed == 0:
                logger.info("✅ 모든 배치 처리 완료")
                break
            else:
                logger.info("⏳ 아직 %d개 배치 대기 중", unprocessed)
        else:
            unprocessed = count_fn(batch_type)
            if unprocessed == 0:
                logger.info("✅ 처리할 배치가 없습니다")
                break
            logger.info("⏳ %d개 배치 대기 중... %d분 후 재확인", unprocessed, poll_interval // 60)

        time.sleep(poll_interval)
        elapsed_time += poll_interval

    if elapsed_time >= max_wait_time:
        logger.warning("⚠️ 타임아웃: 일부 배치가 완료되지 않았습니다 (최대 대기 시간 %d시간 초과)", max_wait_time // 3600)
