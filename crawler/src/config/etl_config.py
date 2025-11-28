import os
from dataclasses import dataclass, field


@dataclass
class ETLConfig:
    """ETL 프로세스 설정을 중앙 관리하는 클래스"""
    
    # Batch 관련 설정
    BATCH_SIZE: int = 1000
    TOKEN_LIMIT: int = 500_000  # OpenAI Batch API 토큰 제한 (보수적 설정)
    
    # Batch Polling 설정
    BATCH_POLL_INTERVAL: int = int(os.getenv("BATCH_POLL_INTERVAL", "300"))  # 5분
    BATCH_MAX_WAIT_TIME: int = int(os.getenv("BATCH_MAX_WAIT_TIME", "90000"))  # 25시간 (BATCH 작업이 24시간이니까 여유있게 25시간)
    
    # OpenAI 모델 설정
    MODEL_NAME: str = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
    
    # Prompt 버전
    CLASSIFICATION_PROMPT_VERSION: str = os.getenv("CLASSIFICATION_PROMPT_VERSION", "v0")
    PARSING_PROMPT_VERSION: str = os.getenv("PARSING_PROMPT_VERSION", "v0")
    
    # Reddit 관련 설정
    REDDIT_KEYWORDS: list[str] = field(default_factory=lambda: ["travel scam"])
    
    # MySQL 설정
    MYSQL_HOST: str = os.getenv("MYSQL_HOST")
    MYSQL_PORT: int = int(os.getenv("MYSQL_PORT", "3306"))
    MYSQL_USER: str = os.getenv("MYSQL_USER")
    MYSQL_PASSWORD: str = os.getenv("MYSQL_PASSWORD")
    MYSQL_DATABASE: str = os.getenv("MYSQL_DATABASE")
    
    @classmethod
    def create_default(cls) -> "ETLConfig":
        """기본 설정으로 ETLConfig 인스턴스 생성"""
        return cls()



