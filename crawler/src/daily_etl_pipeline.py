"""Daily ETL Pipeline - 매일 실행되는 incremental 수집 파이프라인."""
import argparse
import sys

from etl_orchestrator import ETLOrchestrator


def main():
    parser = argparse.ArgumentParser(description="Run the daily ETL pipeline (last week → daily load).")
    parser.add_argument("--limit", type=int, default=None, help="최대 수집 게시글 수 (테스트용)")
    args = parser.parse_args()

    orchestrator = ETLOrchestrator(job_name="daily_etl_pipeline")
    success = orchestrator.run_pipeline(mode="daily", limit=args.limit)

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
