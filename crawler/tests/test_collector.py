from collector import travel_scam_extractor
from collector import travel_scam_classifier
import praw


KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


def crawl_travel_scam_with_url(reddit: praw.Reddit, limit: int, url: str) -> None:

    submission = reddit.submission(url=url)  # url로 직접 게시글 가져오기
    post_body = clean_text(submission.selftext)

    is_travel_scam = travel_scam_classifier.classify(post_body)
    # if is_travel_scam:
    #     travel_scam_data = travel_scam_extractor.extract(post_body)
    #     # todo: db에 저장
    #     print(travel_scam_data)

    # post.comments.replace_more(limit=0)

    # # depth=1 댓글만 선택 (top-level comments)
    # for i, comment in enumerate(post.comments):

    #     comment_body = clean_text(comment.body)
    #     is_travel_scam_cm = travel_scam_classifier.classify(comment_body)
    #     if is_travl_scam_cm:
    #         travel_scam_data = travel_scam_extractor.extract(commnet_body)
    #         # todo: db에 저장
