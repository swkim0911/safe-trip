"""분류되지 않은 raw_documents 수 확인."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent / "src"))

from config.dependencies import container


def count_unclassified():
    repo = container.reddit_repository

    total = repo.raw_collection.count_documents({})
    unclassified = repo.raw_collection.count_documents({"classification": {"$exists": False}})
    classified = total - unclassified

    print(f"\n전체       : {total:,}개")
    print(f"분류 완료  : {classified:,}개")
    print(f"분류 필요  : {unclassified:,}개")


if __name__ == "__main__":
    count_unclassified()
