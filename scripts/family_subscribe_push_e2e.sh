#!/usr/bin/env bash
set -euo pipefail

# 家属端订阅消息推送链路端到端验证脚本。
# 用管理端账号登录后，调用 /api/admin/family/ops/test-notify 为指定家属真实投递一条
# 订阅消息，并打印落库结果（SUCCESS/SKIPPED/FAILED + 错误原因），用于上线前核验
# access_token / openId 绑定 / 模板配置是否打通。
#
# 用法：
#   FAMILY_USER_ID=123 ./scripts/family_subscribe_push_e2e.sh
# 可选环境变量：
#   BASE_URL(默认 http://localhost:8080) ADMIN_USERNAME ADMIN_PASSWORD
#   ELDER_ID EVENT_TYPE LEVEL TITLE CONTENT PAGE_PATH

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-123456}"
FAMILY_USER_ID="${FAMILY_USER_ID:-}"
ELDER_ID="${ELDER_ID:-}"
EVENT_TYPE="${EVENT_TYPE:-GENERAL}"
LEVEL="${LEVEL:-normal}"
TITLE="${TITLE:-推送链路自测}"
CONTENT="${CONTENT:-这是一条家属端订阅消息推送链路的端到端测试通知。}"
PAGE_PATH="${PAGE_PATH:-pages/messages/index}"

echo "== 家属端订阅消息推送链路 E2E =="
echo "BASE_URL: $BASE_URL"
echo "FAMILY_USER_ID: ${FAMILY_USER_ID:-<未指定>}"
echo

if [[ -z "$FAMILY_USER_ID" ]]; then
  echo "请通过 FAMILY_USER_ID 指定要测试推送的家属账号 ID。"
  echo "例如：FAMILY_USER_ID=123 $0"
  exit 1
fi

if ! curl -s -o /dev/null --connect-timeout 2 "$BASE_URL/api/auth/me"; then
  echo "无法连接后端服务：$BASE_URL，请先启动后端。"
  exit 1
fi

extract_token() {
  sed -n 's/.*"token":"\([^"]*\)".*/\1/p' | head -n 1
}

echo "[1/2] 登录管理端账号"
login_resp="$(curl -sS -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}")"
token="$(printf '%s' "$login_resp" | extract_token)"
if [[ -z "$token" ]]; then
  echo "登录失败：$login_resp"
  exit 1
fi
echo "登录成功。"
echo

echo "[2/2] 触发测试推送并读取投递结果"
payload="$(cat <<JSON
{
  "familyUserId": $FAMILY_USER_ID,
  ${ELDER_ID:+\"elderId\": $ELDER_ID,}
  "eventType": "$EVENT_TYPE",
  "level": "$LEVEL",
  "title": "$TITLE",
  "content": "$CONTENT",
  "pagePath": "$PAGE_PATH"
}
JSON
)"

resp="$(curl -sS -X POST "$BASE_URL/api/admin/family/ops/test-notify" \
  -H "Authorization: Bearer $token" \
  -H 'Content-Type: application/json' \
  -d "$payload")"

if command -v jq >/dev/null 2>&1; then
  printf '%s\n' "$resp" | jq .
else
  printf '%s\n' "$resp"
fi
