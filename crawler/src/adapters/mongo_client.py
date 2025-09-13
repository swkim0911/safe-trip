from pymongo import MongoClient
from dotenv import load_dotenv
import os

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017/")

SAFETRIP_DB = os.getenv("SAFETRIP_DB", "safe_trip")
WORLD_DB = os.getenv("WORLD_DB", "world")

client = MongoClient(MONGO_URI)
safetrip_db = client[SAFETRIP_DB]
world_db = client[WORLD_DB]


def get_raw_collection():
    return safetrip_db["raw_reddit"]

def get_parsed_collection():
    return safetrip_db["parsed_reddit"]

def get_country_collection():
    return world_db["countries"]

def get_state_collection():
    return world_db["states"]

def get_city_collection():
    return world_db["cities"]

def get_batch_job_collection():
    return safetrip_db["batch_jobs"]
