"""
Parse Job 실행 테스트 (10개 문서로 실제 파싱 작업 테스트)
"""
import sys
import subprocess
import time
from pathlib import Path


def test_parse_job_with_limit():
    """parse_job.py를 10개 문서 제한으로 실행하여 전체 플로우 테스트"""
    print("\n" + "=" * 60)
    print("🧪 Parse Job 테스트 (limit=10)")
    print("=" * 60 + "\n")
    
    # parse_job.py 실행
    src_dir = Path(__file__).parent.parent.parent / "src"
    parse_job_path = src_dir / "etl_jobs" / "parse_job.py"
    
    cmd = [sys.executable, str(parse_job_path), "--limit", "10"]
    print(f"실행 명령어: {' '.join(cmd)}\n")
    
    start_time = time.time()
    
    try:
        # subprocess로 parse_job 실행 (실시간 출력)
        process = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            cwd=src_dir
        )
        
        # 실시간 출력
        for line in process.stdout:
            print(line, end='')
        
        process.wait()
        end_time = time.time()
        
        print("\n" + "=" * 60)
        
        if process.returncode == 0:
            print(f"✅ 테스트 성공 (실행 시간: {end_time - start_time:.2f}초)")
        else:
            print(f"❌ 테스트 실패 (반환 코드: {process.returncode})")
            sys.exit(1)
        
        print("=" * 60 + "\n")
        
    except Exception as e:
        print(f"\n❌ 실행 중 오류: {e}")
        sys.exit(1)


if __name__ == "__main__":
    test_parse_job_with_limit()

