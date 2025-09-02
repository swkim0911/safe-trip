
from pymongo import MongoClient
from dotenv import load_dotenv
import os

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017/")

MONGO_DB = os.getenv("MONGO_DB", "safe_trip")

client = MongoClient(MONGO_URI)
db = client[MONGO_DB]


def get_raw_collection():
    return db["raw_reddit"]

def get_parsed_collection():
    return db["parsed_reddt"]
