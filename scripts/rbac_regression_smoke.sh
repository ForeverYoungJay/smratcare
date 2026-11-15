#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${BASE_URL:-http://localhost:8080}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

echo "== RBAC 回归冒烟开始 =="
echo "BASE_URL: $BASE_URL"
echo

if ! curl -s -o /dev/null --connect-timeout 2 "$BASE_URL/api/auth/me"; then
  echo "❌ 无法连接后端服务：$BASE_URL"
  echo "请先启动后端（例如 docker compose up -d），再执行本脚本。"
  exit 1
fi

extract_token() {
  sed -n 's/.*"token":"\([^"]*\)".*/\1/p' | head -n 1
}

login_token() {
  local username="$1"
  local password="${2:-123456}"
  local resp token
  if ! resp="$(curl -sS -X POST "$BASE_URL/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}")"; then
    echo "登录请求失败: $username"
    return 1
  fi
  token="$(printf '%s' "$resp" | extract_token)"
  if [[ -z "$token" ]]; then
    echo "登录失败: $username"
    echo "响应: $resp"
    return 1
  fi
  printf '%s' "$token"
}

api_status() {
  local token="$1"
  local method="$2"
  local path="$3"
  local body="${4:-}"
  local out_file="$TMP_DIR/resp_$(date +%s%N).json"
  if [[ -n "$body" ]]; then
    curl -sS -o "$out_file" -w '%{http_code}' -X "$method" "$BASE_URL$path" \
      -H 'Content-Type: application/json' \
      -H "Authorization: Bearer $token" \
      -d "$body"
  else
    curl -sS -o "$out_file" -w '%{http_code}' -X "$method" "$BASE_URL$path" \
      -H "Authorization: Bearer $token"
  fi
}

assert_status_eq() {
  local actual="$1"
  local expected="$2"
  local title="$3"
  if [[ "$actual" != "$expected" ]]; then
    echo "❌ $title -> 期望 ${expected}，实际 ${actual}"
    exit 1
  fi
  echo "✅ $title -> $actual"
}

assert_not_403() {
  local actual="$1"
  local title="$2"
  if [[ "$actual" == "403" ]]; then
    echo "❌ $title -> 不应为 403"
    exit 1
  fi
  echo "✅ $title -> $actual"
}

echo "[1/3] 菜单/路由权限规则静态断言"
(cd admin-web && npm run test:unit -- --run src/utils/routeAccess.test.ts >/dev/null)
echo "✅ 路由权限单元测试通过"
echo

echo "[2/3] 获取回归账号 token"
finance_emp_token="$(login_token finance_emp)"
finance_minister_token="$(login_token finance_minister)"
nursing_emp_token="$(login_token nursing_emp)"
nursing_minister_token="$(login_token nursing_minister)"
marketing_emp_token="$(login_token marketing_emp)"
marketing_minister_token="$(login_token marketing_minister)"
echo "✅ 6 个回归账号登录成功"
echo

echo "[3/3] 核心接口 403/200 回归断言"

status="$(api_status "$finance_emp_token" "PUT" "/api/finance/fee/admission-audit/-1/review" '{"status":"APPROVED","remark":"smoke"}')"
assert_status_eq "$status" "403" "财务员工不可执行费用审核(review)"

status="$(api_status "$finance_minister_token" "PUT" "/api/finance/fee/admission-audit/-1/review" '{"status":"APPROVED","remark":"smoke"}')"
assert_not_403 "$status" "财务部长可执行费用审核(review)"

status="$(api_status "$nursing_emp_token" "GET" "/api/nursing/service-plans/page?pageNo=1&pageSize=1")"
assert_not_403 "$status" "护理员工可查看护理服务计划"

status="$(api_status "$nursing_minister_token" "GET" "/api/nursing/service-plans/page?pageNo=1&pageSize=1")"
assert_not_403 "$status" "护理部长可查看护理服务计划管理页"

status="$(api_status "$nursing_emp_token" "GET" "/api/nursing/care-levels/list?enabled=1")"
assert_not_403 "$status" "护理员工可查看护理等级"

status="$(api_status "$marketing_emp_token" "GET" "/api/bed/map?includeRisk=false")"
assert_not_403 "$status" "市场员工可只读查看床位房态"

risk_response="$(curl -sS -H "Authorization: Bearer $marketing_emp_token" "$BASE_URL/api/bed/map?includeRisk=true")"
if ! printf '%s' "$risk_response" | jq -e '.data | length > 0 and all(.[]; .riskLevel == null and .riskLabel == null and .riskSource == null and .abnormalVital24hCount == null and .latestAssessmentLevel == null and .latestAssessmentDate == null)' >/dev/null; then
  echo "❌ 市场员工不应读取护理风险数据"
  exit 1
fi
echo "✅ 市场员工无法通过 includeRisk=true 获取护理风险数据"

status="$(api_status "$marketing_emp_token" "PUT" "/api/bed/-1" '{"roomId":-1,"bedNo":"smoke"}')"
assert_status_eq "$status" "403" "市场员工不可修改床位"

status="$(api_status "$marketing_emp_token" "PUT" "/api/marketing/plans/-1" '{"moduleType":"SPEECH","title":"权限检查"}')"
assert_status_eq "$status" "403" "市场员工不可修改营销计划"

status="$(api_status "$marketing_minister_token" "PUT" "/api/marketing/plans/-1" '{"moduleType":"SPEECH","title":"权限检查"}')"
assert_not_403 "$status" "市场部长可进入营销计划修改接口"

status="$(api_status "$finance_emp_token" "GET" "/api/marketing/plans/page?pageNo=1&pageSize=1")"
assert_status_eq "$status" "403" "财务员工不可跨部门访问营销计划列表"

status="$(api_status "$marketing_emp_token" "GET" "/api/marketing/plans/page?pageNo=1&pageSize=1")"
assert_not_403 "$status" "市场员工可访问营销计划列表"

echo
echo "== RBAC 回归冒烟通过 =="
