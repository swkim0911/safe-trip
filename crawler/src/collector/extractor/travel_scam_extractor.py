from datetime import datetime

class TravelScamExtractor:
    def __init__(self, reddit, redditRepository):
        self.reddit = reddit
        self.redditRepository = redditRepository 
        self.BATCH_SIZE = 100
        self.operations = []
        self.keywords = ["travel scam"]

    def __add_operation(self, doc: dict):
        self.operations.append(doc)
        if len(self.operations) >= self.BATCH_SIZE:
            self.redditRepository.flush_raw_ops(self.operations)
    
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
            self.__add_operation(post_doc)

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
                    self.__add_operation(comment_doc)
        
        # 마지막 flush
        self.redditRepository.flush_raw_ops(self.operations)