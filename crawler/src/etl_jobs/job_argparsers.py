import argparse

def create_extract_job_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="extract_job",
        description="Extract travel scam data from Reddit and store raw records.",
    )
    parser.add_argument(
        "time_filter",
        choices=["week", "all"],
        help="Reddit API time filter.",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Optional maximum number of posts to fetch.",
    )
    return parser


def create_classify_job_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="classify_job",
        description="Classify raw Reddit posts as travel scam or not using batch API.",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Optional maximum number of documents to classify (for testing).",
    )
    return parser


def create_parse_job_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="parse_job",
        description="Parse classified travel scam posts using batch API.",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Optional maximum number of documents to parse (for testing).",
    )
    return parser


def create_load_job_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="load_job",
        description="Load parsed travel scam data into MySQL.",
    )
    parser.add_argument(
        "load_scope",
        choices=["daily", "all"],
        help="Limit the load to 'daily' data or load 'all' available data.",
    )
    return parser

