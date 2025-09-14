from utils import prompt_utils
from datetime import datetime
from pathlib import Path
import json, os

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

            req = {
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
            f.write(json.dumps(req, ensure_ascii=False) + "\n")

    return filename
