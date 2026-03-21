import logging
from datetime import datetime, timezone


class TravelScamExtractor:

    def __init__(self, reddit_client, reddit_repository, etl_config):
        self.reddit_client = reddit_client
        self.reddit_repository = reddit_repository
        self.etl_config = etl_config
        self.logger = logging.getLogger(__name__)
        self._raw_records = []

    def _clean_text(self, text: str) -> str:
        if not text:
            return ""
        return text.replace("\u200b", "").strip()

    def _add_raw_record_to_buffer(self, record: dict):
        self._raw_records.append(record)
        if len(self._raw_records) >= self.etl_config.BATCH_SIZE:
            self.reddit_repository.flush_raw_records(self._raw_records)
            self.logger.info("중간 flush: %d건 저장", len(self._raw_records))
            self._raw_records.clear()

    def extract(self, time_filter: str, limit: int | None):
        """Reddit에서 travel scam 데이터를 추출하여 MongoDB에 저장한다.

        Args:
            time_filter: 'week' 또는 'all'
            limit: 최대 게시글 수 (None이면 전체)
        """
        subreddits = "+".join(self.etl_config.REDDIT_SUBREDDITS)
        keywords = " OR ".join(self.etl_config.REDDIT_KEYWORDS)
        self.logger.info("추출 시작 (subreddits=%s, keywords=%s, time_filter=%s, limit=%s)", subreddits, keywords, time_filter, limit)

        subreddit = self.reddit_client.subreddit(subreddits)
        post_count = 0
        comment_count = 0

        for post in subreddit.search(keywords, sort="relevance", time_filter=time_filter, limit=limit):
            if post.selftext and post.selftext not in ("[deleted]", "[removed]"):
                post_record = {
                    "reddit_id": f"t3_{post.id}",
                    "source": "reddit",
                    "author": str(post.author) if post.author else None,
                    "body": self._clean_text(post.selftext),
                    "url": f"https://reddit.com{post.permalink}",
                    "type": "post",
                    "posted_at": datetime.fromtimestamp(post.created_utc, tz=timezone.utc)
                }
                self._add_raw_record_to_buffer(post_record)
                post_count += 1

            post.comments.replace_more(limit=0)  # MoreComments 객체 제거 (Reddit API lazy loading 방지)
            for comment in post.comments:  # post.comments iterate는 최상위 댓글만 반환 (중첩 댓글 제외)
                if not comment.body or comment.body in ("[deleted]", "[removed]"):
                    continue

                comment_record = {
                    "reddit_id": f"t1_{comment.id}",
                    "source": "reddit",
                    "author": str(comment.author) if comment.author else None,
                    "body": self._clean_text(comment.body),
                    "url": f"https://reddit.com{comment.permalink}",
                    "type": "comment",
                    "posted_at": datetime.fromtimestamp(comment.created_utc, tz=timezone.utc)
                }
                self._add_raw_record_to_buffer(comment_record)
                comment_count += 1

        if self._raw_records:
            self.reddit_repository.flush_raw_records(self._raw_records)
            self._raw_records.clear()

        self.logger.info("추출 완료 (posts=%d, comments=%d, total=%d)", post_count, comment_count, post_count + comment_count)