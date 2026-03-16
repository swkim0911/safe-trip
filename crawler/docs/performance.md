# 성능 개선 기록

## 1. Location Enrichment N+1 쿼리 제거

### 문제

`TravelScamEnricher._try_fuzzy()`는 Exact/Swap/Remap 매칭이 모두 실패했을 때 호출되는 마지막 단계다.
이 경로에서 문서 하나를 처리할 때마다 MongoDB를 반복 조회했다.

```
문서 1 처리:
  get_all_countries()              ← 전체 국가 조회
  get_cities_by_country_id(KR)     ← 한국 도시 전체 조회
  get_states_by_country_id(KR)     ← 한국 주(state) 전체 조회

문서 2 처리 (같은 나라):
  get_all_countries()              ← 동일한 쿼리 재실행
  get_cities_by_country_id(KR)     ← 동일한 쿼리 재실행
  get_states_by_country_id(KR)     ← 동일한 쿼리 재실행
```

N개 문서, K개 고유 국가가 있을 때 쿼리 수: **O(N)**

### 개선 방법

`TravelScamEnricher`에 lazy 캐시 3개를 추가했다.

```python
self._countries_cache = None    # list[country_doc], 최초 1회 로드
self._states_cache = {}         # country_id → list[state_doc], 국가당 1회 로드
self._cities_cache = {}         # country_id → list[city_doc], 국가당 1회 로드
```

- 전체 cities를 한꺼번에 올리지 않고 **조회된 국가 단위로 캐싱**
- 동일 국가의 문서를 처리할 때 DB 조회 없이 메모리에서 직접 반환

개선 후 쿼리 수: `get_all_countries()` 1회 + `get_*_by_country_id()` 고유 국가 수만큼

### 효과

여행 사기 데이터는 특정 국가(동남아, 유럽 등)에 집중되는 특성이 있다.
100개 문서가 10개 국가에 걸쳐 있다고 가정하면:

| 항목 | 개선 전 | 개선 후 |
|------|---------|---------|
| `get_all_countries()` 호출 수 | 100회 | 1회 |
| `get_cities_by_country_id()` 호출 수 | 100회 | 최대 10회 |
| `get_states_by_country_id()` 호출 수 | 100회 | 최대 10회 |
| **총 쿼리 수** | **~300회** | **~21회** |

---

## 2. 배치 제출 로직 중복 제거

### 문제

`classify_raw_documents_in_batch()`와 `parse_classified_documents_in_batch()`가
동일한 토큰 계산 → 배치 누적 → flush 패턴을 각각 ~30줄씩 구현하고 있었다.

```python
# classify 메서드
for doc in unclassified_docs:
    token_count = estimate_classification_request_tokens(body)
    if token_count > TOKEN_LIMIT: continue
    if expected_tokens + token_count > TOKEN_LIMIT:
        submit_batch(batch_docs)  # flush
        batch_docs = [batch_doc]
    ...

# parse 메서드 (동일한 구조 반복)
for doc in unparsed_docs:
    token_count = estimate_parsing_request_tokens(body)
    if token_count > TOKEN_LIMIT: continue
    if expected_tokens + token_count > TOKEN_LIMIT:
        submit_batch(batch_docs)  # flush
        batch_docs = [batch_doc]
    ...
```

### 개선 방법

공통 헬퍼 `_submit_batches(docs, token_estimator, batch_label, submit_fn)`를 추출했다.

```python
# classify
self._submit_batches(
    docs=unclassified_docs,
    token_estimator=estimate_classification_request_tokens,
    batch_label="classification",
    submit_fn=self.travel_scam_classifier.submit_classification_batch,
)

# parse
self._submit_batches(
    docs=unparsed_docs,
    token_estimator=estimate_parsing_request_tokens,
    batch_label="parsing",
    submit_fn=self.travel_scam_parser.submit_parsing_batch,
)
```

향후 새로운 배치 job이 추가될 때 동일한 인터페이스로 재사용 가능하다.

---

## 3. Polling 로직 공통화

### 문제

`classify_job.py`와 `parse_job.py`가 배치 완료를 대기하는 while 루프를 각각 ~25줄씩 복붙하고 있었다.

### 개선 방법

`utils/batch_utils.py`에 `wait_for_batch()` 유틸 함수를 추가하고, 두 job이 이를 공유하도록 했다.

```python
wait_for_batch(
    process_fn=transformer.process_classification_batch_results,
    count_fn=transformer.get_unprocessed_batch_count,
    batch_type="classification",
    poll_interval=etl_config.BATCH_POLL_INTERVAL,
    max_wait_time=etl_config.BATCH_MAX_WAIT_TIME,
    logger=logger,
)
```
