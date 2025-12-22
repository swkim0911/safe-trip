import argparse
import sys

from etl_orchestrator import ETLOrchestrator

def main():

    parser = argparse.ArgumentParser(description="Run INITIAL ETL pipeline (full extract → classify → parse → enrich → load)")
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Limit number of records for initial extract (optional)"
    )
    args = parser.parse_args()

    extract_args = ["all"]
    if args.limit is not None:
        extract_args.append(str(args.limit))
    load_args = ["all"]
  
    # ETL 파이프라인 실행
    orchestrator = ETLOrchestrator(job_name="init_etl_pipeline")
    success = orchestrator.run_pipeline(extract_args, load_args)
    
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
