import logging
from datetime import datetime, UTC, timedelta, timezone

import mysql.connector


class TravelScamLoader:
    """MongoDB에서 MySQL로 Parsing된 데이터를 적재하는 클래스"""
    
    def __init__(self, reddit_repository, etl_config):
        self.reddit_repository = reddit_repository
        self.etl_config = etl_config
        self.logger = logging.getLogger(__name__)

    def mongo_to_mysql(self, load_scope=None):
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
        query = {"parsing.location_enriched": True}

        if load_scope == "daily":
            today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0) # 오늘의 자정 시간 ex) 2025-09-11 00:00:00+00:00
            tomorrow = today + timedelta(days=1) # 내일 자정 시간 ex) 2025-09-12 00:00:00+00:00
            query = {
                "parsing.location_enriched": True,
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
            action_raw = doc.get("action")
            if not action_raw:
                self.logger.warning("action 필드 없음 (reddit_id=%s), 건너뜀", external_id)
                continue
            action_name = action_raw.split(" (")[0].strip()
            cursor.execute(
                "SELECT id FROM scam_action WHERE name = %s", (action_name,))
            row = cursor.fetchone()
            if not row:
                self.logger.warning("scam_action 미존재: %s (reddit_id=%s), 건너뜀", action_name, external_id)
                continue
            action_id = row[0]

            # Context 매핑
            context_raw = doc.get("context")
            if not context_raw:
                self.logger.warning("context 필드 없음 (reddit_id=%s), 건너뜀", external_id)
                continue
            context_name = context_raw.split(" (")[0].strip()
            cursor.execute(
                "SELECT id FROM scam_context WHERE name = %s", (context_name,))
            row = cursor.fetchone()
            if not row:
                self.logger.warning("scam_context 미존재: %s (reddit_id=%s), 건너뜀", context_name, external_id)
                continue
            context_id = row[0]

            cursor.execute(
                "SELECT id FROM countries WHERE dataset_id = %s", (country_id,)
            )
            row = cursor.fetchone()
            if not row:
                self.logger.warning("country 미존재: dataset_id=%s (reddit_id=%s), 건너뜀", country_id, external_id)
                continue
            country_id = row[0]

            if state_id:
                cursor.execute(
                    "SELECT id FROM states WHERE dataset_id = %s", (state_id,)
                )
                row = cursor.fetchone()
                state_id = row[0] if row else None
            if city_id:
                cursor.execute(
                    "SELECT id FROM cities WHERE dataset_id = %s", (city_id,)
                )
                row = cursor.fetchone()
                city_id = row[0] if row else None

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
