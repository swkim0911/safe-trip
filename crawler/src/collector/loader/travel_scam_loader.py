import mysql.connector
from datetime import datetime, UTC


class TravelScamLoader:
    def __init__(self, parsed_collection):
        self.parsed_collection = parsed_collection

    def mongo_to_mysql(self):
        mysql_conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="password",
            database="safetrip"
        )
        cursor = mysql_conn.cursor()

        for doc in self.parsed_collection.find({}):
            country_id = doc.get("country_id")
            state_id = doc.get("state_id")
            external_id = doc.get("reddit_id")
            source = doc.get("source")
            source_url = doc.get("url")
            author = doc.get("author")
            title = doc.get("title")
            summary = doc.get("summary")
            posted_at = doc.get("posted_at")
            collected_at = doc.get("created_at")

            # Action 매핑
            action_name = doc.get("action")
            cursor.execute(
                "SELECT id FROM scam_action WHERE name = %s", (action_name,))
            row = cursor.fetchone()
            action_id = row[0] if row else None

            # Context 매핑
            context_name = doc.get("context")
            cursor.execute(
                "SELECT id FROM scam_context WHERE name = %s", (context_name,))
            row = cursor.fetchone()
            context_id = row[0] if row else None

            if not action_id or not context_id:
                print(
                    f"[WARN] 매핑 실패: action={action_name}, context={context_name}")
                continue

            now = datetime.now(UTC)

            # INSERT
            cursor.execute(
                """
                INSERT INTO external_report 
                (scam_action_id, scam_context_id, country_id, state_id, external_id, source, source_url, author, title, summary, posted_at, collected_at, created_at, modified_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """,
                (
                    action_id, context_id, country_id, state_id,
                    external_id, source, source_url, author, title, summary,
                    posted_at, collected_at, now, now
                )
            )

        mysql_conn.commit()
        cursor.close()
        mysql_conn.close()
