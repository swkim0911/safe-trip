import subprocess
import sys
import time
from pathlib import Path

from config.logging_config import setup_logging


class ETLController:
    """각 ETL Job을 subprocess로 순차 실행하는 컨트롤러 클래스"""
    
    def __init__(self):
        self.logger = setup_logging("init_etl_pipeline")
        self.src_dir = Path(__file__).parent
        
    def _get_job_path(self, job_file: str) -> str:
        """Job 파일의 절대 경로 반환"""
        return str(self.src_dir / "etl_jobs" / job_file)
    
    def run_job(self, job_name: str, job_file: str, args: list = None) -> bool:
        """
        개별 Job을 subprocess로 실행
        
        Args:
            job_name: Job 이름 (로깅용)
            job_file: Job 파일명
            args: Job에 전달할 인자 리스트
            
        Returns:
            성공 여부 (True: 성공, False: 실패)
        """
        job_path = self._get_job_path(job_file)
        cmd = [sys.executable, job_path] + (args or [])
        
        self.logger.info("=" * 60)
        self.logger.info("%s 시작", job_name)
        self.logger.info("명령어: %s", " ".join(cmd))
        
        try:
            start = time.time()
            result = subprocess.run(
                cmd,
                check=False,
                capture_output=True,
                text=True,
                cwd=self.src_dir
            )
            end = time.time()
            
            # Job 로그 (표준 출력/에러 로깅)
            if result.stdout:
                self.logger.info("[Job 로그 - %s]\n%s", job_name, result.stdout.strip())

            if result.stderr:
                self.logger.error("[Job 오류 로그 - %s]\n%s", job_name, result.stderr.strip())

            if result.returncode == 0:
                self.logger.info("%s 완료 (실행 시간: %.2f 초)", job_name, end - start)
                return True
            else:
                self.logger.error(
                    "%s 실패 (반환 코드: %d, 실행 시간: %.2f 초)",
                    job_name, result.returncode, end - start
                )
                return False
                
        except Exception as e:
            self.logger.error("%s 실행 중 예외 발생: %s", job_name, e, exc_info=True)
            return False
    
    def run_pipeline(self, extract_args: list = None, load_args: list = None) -> bool:
        """
        전체 ETL 파이프라인 실행
        
        Args:
            extract_args: Extract Job에 전달할 인자 (기본값: ["all"])            
            load_args: Load Job에 전달할 인자 (기본값: ["all"]) 
        Returns:
            성공 여부
        """
        if extract_args is None:
            extract_args = ["all"]
        if load_args is None:
            load_args = ["all"]
        jobs = [
            ("Extract Job", "extract_job.py", extract_args),
            ("Classification Job", "classification_job.py", []),
            ("Parsing Job", "parsing_job.py", []),
            ("Load Job", "load_job.py", load_args),
        ]
        
        pipeline_start = time.time()
        self.logger.info("=" * 60)
        self.logger.info("ETL 파이프라인 시작")
        self.logger.info("=" * 60)
        
        for job_name, job_file, args in jobs:
            success = self.run_job(job_name, job_file, args)
            if not success:
                self.logger.error("!!! %s 실패. 파이프라인 중단.", job_name)
                return False
        
        pipeline_end = time.time()
        self.logger.info("=" * 60)
        self.logger.info("ETL 파이프라인 완료 (총 실행 시간: %.2f 초)", pipeline_end - pipeline_start)
        self.logger.info("=" * 60)
        return True


def main():
    
    # 명령줄 인자 파싱
    extract_args = [sys.argv[1]] if len(sys.argv) > 1 else None
    load_args = [sys.argv[2]] if len(sys.argv) > 2 else None
  
    # ETL 파이프라인 실행
    controller = ETLController()
    success = controller.run_pipeline(extract_args, load_args)
    
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()

