from utils import prompt_utils
from datetime import datetime
from pathlib import Path
import json, os

def get_batch_filename(folder: str = "data/batch_inputs") -> str:
    Path(folder).mkdir(parents=True, exist_ok=True)  # 폴더 없으면 생성
    filename = f"batch_{datetime.now().strftime('%Y%m%d_%H%M%S')}.jsonl"
    return str(Path(folder) / filename)

"""
docs 배열을 받아 JSONL 파일로 변환한다.
jsons: [{"reddit_id": "xxx", "body": "..."}, ...]
return: 생성된 파일 경로
"""
def write_jsonl(jsons: list[dict[str, str]], filename: str | None = None) -> str:
    
    system_content = prompt_utils.get_classification_system_content()
    
    if filename is None:
            filename = get_batch_filename()
    
    with open(filename, "w", encoding="utf-8") as f:
        MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
    
        for item in jsons:
            reddit_id = item["reddit_id"]
            body = item["body"]
            
            prompt = prompt_utils.generate_classification_prompt(body)

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
