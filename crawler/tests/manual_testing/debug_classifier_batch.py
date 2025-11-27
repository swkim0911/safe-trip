from collector.classifier import travel_scam_classifier
from adapters.reddit_client import get_instance
import time
import praw


KEYWORDS = ['"travel scam"']


def clean_text(text: str) -> str:
    if not text:
        return ""
    return text.replace("\u200b", "").strip()


def test_classify_prompt_for_all_case(reddit, file_path):
    results = []
    expected_label = None
    current_url = None

    with open(file_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue

            if line.startswith("##"):
                # 설명 줄은 무시
                continue
            elif line.startswith("-"):
                # 정답 라벨 줄
                try:
                    expected_label = int(line.lstrip("-").strip())
                except ValueError:
                    expected_label = None

                if current_url and expected_label is not None:

                    is_travel_scam = crawl_travel_scam_with_url(
                        reddit, current_url)
                    match = (is_travel_scam == expected_label)
                    results.append({
                        "url": current_url,
                        "expected": expected_label,
                        "predicted": is_travel_scam,
                        "match": match
                    })
                current_url = None
                expected_label = None
            else:
                # URL 줄
                current_url = line

    return results


def crawl_travel_scam_with_url(reddit: praw.Reddit, url: str) -> int:

    submission = reddit.submission(url=url)  # url로 직접 게시글 가져오기
    post_body = clean_text(submission.selftext)

    is_travel_scam = travel_scam_classifier.classify(post_body)
    return is_travel_scam


if __name__ == "__main__":
    start = time.time()

    reddit = get_instance()
    file_path = "data/post_list.txt"
    results = test_classify_prompt_for_all_case(reddit, file_path)

    for r in results:
        print(f"URL: {r['url']}")
        print(
            f"Expected: {r['expected']}, Predicted: {r['predicted']}, Match: {r['match']}")
        print("-" * 50)
    end = time.time()

    print(f"테스트 실행 시간: {end - start:.2f} 초")
