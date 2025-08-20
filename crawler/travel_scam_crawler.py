import time
import praw
import travel_scam_classifier
import travel_scam_analyzer
from datetime import datetime


def crawl_travel_scam(reddit:  praw.Reddit) -> None:
