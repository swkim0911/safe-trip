# Python 3.13 slim 이미지 기반
FROM python:3.13-slim

# 작업 디렉토리
WORKDIR /app

# 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 소스 코드 복사
COPY . .

# 컨테이너 실행 시 daily-etl.py 실행
CMD ["python", "src/daily_etl.py"]
