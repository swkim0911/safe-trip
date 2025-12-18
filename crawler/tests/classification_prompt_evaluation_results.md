# Classification Prompt 평가 결과

## 평가 개요

- **평가일**: 2024년 12월 1일 15:01
- **테스트 샘플 수**: 30개
- **평가 방법**: OpenAI Batch API
- **비교 버전**: v1_baseline vs v2_few_shot

---

## 전체 결과 요약

| 버전 | 정확도 | 정답 수 | 실행 시간 |
|------|--------|---------|-----------|
| v1_baseline | 93.3% (28/30) | 28 | 122.4초 |
| **v2_few_shot** ⭐ | **100.0% (30/30)** | **30** | 244.0초 |

### 주요 특징

- **개선**: v1 대비 v2(few-shot) 정확도 개선 (93.3% → 100%)
-  **속도**: v1이 121.6초 더 빠름 (하지만 정확도가 우선)

---

## 카테고리별 정확도

### v1_baseline

| 카테고리                                       | 정확도          | 정답/전체     |
|--------------------------------------------|--------------|-----------|
| **clear_negative**                         | **95.8%** ⚠️ | **23/24** |
| clear_positive                             | 100.0%       | 2/2       |
| just incorrect information                 | 100.0%       | 1/1       |
| lacks_context and there is no location     | 100.0%       | 1/1       |
| clear_negative. it's about safety tips | **0%** ⚠️    | 0/1       |
| clear_negative and lacks_context           | 100.0%       | 1/1       |

**약점**: 
- 기부 권유를 사기로 오분류 (실제로는 사기가 아님)
- 경험 내용이 짧고 조언 글을 positive로 분류

### v2_few_shot

| 카테고리                                   | 정확도 | 정답/전체 |
|----------------------------------------|--------|-----------|
| clear_negative                         | **100.0%** ✅ | 24/24 |
| clear_positive                         | **100.0%** ✅ | 2/2 |
| just incorrect information             | **100.0%** ✅ | 1/1 |
| lacks_context and there is no location | **100.0%** ✅ | 1/1 |
| clear_negative. it's about safety tips | **100.0%** ✅ | 1/1 |
| clear_negative and lacks_context       | **100.0%** ✅ | 1/1 |

---

## 오류 분석

### v1_baseline 오류 (2건)

#### 오류 1: donation 권유를 사기로 오분류


```
예상: 0 (사기 아님)
예측: 1 (사기)

본문 요약 내용:
"Little kids in Berlin making you sign something for a 'donation', then 
telling you the minimum donation is 20 euros or something like that."
```

**분석**: 
- 기부 요청을 사기로 잘못 판단
- 실제로는 귀찮게 하지만 사기는 아닐 수 있음
- 맥락 없이 키워드("money")만으로 판단한 문제

#### 오류 2: 여행 조언을 경험으로 오분류

```
카테고리: clear_negative. it's about safety tips
예상: 0 (사기 아님)
예측: 1 (사기)

본문 요약 내용:
"I had many of these same experiences living in Fes, Morocco for three months 
in 2010. Groping, stalking, theft. I was even refused the right to keep m..."
```

**분석**:
- 조언과 경험을 구분하지 못하는 문제
- Zero-shot의 근본적 한계

---


## 버전별 특징 비교

### v1_baseline (Zero-shot)

**장점**:
- ✅ 빠른 처리 시간 (122초, v2 대비 50% 빠름)
- ✅ 토큰 사용량 적음 (비용 절감)

**단점**:
- ❌ 정확도 93.3% (2건 오류)
- ❌ 조언과 경험을 구분하지 못함
- ❌ 맥락 이해 부족

**결론**: 속도는 빠르지만 정확도가 부족함

---

### v2_few_shot (권장)

**장점**:
- ✅ **완벽한 정확도 100%** (30/30)
- ✅ 애매한 글을 명확히 구분 (조언 vs 경험)

**단점**:
- ⚠️ 처리 시간 2배 (244초 vs 122초)
- ⚠️ 토큰 사용량 증가

**결론**: 정확도가 최우선이므로 **v2_few_shot을 production에 배포**

---

## 실행 로그

### v1_baseline
```
테스트 샘플: 30개
배치 제출 완료
대기 시간: 120초
최종 정확도: 93.3% (28/30)
오류 수: 2건
```

### v2_few_shot
```
테스트 샘플: 30개
배치 제출 완료
대기 시간: 240초
최종 정확도: 100.0% (30/30) ✅
오류 수: 0건
```
---

## 비용 분석

### 토큰 사용량 추정 - GPT-4o mini
응답은 0과 1로 통일하니 요청 토큰만 비교

| 버전 | 요청 토큰 | 1개 요청당 비용  |
|------|-----------|------------|
| v1_baseline | 391 tokens | $0.0000293 |
| v2_few_shot | 732 tokens | $0.0000549 |

**차이**: v2가 v1보다 약 2배 더 비쌈

---

## 결론
v1_prompt -> v2_prompt 교체

- ✅ 100% 정확도 (30/30)
- ✅ 비용 대비 효과 우수
