from datetime import datetime, timezone


class TravelScamExtractor:
    """Reddit에서 여행 사기 관련 데이터를 추출하는 클래스"""
    
    """
    Args:
        reddit_client: Reddit API 클라이언트
        reddit_repository: Reddit 데이터 저장소
        config: ETL 설정 객체
    """
    def __init__(self, reddit_client, reddit_repository, config):
        self.reddit_client = reddit_client
        self.reddit_repository = reddit_repository 
        self.config = config
        self._raw_docs = []

    def _clean_text(self, text: str) -> str:
        if not text:
            return ""
        return text.replace("\u200b", "").strip()

    def _buffer_doc(self, doc: dict):
        self._raw_docs.append(doc)
        if len(self._raw_docs) >= self.config.BATCH_SIZE:
            self.reddit_repository.flush_raw_docs(self._raw_docs)
    
    '''
    reddit에 있는 travel scam raw data -> mongoDB에 json 형태로 저장

    Args:
        time_filter: week/all 중 하나
        limit: 가져올 최대 게시글 수
    '''
    def extract(self, time_filter: str, limit: int | None):
        subreddit = self.reddit_client.subreddit("travel")

        for post in subreddit.search(" OR ".join(self.config.REDDIT_KEYWORDS), sort="relevance", time_filter=time_filter, limit=limit):
            if post.selftext and post.selftext not in ("[deleted]", "[removed]"):
                post_doc = {
                    "reddit_id": f"t3_{post.id}",
                    "source": "reddit",
                    "author": str(post.author) if post.author else None,
                    "body": self._clean_text(post.selftext),
                    "url": f"https://reddit.com{post.permalink}",
                    "type": "post",
                    "posted_at": datetime.fromtimestamp(post.created_utc, tz=timezone.utc)
                }
                self._buffer_doc(post_doc)

            post.comments.replace_more(limit=0)
            for comment in post.comments:
                if comment.parent_id.startswith("t3_"): # depth = 1 댓글만 추출
                    if not comment.body or comment.body in ("[deleted]", "[removed]"):
                        continue

                    comment_doc = {
                        "reddit_id": f"t1_{comment.id}",
                        "source": "reddit",
                        "author": str(comment.author) if comment.author else None,
                        "body": self._clean_text(comment.body),
                        "url": f"https://reddit.com{comment.permalink}",
                        "type": "comment",
                        "posted_at": datetime.fromtimestamp(comment.created_utc, tz=timezone.utc)
                    }
                    self._buffer_doc(comment_doc)
        
        # 마지막 flush
        self.reddit_repository.flush_raw_docs(self._raw_docs)