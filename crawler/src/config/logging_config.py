import logging
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent.parent  # src/ 상위 디렉토리 (프로젝트 루트)
LOG_DIR = BASE_DIR / "logs"

def setup_logging(name: str, level=logging.INFO):
    Path(LOG_DIR).mkdir(exist_ok=True)
    logfile = Path(LOG_DIR) / f"{name}.log"

    logging.basicConfig(
        level=level,
        format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
        handlers=[
            logging.FileHandler(logfile, encoding="utf-8"),
            logging.StreamHandler()
        ]
    )
    return logging.getLogger(name)
