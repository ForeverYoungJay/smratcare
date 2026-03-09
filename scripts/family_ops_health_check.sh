#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-123456}"
WINDOW_HOURS="${WINDOW_HOURS:-24}"

echo "== Family 链路巡检 =="
echo "BASE_URL: $BASE_URL"
echo "ADMIN_USERNAME: $ADMIN_USERNAME"
echo "WINDOW_HOURS: $WINDOW_HOURS"
echo

if ! curl -s -o /dev/null --connect-timeout 2 "$BASE_URL/api/auth/me"; then
  echo "无法连接后端服务：$BASE_URL"
  echo "请先启动后端，再执行本脚本。"
  exit 1
fi

extract_token() {
  sed -n 's/.*"token":"\([^"]*\)".*/\1/p' | head -n 1
}

echo "[1/2] 登录管理端账号并获取 token"
login_resp="$(curl -sS -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}")"

token="$(printf '%s' "$login_resp" | extract_token)"
if [[ -z "$token" ]]; then
  echo "登录失败，未获取到 token。"
  echo "响应：$login_resp"
  exit 1
fi
echo "登录成功。"
echo

echo "[2/2] 拉取家属链路健康概览"
health_resp="$(curl -sS -X GET "$BASE_URL/api/admin/family/ops/health?hours=$WINDOW_HOURS" \
  -H "Authorization: Bearer $token")"

if command -v jq >/dev/null 2>&1; then
  printf '%s\n' "$health_resp" | jq .
else
  printf '%s\n' "$health_resp"
fi
