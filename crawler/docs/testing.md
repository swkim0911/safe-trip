# 테스트 및 검증 가이드

## 환경 준비

### 1. 의존성 설치

```bash
cd crawler
pip install -r requirements.txt
```

### 2. 환경 변수 설정 (`.env`)

```env
# Reddit API
CLIENT_ID=...
CLIENT_SECRET=...
USERNAME=...
PASSWORD=...
USER_AGENT=...

# OpenAI
OPENAI_API_KEY=...

# MongoDB
MONGO_URL=mongodb://localhost:27018/
SAFETRIP_DB=safe_trip
WORLD_DB=world

# MySQL
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=safetrip-user
MYSQL_PASSWORD=user1234
MYSQL_DATABASE=safetrip
```

로컬 DB는 프로젝트 루트에서 `docker-compose -f docker-compose.dev.yml up`으로 실행한다.

---

## 단위 테스트

```bash
cd crawler
pytest
```

---

## 단계별 독립 실행

각 Job은 `python -m etl_jobs.<job>` 형태로 단독 실행할 수 있다.
실행 위치는 `crawler/src/`다.

```bash
cd crawler/src
```

### Extract Job

Reddit에서 게시글을 수집해 MongoDB `raw_documents`에 저장한다.

```bash
# 최근 1주일치, 최대 10개
python -m etl_jobs.extract_job week 10

# 전체 수집 (limit 없음)
python -m etl_jobs.extract_job all
```

**검증:** MongoDB `raw_documents` 컬렉션에 문서가 쌓였는지 확인한다.

### Classify Job

`raw_documents`를 읽어 OpenAI Batch API로 여행 사기 여부를 분류한다.

```bash
python -m etl_jobs.classify_job
```

배치 제출 후 polling 루프에 진입한다. 완료까지 최대 `BATCH_MAX_WAIT_TIME`(기본 25시간)을 대기한다.

**검증:** `raw_documents`의 `classification.is_travel_scam` 필드가 채워졌는지 확인한다.

### Parse Job

`is_travel_scam: true`인 문서를 OpenAI Batch API로 파싱해 `parsed_documents`에 저장한다.

```bash
python -m etl_jobs.parse_job
```

**검증:** `parsed_documents` 컬렉션에 `title`, `action`, `country_name` 등의 필드가 채워졌는지 확인한다.

### Enrich Location Job

`parsed_documents`의 지명 문자열을 DB ID로 매핑한다.

```bash
python -m etl_jobs.enrich_location_job
```

**검증:** `parsed_documents`의 `parsing.location_enriched: true`인 문서 수를 확인한다.

### Load Job

`location_enriched: true`인 `parsed_documents`를 MySQL `external_report` 테이블로 이전한다.

```bash
# 오늘 수집된 문서만
python -m etl_jobs.load_job daily

# 전체
python -m etl_jobs.load_job all
```

**검증:** MySQL `external_report` 테이블에 행이 추가됐는지 확인한다.

---

## 전체 파이프라인 실행

### 초기 전체 적재

```bash
cd crawler/src

# 전체 수집 (limit 없음)
python init_etl_pipeline.py

# 테스트용 (최대 10개)
python init_etl_pipeline.py --limit 10
```

### 일별 증분 적재

```bash
cd crawler/src

# 최근 1주일치, 오늘 수집분만 MySQL에 적재
python daily_etl_pipeline.py week daily

# 최근 1주일치, 전체 MySQL에 적재
python daily_etl_pipeline.py week all
```

---

## 성공/실패 판단 기준

각 Job은 성공 시 exit code 0, 실패 시 exit code 1로 종료한다.
`ETLOrchestrator`는 Job이 실패하면 파이프라인을 즉시 중단하고 에러 로그를 출력한다.

```
[ERROR] !!! Classify Job 실패. 파이프라인 중단.
```

로그 파일은 `crawler/logs/` 디렉토리에 job별로 저장된다.
