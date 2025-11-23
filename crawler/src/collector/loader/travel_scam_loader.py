import logging
from datetime import datetime, UTC, timedelta, timezone

import mysql.connector
from dotenv import load_dotenv


class TravelScamLoader:
    """MongoDB에서 MySQL로 데이터를 적재하는 클래스"""
    
    def __init__(self, reddit_repository, etl_config):
        """
        Args:
            reddit_repository: Reddit 데이터 저장소
            etl_config: ETL 설정 객체
        """
        self.reddit_repository = reddit_repository
        self.etl_config = etl_config
        self.logger = logging.getLogger(__name__)

    def mongo_to_mysql(self, load_scope=None):
        load_dotenv()

        host = self.etl_config.MYSQL_HOST
        port = self.etl_config.MYSQL_PORT
        user = self.etl_config.MYSQL_USER
        password = self.etl_config.MYSQL_PASSWORD
        database = self.etl_config.MYSQL_DATABASE

        mysql_conn = mysql.connector.connect(
            host=host,
            port=port,
            user=user,
            password=password,
            database=database,
            charset="utf8mb4"
        )

        cursor = mysql_conn.cursor()
        query = {}
        if load_scope == "daily":
            today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0) # 오늘의 자정 시간 ex) 2025-09-11 00:00:00+00:00
            tomorrow = today + timedelta(days=1) # 내일 자정 시간 ex) 2025-09-12 00:00:00+00:00
            query = {
                "created_at": {
                    "$gte": today,
                    "$lt": tomorrow
                }
            } # created_at >= {오늘 자정} 그리고 created_at < {내일 자정}

        parsed_docs = self.reddit_repository.find_parsed_documents(query)

        for doc in parsed_docs:
            country_id = doc.get("country_id")
            state_id = doc.get("state_id")
            city_id = doc.get("city_id")
            external_id = doc.get("reddit_id")
            source = doc.get("source")
            source_url = doc.get("url")
            author = doc.get("author")
            title = doc.get("title")
            summary = doc.get("summary")
            posted_at = doc.get("posted_at")
            collected_at = doc.get("created_at")

            # Action 매핑
            action_name = doc.get("action").split(" (")[0].strip()
            cursor.execute(
                "SELECT id FROM scam_action WHERE name = %s", (action_name, ))
            row = cursor.fetchone()
            action_id = row[0]

            # Context 매핑
            context_name = doc.get("context").split(" (")[0].strip()
            cursor.execute(
                "SELECT id FROM scam_context WHERE name = %s", (context_name,))
            row = cursor.fetchone()
            context_id = row[0]

            cursor.execute(
                "SELECT id FROM countries WHERE dataset_id = %s", (country_id,)
            )
            row = cursor.fetchone()
            country_id = row[0]

            if state_id:
                cursor.execute(
                    "SELECT id FROM states WHERE dataset_id = %s", (state_id,)
                )
                row = cursor.fetchone()
                state_id = row[0]
            if city_id:
                cursor.execute(
                    "SELECT id FROM cities WHERE dataset_id = %s", (city_id,)
                )
                row = cursor.fetchone()
                city_id = row[0]

            now = datetime.now(UTC)

            # INSERT
            cursor.execute(
                """
                INSERT INTO external_report 
                (scam_action_id, scam_context_id, country_id, state_id, city_id, external_id, source, source_url, author, title, content, posted_at, collected_at, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """,
                (
                    action_id, context_id, country_id, state_id, city_id,
                    external_id, source, source_url, author, title, summary,
                    posted_at, collected_at, now, now
                )
            )

        mysql_conn.commit()
        cursor.close()
        mysql_conn.close()
