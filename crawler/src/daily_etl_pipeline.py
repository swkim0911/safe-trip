import argparse
import sys

from etl_orchestrator import ETLOrchestrator


def main():
    parser = argparse.ArgumentParser(description="Run the ETL pipeline (extract → classify → parse → load).")
    parser.add_argument("time_filter", choices=["week", "all"])
    parser.add_argument("--limit", type=int, default=None)
    parser.add_argument("load_scope", choices=["daily", "all"])
    parsed_args = parser.parse_args()

    extract_args = [parsed_args.time_filter]
    if parsed_args.limit is not None:
        extract_args.append(str(parsed_args.limit))
    load_args = [parsed_args.load_scope]

    # ETL 파이프라인 실행
    orchestrator = ETLOrchestrator(job_name="daily_etl_pipeline")
    success = orchestrator.run_pipeline(extract_args, load_args)

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
