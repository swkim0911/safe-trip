"""
대화형 JSONL 샘플 라벨링 도구
"""
import json
import sys
from pathlib import Path


def interactive_labeling(input_file, output_file=None):
    """
    대화형으로 샘플을 라벨링하고 실시간으로 파일에 저장
    
    Args:
        input_file: 입력 JSONL 파일
        output_file: 출력 JSONL 파일 (None이면 _labeled 붙여서 생성)
    """
    input_path = Path(input_file)
    
    if not input_path.exists():
        print(f"오류: {input_file} 파일이 존재하지 않습니다.")
        return
    
    # 출력 파일 경로 설정
    if output_file is None:
        output_path = input_path.parent / input_path.name.replace("_unlabeled", "_labeled")
    else:
        output_path = Path(output_file)
    
    # 입력 파일 읽기
    with open(input_path, 'r', encoding='utf-8') as f:
        samples = [json.loads(line) for line in f if line.strip()]
    
    total = len(samples)
    print(f"\n{'='*80}")
    print(f"대화형 라벨링 시작")
    print(f"{'='*80}")
    print(f"총 {total}개 샘플")
    print(f"출력 파일: {output_path}")
    print(f"\n명령어:")
    print(f"  1 or 0: expected 값 입력")
    print(f"  s: 이 샘플 건너뛰기")
    print(f"  q: 종료 (지금까지 라벨링한 것만 저장)")
    print(f"{'='*80}\n")
    
    labeled_samples = []
    
    for i, sample in enumerate(samples, 1):
        # 이미 라벨링된 샘플이면 그대로 사용
        if sample.get('expected') is not None and sample.get('category'):
            labeled_samples.append(sample)
            print(f"[{i}/{total}] 이미 라벨링됨 - 건너뜀")
            continue
        
        print(f"\n{'='*80}")
        print(f"샘플 [{i}/{total}]")
        print(f"{'='*80}")
        print(f"reddit_id: {sample['reddit_id']}")
        print(f"Type: {sample.get('type', 'N/A')}")
        print(f"\n[Body]")
        print("-" * 80)
        print(sample['body'])
        print("-" * 80)
        if sample.get('url'):
            print(f"\nURL: {sample['url']}")
        
        # Expected 입력
        while True:
            expected_input = input(f"\nExpected (1/0/s=skip/q=quit): ").strip().lower()
            
            if expected_input == 'q':
                print(f"\n종료 요청. 지금까지 {len(labeled_samples)}개 라벨링 완료.")
                break
            elif expected_input == 's':
                print("샘플 건너뛰기")
                break
            elif expected_input in ['0', '1']:
                expected = int(expected_input)
                
                # Category 입력
                print("\n카테고리 예시:")
                print("  긍정: clear_positive, edge_case_positive")
                print("  부정: clear_negative, advice_only, second_hand, lacks_context, ambiguous_location, ambiguous_firsthand")
                category = input("Category: ").strip()
                
                # Notes 입력 (선택)
                notes = input("Notes (optional, Enter=skip): ").strip()
                
                # 샘플 업데이트
                sample['expected'] = expected
                sample['category'] = category if category else None
                sample['notes'] = notes
                
                labeled_samples.append(sample)
                
                # 실시간 저장
                with open(output_path, 'w', encoding='utf-8') as f:
                    for labeled_sample in labeled_samples:
                        f.write(json.dumps(labeled_sample, ensure_ascii=False) + '\n')
                
                print(f"✓ 저장 완료 ({len(labeled_samples)}/{total})")
                break
            else:
                print("잘못된 입력. 1, 0, s, 또는 q를 입력하세요.")
        
        # quit 명령이면 중단
        if expected_input == 'q':
            break
    
    print(f"\n{'='*80}")
    print(f"라벨링 완료!")
    print(f"{'='*80}")
    print(f"총 라벨링: {len(labeled_samples)}/{total}")
    print(f"저장 위치: {output_path}")
    print(f"{'='*80}\n")


def show_progress(filepath):
    """라벨링 진행 상황 확인"""
    if not Path(filepath).exists():
        print(f"{filepath} 파일이 존재하지 않습니다.")
        return
    
    with open(filepath, 'r', encoding='utf-8') as f:
        samples = [json.loads(line) for line in f if line.strip()]
    
    total = len(samples)
    labeled = sum(1 for s in samples if s.get('expected') is not None)
    
    category_counts = {}
    expected_counts = {0: 0, 1: 0}
    
    for sample in samples:
        if sample.get('expected') is not None:
            expected_counts[sample['expected']] = expected_counts.get(sample['expected'], 0) + 1
            cat = sample.get('category', 'unknown')
            category_counts[cat] = category_counts.get(cat, 0) + 1
    
    print(f"\n{'='*80}")
    print(f"라벨링 진행 상황: {Path(filepath).name}")
    print(f"{'='*80}")
    print(f"총 샘플: {total}")
    print(f"완료: {labeled} ({labeled/total*100:.1f}%)")
    print(f"대기: {total - labeled}")
    
    print(f"\n[Expected 분포]")
    print(f"  1 (포함): {expected_counts.get(1, 0)}")
    print(f"  0 (제외): {expected_counts.get(0, 0)}")
    
    if category_counts:
        print(f"\n[Category 분포]")
        for cat, count in sorted(category_counts.items()):
            print(f"  {cat:20s}: {count}")
    
    print(f"{'='*80}\n")


if __name__ == "__main__":
    import argparse
    
    # 기본 경로
    default_input = Path(__file__).parent.parent.parent / "data" / "evaluation" / "classification_samples_unlabeled.jsonl"
    default_output = Path(__file__).parent.parent.parent / "data" / "evaluation" / "classification_samples_labeled.jsonl"
    
    parser = argparse.ArgumentParser(description="대화형 샘플 라벨링 도구")
    parser.add_argument("input_file", nargs='?', default=str(default_input),
                       help="입력 JSONL 파일")
    parser.add_argument("--output", default=None,
                       help="출력 JSONL 파일 (기본: input_file에서 _labeled로 변경)")
    parser.add_argument("--progress", action="store_true",
                       help="진행 상황만 확인")
    
    args = parser.parse_args()
    
    if args.progress:
        # 진행 상황 확인
        check_file = args.output if args.output else str(default_output)
        show_progress(check_file)
    else:
        # 라벨링 시작
        interactive_labeling(args.input_file, args.output)

