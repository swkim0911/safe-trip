import logging
import sys

_LOGGING_CONFIGURED = False

class LevelBasedStreamHandler(logging.StreamHandler):
    """커스텀 핸들러: 로그 레벨에 따라 stdout/stderr으로 나누어 출력"""

    def emit(self, record):
        if record.levelno >= logging.WARNING:
            self.stream = sys.stderr
        else:
            self.stream = sys.stdout
        super().emit(record)


def setup_logging(name: str) -> logging.Logger:
    """Return a logger that shares the same global handler configuration."""
    global _LOGGING_CONFIGURED

    if not _LOGGING_CONFIGURED:
        root_logger = logging.getLogger()
        root_logger.setLevel(logging.INFO)
        root_logger.handlers.clear()

        handler = LevelBasedStreamHandler()
        formatter = logging.Formatter(
            "%(asctime)s [%(levelname)s] [%(name)s] %(message)s",
            datefmt="%Y-%m-%d %H:%M:%S"
        )
        handler.setFormatter(formatter)
        root_logger.addHandler(handler)

        _LOGGING_CONFIGURED = True

    logger = logging.getLogger(name)
    logger.setLevel(logging.INFO)
    logger.propagate = True  # use root handler
    return logger
