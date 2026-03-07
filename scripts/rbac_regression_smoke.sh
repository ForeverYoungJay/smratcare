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
    echo "❌ $title -> 期望 $expected，实际 $actual"
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
node --input-type=module <<'NODE'
import { hasRouteAccess } from './admin-web/src/utils/roleAccess.js'

const cases = [
  { title: '护理员工可访问护理员工路由', roles: ['NURSING_EMPLOYEE'], required: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], path: '/care/caregiver-info', expected: true },
  { title: '护理员工不可访问护理管理路由', roles: ['NURSING_EMPLOYEE'], required: ['ADMIN'], path: '/care/care-packages', expected: false },
  { title: '护理部长可访问护理管理路由', roles: ['NURSING_MINISTER'], required: ['ADMIN'], path: '/care/care-packages', expected: true },
  { title: '财务员工不可访问营销模块', roles: ['FINANCE_EMPLOYEE'], required: ['MARKETING_EMPLOYEE', 'MARKETING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], path: '/marketing/workbench', expected: false },
  { title: '市场员工可访问营销模块', roles: ['MARKETING_EMPLOYEE'], required: ['MARKETING_EMPLOYEE', 'MARKETING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], path: '/marketing/workbench', expected: true }
]

for (const c of cases) {
  const actual = hasRouteAccess(c.roles, c.required, c.path)
  if (actual !== c.expected) {
    console.error(`❌ ${c.title} -> expected=${c.expected}, actual=${actual}`)
    process.exit(1)
  }
  console.log(`✅ ${c.title}`)
}
NODE
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

status="$(api_status "$finance_emp_token" "PUT" "/api/finance/fee/admission-audit/1/review" '{"status":"APPROVED","remark":"smoke"}')"
assert_status_eq "$status" "403" "财务员工不可执行费用审核(review)"

status="$(api_status "$finance_minister_token" "PUT" "/api/finance/fee/admission-audit/1/review" '{"status":"APPROVED","remark":"smoke"}')"
assert_not_403 "$status" "财务部长可执行费用审核(review)"

status="$(api_status "$nursing_emp_token" "GET" "/api/nursing/service-plans/page?pageNo=1&pageSize=1")"
assert_status_eq "$status" "403" "护理员工不可查看护理服务计划管理页"

status="$(api_status "$nursing_minister_token" "GET" "/api/nursing/service-plans/page?pageNo=1&pageSize=1")"
assert_not_403 "$status" "护理部长可查看护理服务计划管理页"

status="$(api_status "$marketing_emp_token" "POST" "/api/marketing/plans" '{}')"
assert_status_eq "$status" "403" "市场员工不可创建营销计划"

status="$(api_status "$marketing_minister_token" "POST" "/api/marketing/plans" '{}')"
assert_not_403 "$status" "市场部长可创建营销计划"

status="$(api_status "$finance_emp_token" "GET" "/api/marketing/plans/page?pageNo=1&pageSize=1")"
assert_status_eq "$status" "403" "财务员工不可跨部门访问营销计划列表"

status="$(api_status "$marketing_emp_token" "GET" "/api/marketing/plans/page?pageNo=1&pageSize=1")"
assert_not_403 "$status" "市场员工可访问营销计划列表"

echo
echo "== RBAC 回归冒烟通过 =="
