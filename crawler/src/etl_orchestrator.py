import subprocess
import sys
import time
from pathlib import Path

from config.logging_config import setup_logging

class ETLOrchestrator:
    """각 ETL Job을 subprocess로 순차 실행하는 오케스트레이터 클래스"""

    def __init__(self, job_name: str):
        self.logger = setup_logging(job_name)
        self.src_dir = Path(__file__).parent

    def _get_job_module(self, job_file: str) -> str:
        """Job 파일명을 python -m 으로 실행할 모듈명으로 변환"""
        module_name = Path(job_file).stem
        return f"etl_jobs.{module_name}"

    def _run_job(self, job_name: str, job_file: str, args: list = None) -> bool:
        """
        개별 Job을 subprocess로 실행

        Args:
            job_name: Job 이름 (로깅용)
            job_file: Job 파일명
            args: Job에 전달할 인자 리스트

        Returns:
            성공 여부 (True: 성공, False: 실패)
        """
        job_module = self._get_job_module(job_file)
        cmd = [sys.executable, "-m", job_module] + (args or [])

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

    def run_pipeline(self, mode: str = "init", limit: int | None = None) -> bool:
        """
        전체 ETL 파이프라인 실행

        Args:
            mode: 실행 모드 ("init": 전체 수집, "daily": 일별 증분 수집)
            limit: 최대 수집 게시글 수 (테스트용, 선택)
        Returns:
            성공 여부
        """
        _MODE_ARGS = {
            "init":  {"extract": ["all"],  "load": ["all"]},
            "daily": {"extract": ["week"], "load": ["daily"]},
        }
        if mode not in _MODE_ARGS:
            self.logger.error("알 수 없는 모드: %s (init 또는 daily만 허용)", mode)
            return False

        extract_args = _MODE_ARGS[mode]["extract"][:]
        load_args = _MODE_ARGS[mode]["load"]
        if limit is not None:
            extract_args.append(str(limit))

        jobs = [
            ("Extract Job", "extract_job.py", extract_args),
            ("Classify Job", "classify_job.py", []),
            ("Parse Job", "parse_job.py", []),
            ("Enrich Location Job", "enrich_location_job.py", []),
            ("Load Job", "load_job.py", load_args),
        ]

        pipeline_start = time.time()
        self.logger.info("=" * 60)
        self.logger.info("ETL 파이프라인 시작")

        for job_name, job_file, args in jobs:
            success = self._run_job(job_name, job_file, args)
            if not success:
                self.logger.error("!!! %s 실패. 파이프라인 중단.", job_name)
                return False

        pipeline_end = time.time()
        self.logger.info("=" * 60)
        self.logger.info("ETL 파이프라인 완료 (총 실행 시간: %.2f 초)", pipeline_end - pipeline_start)
        self.logger.info("=" * 60)
        return True