"""
MongoDB에서 classification 테스트용 샘플 추출 스크립트
"""
import json, sys
from pathlib import Path

from adapters import mongo_client


def extract_samples(n_samples=30):
    """
    MongoDB에서 랜덤 샘플 추출
    
    Args:
        n_samples: 추출할 샘플 수 (기본 30개)
    """
    raw_collection = mongo_client.get_raw_collection()
    
    # 전체 문서 중 랜덤 샘플링
    samples = list(raw_collection.aggregate([
        {"$sample": {"size": n_samples}}
    ]))
    
    # 출력 디렉토리 생성
    output_dir = Path(__file__).parent.parent.parent / "data" / "evaluation"
    output_dir.mkdir(parents=True, exist_ok=True)

    # JSONL 형식으로 저장 (라벨링 전)
    output_file = output_dir / "classification_samples_unlabeled.jsonl"

    with open(output_file, "w", encoding="utf-8") as f:
        for sample in samples:
            # 필요한 필드만 추출
            item = {
                "reddit_id": sample["reddit_id"],
                "body": sample["body"],
                "url": sample.get("url", ""),
                "type": sample.get("type", ""),
                # 라벨링용 필드 (수동으로 채울 예정)
                "expected": None,  # 1 or 0
                "category": None,  # 카테고리
                "notes": ""  # 메모
            }
            f.write(json.dumps(item, ensure_ascii=False) + "\n")

    print(f"✓ {n_samples}개 샘플 추출 완료")
    print(f"✓ 저장 위치: {output_file}")
    print(f"\n다음 단계:")
    print(f"1. {output_file} 파일을 열어서 수동으로 라벨링")
    print(f"2. expected 필드에 1 또는 0 입력")
    print(f"3. category 필드에 카테고리 입력 (예: clear_positive, advice_only, etc)")
    print(f"4. 라벨링 완료 후 classification_samples_labeled.jsonl로 저장")
    
    return output_file


if __name__ == "__main__":
    
    # 커맨드라인 인자로 샘플 수 지정 가능
    n_samples = int(sys.argv[1]) if len(sys.argv) > 1 else 30
    
    output_file = extract_samples(n_samples)

