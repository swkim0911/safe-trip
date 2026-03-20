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
    abs_folder.mkdir(parents=True, exist_ok=True)
    filename = f"batch_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}.jsonl"
    return str(Path(abs_folder) / filename)

def write_jsonl(jsons: list[dict[str, str]], job_type: str) -> str:
    """docs 리스트를 OpenAI Batch API용 JSONL 파일로 변환한다.

    Args:
        jsons: [{"reddit_id": "xxx", "body": "..."}, ...]
        job_type: "classification" 또는 "parsing"

    Returns:
        생성된 JSONL 파일 경로
    """

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


def wait_for_capacity(process_fn, count_fn, max_concurrent: int, poll_interval: int, logger=None):
    """in-flight 배치 수가 max_concurrent 미만이 될 때까지 대기한다.

    Args:
        process_fn: 완료된 배치 결과를 처리하는 함수 () -> int
        count_fn: 현재 in-flight 배치 수를 반환하는 함수 () -> int
        max_concurrent: 허용할 최대 동시 in-flight 배치 수
        poll_interval: 폴링 주기 (초)
        logger: 로거 인스턴스
    """
    if logger is None:
        logger = logging.getLogger(__name__)

    while True:
        process_fn()
        count = count_fn()
        if count < max_concurrent:
            return
        logger.info("배치 %d개 처리 중 (한도: %d), %d초 후 재확인...", count, max_concurrent, poll_interval)
        time.sleep(poll_interval)


def submit_batches_in_chunks(docs, token_estimator, token_limit: int, submit_fn, save_batch_fn, batch_label: str, logger=None, capacity_fn=None):
    """토큰 한도 기반으로 docs를 배치로 나눠 제출한다.

    배치 제출 전에 capacity_fn을 호출해 슬롯이 생길 때까지 대기한다.
    슬롯이 생기는 즉시 다음 배치를 제출하는 슬라이딩 윈도우 방식이다.

    Args:
        docs: 처리할 문서 이터러블 (각 doc은 {"reddit_id": ..., "body": ...} 형태)
        token_estimator: body를 받아 토큰 수를 반환하는 함수
        token_limit: 배치당 최대 토큰 수
        submit_fn: 배치 문서 리스트를 제출하고 batch_metadata를 반환하는 함수
        save_batch_fn: batch_metadata를 저장하는 함수
        batch_label: 로그용 레이블 ("classification" | "parsing")
        logger: 로거 인스턴스 (없으면 모듈 로거 사용)
        capacity_fn: 배치 제출 전 슬롯 확보를 대기하는 함수 (None이면 대기 없이 즉시 제출)
    """
    if logger is None:
        logger = logging.getLogger(__name__)

    batch_docs = []
    expected_tokens = 0

    for doc in docs:
        reddit_id = doc["reddit_id"]
        body = doc["body"]
        token_count = token_estimator(body)

        if token_count > token_limit:
            logger.warning("(%s) 문서 %s 가 토큰 제한 초과, 스킵함", batch_label, reddit_id)
            continue

        batch_doc = {"reddit_id": reddit_id, "body": body}

        if expected_tokens + token_count > token_limit:
            if capacity_fn:
                capacity_fn()
            logger.info("(%s) Batch %d개 문서 (%d tokens) 요청", batch_label, len(batch_docs), expected_tokens)
            batch_metadata = submit_fn(batch_docs)
            save_batch_fn(batch_metadata)
            batch_docs = [batch_doc]
            expected_tokens = token_count
        else:
            batch_docs.append(batch_doc)
            expected_tokens += token_count

    if batch_docs:
        if capacity_fn:
            capacity_fn()
        logger.info("(%s) 마지막 Batch %d개 문서 (%d tokens) 요청", batch_label, len(batch_docs), expected_tokens)
        batch_metadata = submit_fn(batch_docs)
        save_batch_fn(batch_metadata)


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
