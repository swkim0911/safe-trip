# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Language

SafeTrip is an English-language service. All UI text, components, and user-facing content must be written in English.

## Project Overview

**Safe-Trip** is a travel scam detection and reporting platform. It collects Reddit scam discussions via a Python ETL pipeline, processes them with OpenAI, and serves the data through a Spring Boot API consumed by a Vue 3 frontend.

## Repository Structure

```
BE/          # Spring Boot backend (Java 17, Gradle)
FE/          # Vue 3 frontend (Vite, Pinia)
crawler/     # Python ETL pipeline
docker-compose.dev.yml   # Local dev: MySQL (3306), Redis (6379), MongoDB (27018)
docker-compose.prod.yml  # Production deployment
```

## Commands

### Backend (run from `BE/`)
```bash
./gradlew clean build -x test   # Build
./gradlew test --no-daemon       # Test
./gradlew bootRun                # Run
```

### Frontend (run from `FE/`)
```bash
npm run dev     # Development server
npm run build   # Production build
```

### Crawler (run from `crawler/`)
```bash
pip install -r requirements.txt

# Run full initial ETL pipeline
python src/init_etl_pipeline.py [--limit N]

# Run daily ETL pipeline
python src/daily_etl_pipeline.py [--limit N]

# Run individual jobs (from crawler/src/)
python -m etl_jobs.extract_job <week|all> [--limit N]
python -m etl_jobs.classify_job [--limit N]
python -m etl_jobs.parse_job [--limit N]
python -m etl_jobs.enrich_location_job
python -m etl_jobs.load_job <daily|all>

# Tests
pytest
```

### Docker
```bash
docker-compose -f docker-compose.dev.yml up    # Local dev
docker-compose -f docker-compose.prod.yml up   # Production
```

## ETL Pipeline Architecture

The pipeline runs 5 sequential stages, each as an isolated subprocess managed by `ETLOrchestrator`:

```
Reddit API → Extract → Classify → Parse → Enrich Location → Load MySQL
```

| Stage | File | Description |
|-------|------|-------------|
| Extract | `etl_jobs/extract_job.py` | Fetches Reddit posts → MongoDB `raw_documents` |
| Classify | `etl_jobs/classify_job.py` | OpenAI Batch API classifies scam relevance → updates `raw_documents` |
| Parse | `etl_jobs/parse_job.py` | OpenAI Batch API extracts structured fields → MongoDB `parsed_documents` |
| Enrich | `etl_jobs/enrich_location_job.py` | Fuzzy-matches location strings to DB IDs |
| Load | `etl_jobs/load_job.py` | Transfers enriched MongoDB data → MySQL |

**Key design decisions:**
- Each job runs as a subprocess (`python -m etl_jobs.<job>`), so failures are isolated and the orchestrator can log/abort cleanly.
- OpenAI Batch API is used (not streaming) for cost reduction. Jobs submit batches, store batch job IDs in MongoDB `batch_jobs`, then poll until completion.
- `ETLOrchestrator` (`src/etl_orchestrator.py`) handles subprocess execution. Entry points are `init_etl_pipeline.py` (full historical load) and `daily_etl_pipeline.py` (recurring).
- `crawler/src/config/dependencies.py` uses `cached_property` as a lightweight DI container (singletons).

## Backend Architecture

- **REST API** at port 8080, JWT-authenticated
- Key packages: `controller/`, `service/`, `entity/`, `repository/`, `security/`, `dto/`, `global/` (exception handling)
- Two storage layers: MySQL (JPA entities) for final data, Redis for caching/sessions
- S3 for user-uploaded report images

## Data Flow

1. Crawler writes processed scam reports to MySQL (`external_report`, `scam_action`, `scam_context`)
2. Backend API reads these and serves them alongside user-submitted `user_report`
3. Frontend displays scam data on an interactive Leaflet map, aggregated by country/state/city

## 커밋 규칙

- `docs/spec.md`를 제외한 `docs/` 하위 파일은 커밋하지 않는다.

## Git Commit Convention

커밋 메시지는 이슈 번호를 prefix로 포함한다:

```
[#이슈번호] type: 설명

예시:
[#143] feat: 지도 마커 클릭 시 사이드바 연동
[#143] fix: AI Bot 배지 잘림 현상 수정
[#143] style: 사이드바 카드 디자인 단순화
[#143] refactor: ETL 파이프라인 구조 정리
```

## CI/CD

Push to `main` → GitHub Actions → build + test → Docker images to DockerHub → SSH deploy to EC2 via `docker-compose.prod.yml`.

### 인스턴스 명명 규칙

배포 인프라에는 **두 가지 다른 분류**가 동시에 존재한다.

- **인스턴스 정체성 (고정)**: `RUNNER_IP` = self-hosted runner가 떠있는 인스턴스, `REMOTE_IP` = SSH로 접근하는 다른 인스턴스. GitHub Actions vars에 IP가 박혀 있어 자동으로 안 바뀜.
- **트래픽 역할 (로테이션)**: `ACTIVE` = 지금 LB가 트래픽을 보내는 쪽, `STANDBY` = 대기 중인 쪽. 매 배포마다 둘 사이를 오감.

표준 Blue-Green 배포 용어에서 Blue ≡ Active이므로, 코드에선 혼동을 피하기 위해 인스턴스 정체성에 Blue/Green 라벨을 쓰지 않는다.

### Daily ETL Cron 위치

Daily ETL cron은 **runner 인스턴스에만** 등록된다 (모니터링 단일 소스 + 무료 티어 한도). LB가 remote 인스턴스를 active로 전환해도 ETL은 항상 runner 인스턴스에서 실행된다. runner 인스턴스를 교체할 경우 워크플로를 한 번 재실행해 cron을 다시 깔아야 한다.

## Load Testing (k6)

k6는 로컬이 아닌 **원격 서버에 설치**되어 있다. k6 명령어는 반드시 원격 서버에 SSH 접속 후 실행해야 한다.

```bash
# 원격 서버에서 실행
k6 run \
  -e BASE_URL=https://api.safetrip.world \
  k6/smoke_test.js
```

## Python Coding Style (crawler/)

엄격한 규칙이 아닌, 읽기 좋은 코드를 위한 기준입니다.

### 주석 (PEP 8 + PEP 20)

```python
# 좋음 — 코드만 봐서 알 수 없는 의도를 설명
post.comments.replace_more(limit=0)  # MoreComments 객체 제거 (Reddit API lazy loading 방지)

# 나쁨 — 코드를 그대로 반복
records.clear()  # records 초기화
```

- 코드 자체로 의도가 드러나면 주석 생략
- 인라인 주석(`#`)은 비즈니스 로직이나 외부 API 동작 등 맥락이 필요할 때만 사용

### Docstring (PEP 257)

```python
# 항상 """ 사용 (''' 금지)
# 함수/메서드 안 첫 줄에 위치
def enrich_location(self, ...) -> dict | None:
    """Location 정보를 DB에서 조회하여 ID를 반환한다.

    Returns:
        성공 시 {"country_id": 1, ...}, 실패 시 None
    """

# 모듈 docstring은 import 전 파일 최상단에
"""Extract Job - Reddit에서 데이터를 추출하는 Job."""
import time, sys
```

- 클래스와 public 메서드에는 docstring 작성
- 내부 구현이 명확한 private 메서드(`_`)는 생략 가능

### 네이밍

```python
# 변수/함수: snake_case
batch_docs = []
def submit_documents_in_batch(): ...

# 클래스: PascalCase
class TravelScamClassifier: ...

# 상수: UPPER_SNAKE_CASE
TOKEN_LIMIT = 100_000
```

### 타입 힌트

```python
# public 메서드에는 파라미터 + 반환 타입 명시
def process_batch_results(self) -> int: ...
def enrich_location(self, country: str | None, ...) -> dict | None: ...
```

### 기타

- logger에는 f-string 대신 `%` 포맷 사용
  - `logger.info("값: %s", val)` → 로그 비활성화 시 문자열 생성 자체를 건너뜀
  - `logger.info(f"값: {val}")` → 출력 여부와 무관하게 항상 문자열 생성
- 빈 줄: 클래스 내 메서드 사이 1줄, 최상위 함수/클래스 사이 2줄

## 코드 설명 규칙

BE(Spring Boot) 또는 Crawler(Python) 코드를 구현할 때,
구현 완료 후 각 레이어(Repository → Service → Controller 또는 해당 모듈)에서
무엇을 했는지 간단히 설명한다.

## OCI 사용 규칙

### 프로필
- 모든 `oci` 명령어는 반드시 `--profile ai-agent` 옵션을 사용할 것
- `DEFAULT` 프로필은 절대 사용 금지

### 실행 규칙
- `oci` 명령어 실행 전 반드시 나에게 먼저 보여주고 승인을 받을 것
- `DELETE`, `TERMINATE` 가 포함된 명령어는 실행 불가
- Bastion 생성 시 목적과 대상 인스턴스를 먼저 확인할 것

### 사용 가능한 작업
- Bastion 생성 및 세션 관리
- Compute 인스턴스 조회 및 모니터링
- MySQL HeatWave 조회 및 모니터링
- 메트릭(CPU, 메모리) 조회
