import argparse


def build_extract_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="extract_job", # 파서 이름
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


def build_load_parser() -> argparse.ArgumentParser:
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

