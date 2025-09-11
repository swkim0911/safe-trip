from datetime import datetime

class TravelScamExtractor:
    def __init__(self, reddit, reddit_repository):
        self.reddit = reddit
        self.reddit_repository = reddit_repository 
        self.BATCH_SIZE = 1000
        self.buffered_docs = []
        self.keywords = ["travel scam"]

    def __add_doc(self, doc: dict):
        self.buffered_docs.append(doc)
        if len(self.buffered_docs) >= self.BATCH_SIZE:
            self.reddit_repository.flush_raw_ops(self.buffered_docs)
    
    '''
    reddit에 있는 travel scam raw data -> mongoDB에 json 형태로 저장

    @Args:
        time_filter: hour/day/week/month/year/all 중 하나
        limit: 가져올 최대 게시글 수
    '''
    def extract(self, time_filter: str, limit: int):
        subreddit = self.reddit.subreddit("travel") ## todo: redit에 직접 의존할 필요없지 않나.

        for post in subreddit.search(" OR ".join(self.keywords), sort="relevance", time_filter=time_filter, limit=limit):
            post_doc = {
                "reddit_id": f"t3_{post.id}",
                "source": "reddit",
                "author": str(post.author) if post.author else None,
                "body": post.selftext,
                "url": f"https://reddit.com{post.permalink}",
                "type": "post",
                "posted_at": datetime.utcfromtimestamp(post.created_utc)
            }
            self.__add_doc(post_doc)

            post.comments.replace_more(limit=0)
            for comment in post.comments:
                if comment.parent_id.startswith("t3_"):
                    comment_doc = {
                        "reddit_id": f"t1_{comment.id}",
                        "source": "reddit",
                        "author": str(comment.author) if comment.author else None,
                        "body": comment.body,
                        "url": f"https://reddit.com{comment.permalink}",
                        "type": "comment",
                        "posted_at": datetime.utcfromtimestamp(comment.created_utc),
                    }
                    self.__add_doc(comment_doc)
        
        # 마지막 flush
        self.reddit_repository.flush_raw_ops(self.buffered_docs)