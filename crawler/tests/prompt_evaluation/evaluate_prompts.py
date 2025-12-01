"""
Classification 프롬프트 버전별 성능 평가 스크립트
"""
import json, os, sys, time
from datetime import datetime
from pathlib import Path
from collections import defaultdict

from adapters.openai_client import call_openai_api_with_batch, get_completed_batch_result
from collector.transformer.travel_scam_classifier import TravelScamClassifier

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")


def load_test_set(filepath):
    """테스트 세트 로드"""
    samples = []
    with open(filepath, "r", encoding="utf-8") as f:
        for line in f:
            if line.strip():
                samples.append(json.loads(line))
    return samples


def load_prompt_version(version_name):
    """특정 버전의 프롬프트와 시스템 콘텐츠 로드"""
    base_dir = Path(__file__).parent.parent.parent / "data" / "prompt" / "classification" / "versions"
    
    prompt_file = base_dir / f"{version_name}_prompt.txt"
    system_file = base_dir / f"{version_name}_system_content.txt"
    
    with open(prompt_file, "r", encoding="utf-8") as f:
        prompt_template = f.read()
    
    with open(system_file, "r", encoding="utf-8") as f:
        system_content = f.read()
    
    return prompt_template, system_content


def create_evaluation_batch_file(test_set, system_content, prompt_template, temperature=0.0):
    """
    평가 전용: 배치 API용 JSONL 파일 생성
    
    Args:
        test_set: 테스트 데이터셋
        system_content: 시스템 콘텐츠
        prompt_template: 프롬프트 템플릿 (format string)
        temperature: OpenAI temperature 파라미터
    
    Returns:
        생성된 파일 경로
    """
    # 배치 파일 저장 디렉토리
    batch_dir = Path(__file__).parent.parent.parent / "data" / "batch_inputs"
    batch_dir.mkdir(parents=True, exist_ok=True)
    
    # 파일명 생성
    filename = batch_dir / f"eval_batch_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}.jsonl"
    
    # JSONL 파일 작성
    with open(filename, "w", encoding="utf-8") as f:
        for item in test_set:
            reddit_id = item["reddit_id"]
            body = item["body"]
            prompt = prompt_template.format(text=body)
            
            request_payload = {
                "custom_id": reddit_id,
                "method": "POST",
                "url": "/v1/responses",
                "body": {
                    "model": MODEL,
                    "input": [
                        {"role": "system", "content": system_content},
                        {"role": "user", "content": prompt}
                    ],
                    "temperature": temperature
                }
            }
            f.write(json.dumps(request_payload, ensure_ascii=False) + "\n")
    
    return str(filename)




def evaluate_prompt_version(version_name, test_set, temperature=0.0, poll_interval=60, max_wait_time=3600):
    """
    단일 프롬프트 버전 평가 (배치 API 사용)
    
    Args:
        version_name: 프롬프트 버전 이름
        test_set: 테스트 데이터셋
        temperature: OpenAI temperature 파라미터
        poll_interval: 배치 완료 확인 간격 (초)
        max_wait_time: 최대 대기 시간 (초, 기본 1시간)
    """
    print(f"\n{'='*80}")
    print(f"평가 중: {version_name}")
    print(f"{'='*80}")
    
    prompt_template, system_content = load_prompt_version(version_name)
    
    results = {
        "version": version_name,
        "correct": 0,
        "total": len(test_set),
        "by_category": defaultdict(lambda: {"correct": 0, "total": 0}),
        "errors": [],
        "api_calls": 1,  # 배치 API는 1번 호출
        "duration": 0
    }
    
    start_time = time.time()
    
    # 1. 배치 파일 생성 (평가 전용 함수 사용)
    print(f"  테스트 샘플: {len(test_set)}개")
    batch_file = create_evaluation_batch_file(test_set, system_content, prompt_template, temperature)
    print(f"  배치 파일 생성: {batch_file}")
    
    # 2. 배치 제출
    print(f"  배치 제출 중...")
    batch_metadata = call_openai_api_with_batch(batch_file)
    batch_id = batch_metadata["batch_id"]
    print(f"  배치 ID: {batch_id}")
    print(f"  배치 제출 완료. 결과 대기 중... (최대 {max_wait_time}초)")
    
    # 3. 배치 완료 대기 (polling)
    elapsed = 0
    while elapsed < max_wait_time:
        time.sleep(poll_interval)
        elapsed += poll_interval
        
        content, status = get_completed_batch_result(batch_id)
        
        if status == "completed" and content:
            print(f"  ✅ 배치 완료 (대기 시간: {elapsed}초)")
            break
        elif status in ["failed", "expired", "cancelled"]:
            print(f"  ❌ 배치 실패: {status}")
            results["errors"].append({
                "reddit_id": "batch",
                "error": f"Batch failed with status: {status}",
                "category": "batch_error"
            })
            return results
        else:
            print(f"  ⏳ 대기 중... ({elapsed}/{max_wait_time}초, 상태: {status})")
    
    if elapsed >= max_wait_time:
        print(f"  ⚠️ 타임아웃: {max_wait_time}초 초과")
        results["errors"].append({
            "reddit_id": "batch",
            "error": "Batch timeout",
            "category": "batch_error"
        })
        return results
    
    # 4. 결과 파싱 및 평가
    print(f"  결과 파싱 중...")
    test_set_map = {item["reddit_id"]: item for item in test_set}
    
    for line in content.splitlines():
        if not line.strip():
            continue
        
        try:
            record = json.loads(line)
            reddit_id = record["custom_id"]
            output_text = record["response"]["body"]["output"][0]["content"][0]["text"]
            
            # TravelScamClassifier의 static 메서드 사용 (배포 코드 재사용)
            predicted = int(TravelScamClassifier.extract_label_from_output(output_text))
            
            # 원본 테스트 데이터에서 expected 가져오기
            original_item = test_set_map.get(reddit_id)
            if not original_item:
                continue
            
            expected = original_item["expected"]
            category = original_item.get("category", "unknown")
            
            # 통계 업데이트
            results["by_category"][category]["total"] += 1
            
            if predicted == expected:
                results["correct"] += 1
                results["by_category"][category]["correct"] += 1
            else:
                results["errors"].append({
                    "reddit_id": reddit_id,
                    "body_preview": original_item["body"][:150],
                    "expected": expected,
                    "predicted": predicted,
                    "category": category,
                    "output": output_text
                })
        
        except Exception as e:
            print(f"  오류 발생 (reddit_id: {reddit_id}): {e}")
            results["errors"].append({
                "reddit_id": reddit_id,
                "error": str(e),
                "category": "parsing_error"
            })
    
    # 5. 배치 파일 삭제
    Path(batch_file).unlink(missing_ok=True)
    
    results["duration"] = time.time() - start_time
    
    # 최종 결과 출력
    accuracy = results["correct"] / results["total"] if results["total"] > 0 else 0
    print(f"\n최종 정확도: {accuracy:.1%} ({results['correct']}/{results['total']})")
    print(f"실행 시간: {results['duration']:.1f}초")
    print(f"API 호출: 배치 API 1회")
    
    # 카테고리별 정확도
    print(f"\n카테고리별 정확도:")
    for cat, stats in sorted(results["by_category"].items()):
        cat_acc = stats["correct"] / stats["total"] if stats["total"] > 0 else 0
        print(f"  {cat:20s}: {cat_acc:5.1%} ({stats['correct']}/{stats['total']})")
    
    # 오류 샘플 출력 (최대 5개)
    if results["errors"]:
        print(f"\n오류 샘플 (총 {len(results['errors'])}개, 최대 5개 표시):")
        for i, error in enumerate(results["errors"][:5], 1):
            print(f"\n  [{i}] Reddit ID: {error['reddit_id']}")
            print(f"      카테고리: {error.get('category', 'unknown')}")
            print(f"      예상: {error.get('expected', 'N/A')} | 예측: {error.get('predicted', 'N/A')}")
            if 'body_preview' in error:
                print(f"      본문: {error['body_preview']}...")
            if 'output' in error:
                print(f"      출력: {error['output'][:100]}")
            if 'error' in error:
                print(f"      에러: {error['error']}")
    
    return results


def compare_prompts(version_names, test_set_path, output_path=None):
    """여러 프롬프트 버전 비교"""
    test_set = load_test_set(test_set_path)
    
    if not test_set:
        print(f"오류: {test_set_path}에 테스트 샘플이 없습니다.")
        return
    
    print(f"테스트 세트 로드 완료: {len(test_set)}개 샘플")
    
    # 각 버전 평가
    all_results = {}
    for version_name in version_names:
        try:
            results = evaluate_prompt_version(version_name, test_set)
            all_results[version_name] = results
        except FileNotFoundError:
            print(f"경고: {version_name} 프롬프트 파일을 찾을 수 없습니다.")
        except Exception as e:
            print(f"오류: {version_name} 평가 중 오류 발생 - {e}")
    
    # 비교 결과 출력
    print(f"\n{'='*80}")
    print("전체 비교 결과")
    print(f"{'='*80}")
    
    for version_name, results in all_results.items():
        accuracy = results["correct"] / results["total"]
        print(f"{version_name:20s}: {accuracy:5.1%} ({results['correct']}/{results['total']}) - {results['duration']:.1f}초")
    
    # 최고 성능 버전
    if all_results:
        best_version = max(all_results.items(), key=lambda x: x[1]["correct"] / x[1]["total"])
        best_accuracy = best_version[1]["correct"] / best_version[1]["total"]
        print(f"\n최고 성능: {best_version[0]} ({best_accuracy:.1%})")
    
    # 결과 저장 (타임스탬프 포함)
    if output_path is None:
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_path = Path(__file__).parent.parent.parent / "data" / "prompt_evaluation" / f"comparison_results_{timestamp}.json"
    
    with open(output_path, "w", encoding="utf-8") as f:
        # defaultdict를 일반 dict로 변환
        serializable_results = {}
        for version, data in all_results.items():
            serializable_results[version] = {
                **data,
                "by_category": dict(data["by_category"])
            }
        json.dump(serializable_results, f, indent=2, ensure_ascii=False)
    
    print(f"\n결과 저장: {output_path}")
    
    return all_results


if __name__ == "__main__":
    # 테스트 세트 경로
    test_set_path = Path(__file__).parent.parent.parent / "data" / "prompt_evaluation" / "classification_samples_labeled.jsonl"

    if not test_set_path.exists():
        print(f"오류: {test_set_path}가 존재하지 않습니다.")
        print("먼저 샘플 데이터를 라벨링하세요.")
        sys.exit(1)

    # 비교할 버전 목록
    versions_to_compare = [
        "v1_baseline",
        "v2_few_shot"
    ]

    print(f"\n{'='*80}")
    print(f"Classification 프롬프트 평가")
    print(f"{'='*80}")
    print(f"테스트 데이터: {test_set_path}")
    print(f"비교 버전: {', '.join(versions_to_compare)}")
    print(f"{'='*80}\n")

    # 평가 실행
    results = compare_prompts(versions_to_compare, test_set_path)

