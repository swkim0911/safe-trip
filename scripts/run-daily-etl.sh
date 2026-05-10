#!/bin/bash
# Daily ETL wrapper — ETL을 실행하고 실패 시 Slack 웹훅으로 알림.
# cron이 직접 docker compose를 부르지 않고 이 스크립트를 통해 호출하게 한다.
# set -e 를 켜지 않는 이유: docker 종료 코드를 받아 알림 후 그대로 반환해야 하기 때문.
set -uo pipefail

DEPLOY_DIR="/home/ubuntu/safe-trip"
LOG_FILE="$DEPLOY_DIR/crawler.log"
ALERTS_ENV="$DEPLOY_DIR/.env.alerts"

cd "$DEPLOY_DIR"

# SLACK_WEBHOOK_URL 등 알림 설정 로드 (없으면 알림만 스킵, ETL은 그대로 실행)
if [ -f "$ALERTS_ENV" ]; then
  # shellcheck source=/dev/null
  . "$ALERTS_ENV"
fi

START_TIME=$(date -u +%FT%TZ)
echo "============================================================"
echo "[$START_TIME] daily ETL 시작"
echo "============================================================"

docker compose -f docker-compose.prod.yml run --rm crawler python src/daily_etl_pipeline.py
EXIT_CODE=$?

END_TIME=$(date -u +%FT%TZ)

if [ "$EXIT_CODE" -eq 0 ]; then
  echo "[$END_TIME] daily ETL 성공"
  exit 0
fi

echo "[$END_TIME] ❌ daily ETL 실패 (exit=$EXIT_CODE)" >&2

if [ -z "${SLACK_WEBHOOK_URL:-}" ]; then
  echo "SLACK_WEBHOOK_URL 미설정 — 알림 건너뜀" >&2
  exit "$EXIT_CODE"
fi

LAST_LOG=$(tail -30 "$LOG_FILE" 2>/dev/null || echo "(로그 파일 없음)")

# 로그/메시지에 따옴표·개행이 들어가도 안전하게 JSON 직렬화 (python3로 escape).
PAYLOAD=$(LAST_LOG="$LAST_LOG" START_TIME="$START_TIME" END_TIME="$END_TIME" EXIT_CODE="$EXIT_CODE" python3 <<'PYEOF'
import json, os
text = (
    "🚨 SafeTrip Daily ETL 실패\n"
    "시작: " + os.environ["START_TIME"] + "\n"
    "종료: " + os.environ["END_TIME"] + "\n"
    "exit=" + os.environ["EXIT_CODE"] + "\n\n"
    "로그 마지막 30줄:\n```\n"
    + os.environ["LAST_LOG"]
    + "\n```"
)
print(json.dumps({"text": text}))
PYEOF
)

curl -sS -X POST -H "Content-Type: application/json" \
  --data "$PAYLOAD" \
  "$SLACK_WEBHOOK_URL" \
  || echo "Slack 알림 전송 실패" >&2

exit "$EXIT_CODE"
