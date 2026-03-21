"""분류된 데이터의 is_travel_scam 비율 확인 (기존 / 신규 분리)."""
import sys
from pathlib import Path
from datetime import datetime, timezone

sys.path.insert(0, str(Path(__file__).parent.parent.parent / "src"))

from config.dependencies import container

# 기존 10,475개가 분류된 시점 이후를 신규로 간주
# classified_at 기준으로 분리
NEW_DATA_CUTOFF = datetime(2025, 1, 1, tzinfo=timezone.utc)  # 신규 수집 시작일 (조정 필요)


def _ratio_str(count, total):
    return f"{count:,}개 ({count / total * 100:.1f}%)" if total else "0개"


def check_classification_ratio():
    repo = container.reddit_repository

    def stats(query_base: dict):
        total = repo.raw_collection.count_documents({**query_base, "classification": {"$exists": True}})
        true_count = repo.raw_collection.count_documents({**query_base, "classification.is_travel_scam": True})
        return total, true_count

    # 전체
    total, true_count = stats({})
    print(f"\n[전체]")
    print(f"  분류 완료 : {total:,}개")
    print(f"  여행 사기 : {_ratio_str(true_count, total)}")
    print(f"  관련 없음 : {_ratio_str(total - true_count, total)}")

    # 기존 (cutoff 이전에 분류된 것)
    old_total, old_true = stats({"classification.classified_at": {"$lt": NEW_DATA_CUTOFF}})
    print(f"\n[기존 (cutoff 이전)]")
    print(f"  분류 완료 : {old_total:,}개")
    print(f"  여행 사기 : {_ratio_str(old_true, old_total)}")
    print(f"  관련 없음 : {_ratio_str(old_total - old_true, old_total)}")

    # 신규 (cutoff 이후에 분류된 것)
    new_total, new_true = stats({"classification.classified_at": {"$gte": NEW_DATA_CUTOFF}})
    print(f"\n[신규 (cutoff 이후)]")
    print(f"  분류 완료 : {new_total:,}개")
    print(f"  여행 사기 : {_ratio_str(new_true, new_total)}")
    print(f"  관련 없음 : {_ratio_str(new_total - new_true, new_total)}")


if __name__ == "__main__":
    check_classification_ratio()
