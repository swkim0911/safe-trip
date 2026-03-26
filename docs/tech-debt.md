# Tech Debt & Known Issues

미구현·미처리 상태로 남긴 기술적 결정 사항을 기록한다.
배포 전 또는 트래픽 증가 시 우선 검토할 항목들.

---

## 좋아요 (Like) — 안정성

### 1. FE 중복 클릭 방지 미구현

**현상**: 좋아요 버튼을 빠르게 여러 번 클릭하면 API 요청이 중복으로 발생한다.
낙관적 업데이트로 UI는 즉시 토글되지만, 동시에 여러 요청이 서버로 날아가면
서버/클라이언트 상태가 어긋날 수 있다.

**해결 방법**: `useComment.js`의 `toggleLike()`에 `isLiking` 플래그 추가해서
in-flight 중 버튼 비활성화.

```js
// useComment.js
const isLiking = ref(false)

async function toggleLike(commentId) {
  if (isLiking.value) return
  isLiking.value = true
  // ...
  isLiking.value = false
}
```

**우선순위**: Medium (UX 문제, 데이터 정합성에도 영향)

---

### 2. BE likes 테이블 UNIQUE constraint 미적용

**현상**: 동일 유저가 동시에 두 요청을 보내면 두 요청 모두 "기존 likes 없음"으로 판단해
`INSERT`를 두 번 날릴 수 있다. 결과적으로 `(user_id, target_id, target_type)` 조합이
중복 저장되어 좋아요 취소가 불가능해진다.

**해결 방법**: DB UNIQUE constraint 추가.

```sql
ALTER TABLE likes
  ADD CONSTRAINT uq_likes_user_target
  UNIQUE (user_id, target_id, target_type);
```

애플리케이션에서는 중복 시 발생하는 `DataIntegrityViolationException`을 잡아서
재조회 후 토글 처리하거나, 409로 응답.

**우선순위**: High (데이터 정합성 문제)

---

### 3. likeCnt 동시성 문제 (Lost Update)

**현상**: 현재 `comment.incrementLikeCnt()` 방식은 JPA로 엔티티를 읽어 `+1` 후 저장하므로,
동시 요청 시 한쪽 업데이트가 유실될 수 있다.

```
User A: read(likeCnt=5) → +1 → write(6)
User B: read(likeCnt=5) → +1 → write(6)  // A의 +1이 유실
```

**해결 방법**: SQL 레벨 원자적 연산 사용.

```sql
UPDATE comment SET like_cnt = like_cnt + 1 WHERE id = ?
```

Spring Data JPA에서는 `@Modifying @Query`로 처리 가능.

**우선순위**: Low (현재 트래픽 규모에서는 발생 가능성 낮음. 서비스 규모 성장 시 재검토)

---

## 댓글 (Comment) — UX

### 4. 댓글 작성 시 모달 번쩍임

**현상**: 댓글 작성·수정·삭제 후 `fetchComments`로 전체 재조회하여 모달이 번쩍인다.
좋아요는 낙관적 업데이트로 해결했지만 댓글은 서버에서 `id`, `createdAt` 등을
받아야 해서 그대로 두었다.

**해결 방법**: 서버 응답에서 새 댓글 객체를 받아 로컬 목록에 추가하는 낙관적 업데이트.
실패 시 제거.

**우선순위**: Low (댓글 작성은 반복 동작이 아니라 번쩍여도 큰 불편함 없음)

---

---

## 댓글 createdAt 타임존 처리 ✅ 해결됨

**현상**: `CommentItem.createdAt`이 `LocalDateTime`(타임존 없음)으로 직렬화되어
`"2026-03-25T05:16:21.221375"` 형태로 응답. 로컬 개발 환경(JVM KST, 브라우저 KST)에서는
정상 동작하지만, EC2/Docker 배포 시 컨테이너가 UTC로 실행되어 브라우저가
UTC 시간을 KST 로컬로 해석 → 댓글 시간이 9시간 미래로 보여 항상 "just now" 표시.

**해결**: `LocalDateTime` → `OffsetDateTime`으로 변경, `atOffset(ZoneOffset.UTC)` 적용.
응답이 `"2026-03-25T05:16:21Z"` 형태로 오면 브라우저가 UTC로 정확히 해석하여
KST로 자동 변환.

**수정 파일**:
- `CommentItem.java` — `LocalDateTime` → `OffsetDateTime`
- `CommentService.toItem()` — `comment.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime()`

**교훈**: `LocalDateTime`은 타임존 정보가 없어 서버/클라이언트 환경 차이에 취약.
API 응답에서 날짜/시간 필드는 `OffsetDateTime` 또는 `Instant`를 사용해야 함.

---

*마지막 업데이트: 2026-03-25*
