# ETL 파이프라인 아키텍처

## 전체 흐름

```
Reddit API
    ↓
[Extract Job]    Reddit 게시글 수집 → MongoDB raw_documents
    ↓
[Classify Job]   OpenAI Batch API로 여행 사기 여부 분류 → raw_documents.classification
    ↓
[Parse Job]      OpenAI Batch API로 구조화된 필드 추출 → MongoDB parsed_documents
    ↓
[Enrich Job]     지명 문자열 → DB ID 매핑 → parsed_documents 업데이트
    ↓
[Load Job]       MongoDB → MySQL 이전 (external_report 테이블)
```

---

## 주요 설계 결정

### 1. 각 Job을 독립 subprocess로 실행

`ETLOrchestrator`가 각 단계를 `python -m etl_jobs.<job>` subprocess로 실행한다.

```python
result = subprocess.run(
    [sys.executable, "-m", "etl_jobs.extract_job", "week"],
    capture_output=True, cwd=self.src_dir
)
```

**이유:**
- 한 Job이 메모리 누수나 예외로 죽어도 Orchestrator가 살아있어 다음 Job을 실행할 수 있다.
- 각 Job의 stdout/stderr가 분리되어 로그 추적이 명확하다.
- Job별로 독립 실행(`python -m etl_jobs.classify_job`)이 가능해 재시도나 디버깅이 쉽다.

### 2. OpenAI Batch API 사용

분류(Classify)와 파싱(Parse) 단계에서 동기 API 대신 Batch API를 사용한다.

**이유:**
- OpenAI Batch API는 동기 API 대비 50% 비용 절감 (공식 할인율)
- 수백~수천 개 문서를 한 번에 묶어 처리하므로 API 호출 횟수가 줄어든다.
- 24시간 completion window 안에서 비동기로 처리되므로 서버 리소스를 점유하지 않는다.

**구조:**
1. 문서 → JSONL 파일 생성 → OpenAI에 배치 제출 → `batch_id`를 MongoDB에 저장
2. Polling 루프에서 `batch_id`로 상태 확인 → 완료 시 결과 파싱 → MongoDB 저장

### 3. MongoDB → MySQL 2단계 저장소

처리 중 데이터는 MongoDB에, 최종 서비스 데이터는 MySQL에 저장한다.

```
MongoDB (임시/처리 중)           MySQL (최종/서비스용)
─────────────────────           ──────────────────────
raw_documents                   external_report
parsed_documents       Load →   scam_action
batch_jobs                      scam_context
                                countries / states / cities
```

**이유:**
- OpenAI 배치 처리 중 실패하면 MongoDB의 중간 결과를 보존한 채 재시도 가능
- 파싱 결과의 스키마가 유동적인 초기 단계에서 MongoDB의 유연한 문서 구조가 유리
- MySQL은 Spring Boot API가 JPA로 서빙하는 최종 데이터만 담당

### 4. Dependency Injection Container

`config/dependencies.py`의 `Container` 클래스가 모든 의존성을 관리한다.

```python
class Container:
    @cached_property
    def transformer(self) -> TravelScamTransformer:
        return TravelScamTransformer(
            travel_scam_classifier=self.classifier,
            ...
        )

container = Container()  # 모듈 레벨 싱글톤
```

**이유:**
- `@cached_property`로 인스턴스를 처음 접근 시 한 번만 생성(lazy singleton)
- 테스트 시 `container`의 속성을 교체해 의존성 주입 가능
- 외부 라이브러리(FastAPI의 Depends 등) 없이 Python 표준으로 구현

### 5. Location Enrichment 다단계 매칭

LLM이 추출한 지명 문자열을 DB의 ID로 매핑하는 단계에서 4단계 전략을 사용한다.

```
1단계: Exact match     "seoul" == "seoul"
    ↓ 실패
2단계: Swap match      (state, city) ↔ (city, state) 교환 시도
    ↓ 실패
3단계: Remap match     country만 있을 때 state/city 필드로 재배치 시도
    ↓ 실패
4단계: Fuzzy match     RapidFuzz 유사도 + population 기반 최우선 후보 선택
```

**Fuzzy match 보조 전략:**
- `name`과 `native`(현지어) 필드 모두 비교
- 부분 일치 → fuzzy 유사도 순으로 시도
- 후보가 여러 개면 인구수가 많은 쪽 선택 (관광지 가능성이 높다는 가정)

---

## 디렉토리 구조

```
crawler/src/
├── etl_orchestrator.py         # Job 순차 실행 (subprocess)
├── init_etl_pipeline.py        # 초기 전체 적재 진입점
├── daily_etl_pipeline.py       # 일별 증분 적재 진입점
├── etl_jobs/                   # 각 단계별 Job (독립 실행 단위)
│   ├── extract_job.py
│   ├── classify_job.py
│   ├── parse_job.py
│   ├── enrich_location_job.py
│   ├── load_job.py
│   └── cli/job_argparsers.py
├── collector/
│   ├── extractor/              # Reddit 데이터 수집
│   ├── transformer/            # 분류/파싱/위치보강 로직
│   └── loader/                 # MongoDB → MySQL 이전
├── adapters/                   # 외부 서비스 연결 (MongoDB, Reddit, OpenAI)
├── repository/                 # 데이터 접근 계층
├── config/
│   ├── dependencies.py         # DI Container
│   ├── etl_config.py           # 설정값 (배치 크기, 타임아웃 등)
│   └── logging_config.py
└── utils/                      # 공통 유틸 (토큰 계산, 배치 처리, fuzzy 매칭 등)
```
