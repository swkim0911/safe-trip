#!/bin/bash
set -e

export PATH="$PATH:/home/ubuntu/bin"

# ── 설정 ────────────────────────────────────────────────────────────
# RUNNER_IP / REMOTE_IP 는 인스턴스 정체성(고정), ACTIVE_IP / STANDBY_IP 는
# LB 상태에 따라 매 배포마다 결정되는 역할(Blue-Green 표준 의미).
LB_ID="ocid1.loadbalancer.oc1.ap-chuncheon-1.aaaaaaaaboowr4zz4smwzpjuwcimjkd7nbaxbtbinqdg6ohbem5c4hyx46pq"
BACKEND_SET="bs_lb_2026-0401-1645"
RUNNER_IP="${RUNNER_IP:?RUNNER_IP variable is not set}"   # self-hosted runner가 떠있는 인스턴스 (고정)
REMOTE_IP="${REMOTE_IP:?REMOTE_IP variable is not set}"   # SSH 대상 인스턴스 (고정)
SSH_KEY="$HOME/.ssh/id_rsa"
DEPLOY_DIR="/home/ubuntu/safe-trip"
HEALTH_URL="http://localhost:8080/actuator/health"
OCI_PROFILE="DEFAULT"

# ── 현재 active/standby 판별 ────────────────────────────────────────
RUNNER_OFFLINE=$(oci lb backend get \
  --load-balancer-id "$LB_ID" \
  --backend-set-name "$BACKEND_SET" \
  --backend-name "$RUNNER_IP:8080" \
  --profile "$OCI_PROFILE" \
  --query 'data.offline' \
  --raw-output)

if [ "$RUNNER_OFFLINE" = "false" ]; then
  ACTIVE_IP="$RUNNER_IP"
  STANDBY_IP="$REMOTE_IP"
  STANDBY_NAME="$REMOTE_IP:8080"
  ACTIVE_NAME="$RUNNER_IP:8080"
else
  ACTIVE_IP="$REMOTE_IP"
  STANDBY_IP="$RUNNER_IP"
  STANDBY_NAME="$RUNNER_IP:8080"
  ACTIVE_NAME="$REMOTE_IP:8080"
fi

echo "▶ Active: $ACTIVE_IP / Standby: $STANDBY_IP"

# ── Standby에 새 버전 배포 ────────────────────────────────────────────
echo "▶ Standby($STANDBY_IP)에 새 버전 배포 중..."

if [ "$STANDBY_IP" = "$RUNNER_IP" ]; then
  # standby가 자기 자신 (runner 인스턴스)
  mkdir -p "$DEPLOY_DIR"
  cp "$(dirname "$0")/../docker-compose.prod.yml" "$DEPLOY_DIR/"
  cd "$DEPLOY_DIR"
  docker compose -f docker-compose.prod.yml pull backend
  docker compose -f docker-compose.prod.yml up -d backend
else
  # standby가 원격 인스턴스
  ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "ubuntu@$STANDBY_IP" bash <<EOF
    cd $DEPLOY_DIR
    docker compose -f docker-compose.prod.yml pull backend
    docker compose -f docker-compose.prod.yml up -d backend
EOF
fi

# ── 헬스체크 ────────────────────────────────────────────────────────
echo "▶ Standby($STANDBY_IP) 헬스체크 중..."
for i in $(seq 1 12); do
  if [ "$STANDBY_IP" = "$RUNNER_IP" ]; then
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" || echo "000")
  else
    STATUS=$(ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "ubuntu@$STANDBY_IP" \
      "curl -s -o /dev/null -w '%{http_code}' $HEALTH_URL" || echo "000")
  fi

  if [ "$STATUS" = "200" ]; then
    echo "✅ 헬스체크 통과 (${i}회차)"
    break
  fi

  echo "  대기 중... ($i/12, status=$STATUS)"
  sleep 10

  if [ "$i" = "12" ]; then
    echo "❌ 헬스체크 실패 — 롤백"
    exit 1
  fi
done

# ── OCI LB 스위칭 ────────────────────────────────────────────────────
echo "▶ LB 스위칭: Standby($STANDBY_IP) → Online, Active($ACTIVE_IP) → Offline"

oci lb backend update \
  --load-balancer-id "$LB_ID" \
  --backend-set-name "$BACKEND_SET" \
  --backend-name "$STANDBY_NAME" \
  --weight 1 \
  --offline false \
  --backup false \
  --drain false \
  --profile "$OCI_PROFILE" \
  --wait-for-state SUCCEEDED

oci lb backend update \
  --load-balancer-id "$LB_ID" \
  --backend-set-name "$BACKEND_SET" \
  --backend-name "$ACTIVE_NAME" \
  --weight 1 \
  --offline true \
  --backup false \
  --drain false \
  --profile "$OCI_PROFILE" \
  --wait-for-state SUCCEEDED

# ── 기존 Active 컨테이너 종료 ────────────────────────────────────────
echo "▶ Active($ACTIVE_IP) 컨테이너 종료 중..."

if [ "$ACTIVE_IP" = "$RUNNER_IP" ]; then
  cd "$DEPLOY_DIR"
  docker compose -f docker-compose.prod.yml stop backend
else
  ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "ubuntu@$ACTIVE_IP" \
    "docker compose -f $DEPLOY_DIR/docker-compose.prod.yml stop backend"
fi

echo "✅ Blue-Green 배포 완료"
