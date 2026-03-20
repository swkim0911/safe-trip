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

    orchestrator = ETLOrchestrator(job_name="init_etl_pipeline")
    success = orchestrator.run_pipeline(mode="init", limit=args.limit)
    
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
