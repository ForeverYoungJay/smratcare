#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${BASE_URL:-http://localhost:8080}"
FAMILY_PHONE="${FAMILY_PHONE:-}"
FAMILY_PASSWORD="${FAMILY_PASSWORD:-Family@123}"
FAMILY_NEW_PASSWORD="${FAMILY_NEW_PASSWORD:-Family@456}"
REGISTER_CODE="${REGISTER_CODE:-}"
RESET_CODE="${RESET_CODE:-}"
BIND_ID_CARD="${BIND_ID_CARD:-}"
MALL_PRODUCT_ID="${MALL_PRODUCT_ID:-}"
MALL_QTY="${MALL_QTY:-1}"
RUN_SUBMIT="${RUN_SUBMIT:-false}"
RUN_RESET="${RUN_RESET:-false}"
RUN_CANCEL="${RUN_CANCEL:-false}"
RUN_REFUND="${RUN_REFUND:-false}"

if ! command -v jq >/dev/null 2>&1; then
  echo "本脚本依赖 jq，请先安装 jq 后重试。"
  exit 1
fi

if [[ -z "$FAMILY_PHONE" ]]; then
  ts="$(date +%s)"
  suffix="$(printf "%08d" "$((ts % 100000000))")"
  FAMILY_PHONE="139${suffix}"
fi

request_json() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local token="${4:-}"
  local curl_args=(
    -sS
    -X "$method"
    "${BASE_URL}${path}"
    -H "Content-Type: application/json"
  )
  if [[ -n "$token" ]]; then
    curl_args+=(-H "Authorization: Bearer ${token}")
  fi
  if [[ -n "$body" ]]; then
    curl_args+=(-d "$body")
  fi
  curl "${curl_args[@]}"
}

assert_ok() {
  local resp="$1"
  local label="$2"
  local code
  code="$(printf '%s' "$resp" | jq -r '.code // empty')"
  if [[ "$code" != "0" ]]; then
    echo "[失败] ${label}"
    printf '%s\n' "$resp" | jq .
    exit 1
  fi
}

to_bool() {
  local raw="${1:-false}"
  [[ "$raw" == "true" || "$raw" == "1" || "$raw" == "yes" ]]
}

echo "== 家属账号 + 商城回归脚本 =="
echo "BASE_URL: $BASE_URL"
echo "FAMILY_PHONE: $FAMILY_PHONE"
echo "RUN_SUBMIT: $RUN_SUBMIT"
echo "RUN_RESET: $RUN_RESET"
echo "RUN_CANCEL: $RUN_CANCEL"
echo "RUN_REFUND: $RUN_REFUND"
echo

bootstrap_resp="$(request_json GET "/api/auth/family/bootstrap")"
assert_ok "$bootstrap_resp" "获取家属引导信息"
ORG_ID="$(printf '%s' "$bootstrap_resp" | jq -r '.data.orgId')"
ORG_NAME="$(printf '%s' "$bootstrap_resp" | jq -r '.data.orgName // ""')"
echo "[1/9] bootstrap 成功：orgId=${ORG_ID} orgName=${ORG_NAME}"

send_register_resp="$(request_json POST "/api/auth/family/sms-code/send" \
  "{\"orgId\":${ORG_ID},\"phone\":\"${FAMILY_PHONE}\",\"scene\":\"REGISTER\"}")"
assert_ok "$send_register_resp" "发送注册验证码"
if [[ -z "$REGISTER_CODE" ]]; then
  REGISTER_CODE="$(printf '%s' "$send_register_resp" | jq -r '.data.debugCode // empty')"
fi
if [[ -z "$REGISTER_CODE" ]]; then
  echo "[失败] 无法自动获取注册验证码。"
  echo "请通过环境变量 REGISTER_CODE 传入真实短信验证码后重试。"
  exit 1
fi
echo "[2/9] 注册验证码发送成功"

register_resp="$(request_json POST "/api/auth/family/register" \
  "{\"orgId\":${ORG_ID},\"phone\":\"${FAMILY_PHONE}\",\"verifyCode\":\"${REGISTER_CODE}\",\"password\":\"${FAMILY_PASSWORD}\",\"realName\":\"回归家属\"}")"
assert_ok "$register_resp" "注册家属账号"
echo "[3/9] 注册成功"

login_resp="$(request_json POST "/api/auth/family/login" \
  "{\"orgId\":${ORG_ID},\"phone\":\"${FAMILY_PHONE}\",\"password\":\"${FAMILY_PASSWORD}\"}")"
assert_ok "$login_resp" "账号密码登录"
FAMILY_TOKEN="$(printf '%s' "$login_resp" | jq -r '.data.token // empty')"
if [[ -z "$FAMILY_TOKEN" ]]; then
  echo "[失败] 登录成功但未返回 token"
  exit 1
fi
echo "[4/9] 账号密码登录成功"

security_verify_resp="$(request_json POST "/api/family/security/password/verify" \
  "{\"password\":\"${FAMILY_PASSWORD}\",\"scene\":\"SECURITY\"}" "$FAMILY_TOKEN")"
assert_ok "$security_verify_resp" "敏感操作密码二次验证"
SECURITY_PASSED="$(printf '%s' "$security_verify_resp" | jq -r '.data.passed')"
if [[ "$SECURITY_PASSED" != "true" ]]; then
  echo "[失败] 敏感操作密码验证未通过"
  printf '%s\n' "$security_verify_resp" | jq .
  exit 1
fi
echo "[5/9] 敏感操作密码验证通过"

bindings_resp="$(request_json GET "/api/family/bindings" "" "$FAMILY_TOKEN")"
assert_ok "$bindings_resp" "查询家属绑定"
BIND_COUNT="$(printf '%s' "$bindings_resp" | jq '.data | length')"
if [[ "$BIND_COUNT" -eq 0 && -n "$BIND_ID_CARD" ]]; then
  bind_resp="$(request_json POST "/api/family/bindings" \
    "{\"elderIdCardNo\":\"${BIND_ID_CARD}\",\"relation\":\"子女\",\"isPrimary\":1,\"remark\":\"回归脚本绑定\"}" "$FAMILY_TOKEN")"
  assert_ok "$bind_resp" "身份证绑定老人"
  bindings_resp="$(request_json GET "/api/family/bindings" "" "$FAMILY_TOKEN")"
  assert_ok "$bindings_resp" "重新查询家属绑定"
  BIND_COUNT="$(printf '%s' "$bindings_resp" | jq '.data | length')"
fi
ELDER_ID="$(printf '%s' "$bindings_resp" | jq -r '.data[0].elderId // empty')"
echo "[6/9] 绑定老人数量：${BIND_COUNT}"

products_resp="$(request_json GET "/api/family/mall/products?pageNo=1&pageSize=20" "" "$FAMILY_TOKEN")"
assert_ok "$products_resp" "查询商城商品"
if [[ -z "$MALL_PRODUCT_ID" ]]; then
  MALL_PRODUCT_ID="$(printf '%s' "$products_resp" | jq -r '.data[0].id // empty')"
fi
if [[ -z "$MALL_PRODUCT_ID" ]]; then
  echo "[7/9] 商城暂无商品，跳过预检与下单"
else
  echo "[7/9] 商城商品查询成功，productId=${MALL_PRODUCT_ID}"
fi

if [[ -n "$ELDER_ID" && -n "$MALL_PRODUCT_ID" ]]; then
  preview_resp="$(request_json POST "/api/family/mall/orders/preview" \
    "{\"elderId\":${ELDER_ID},\"productId\":${MALL_PRODUCT_ID},\"qty\":${MALL_QTY}}" "$FAMILY_TOKEN")"
  assert_ok "$preview_resp" "商城下单预检"
  PREVIEW_ALLOWED="$(printf '%s' "$preview_resp" | jq -r '.data.allowed')"
  CREATED_ORDER_ID=""
  echo "[8/9] 商城预检完成，allowed=${PREVIEW_ALLOWED}"

  if to_bool "$RUN_SUBMIT"; then
    submit_resp="$(request_json POST "/api/family/mall/orders/submit" \
      "{\"elderId\":${ELDER_ID},\"productId\":${MALL_PRODUCT_ID},\"qty\":${MALL_QTY}}" "$FAMILY_TOKEN")"
    assert_ok "$submit_resp" "商城提交下单"
    SUBMIT_ALLOWED="$(printf '%s' "$submit_resp" | jq -r '.data.allowed')"
    CREATED_ORDER_ID="$(printf '%s' "$submit_resp" | jq -r '.data.orderId // empty')"
    echo "      下单结果 allowed=${SUBMIT_ALLOWED} orderNo=$(printf '%s' "$submit_resp" | jq -r '.data.orderNo // ""')"
  fi
else
  echo "[8/9] 跳过商城预检：缺少绑定老人或商品"
fi

orders_path="/api/family/mall/orders?pageNo=1&pageSize=20"
if [[ -n "$ELDER_ID" ]]; then
  orders_path="${orders_path}&elderId=${ELDER_ID}"
fi
orders_resp="$(request_json GET "$orders_path" "" "$FAMILY_TOKEN")"
assert_ok "$orders_resp" "查询商城订单"
ORDER_COUNT="$(printf '%s' "$orders_resp" | jq '.data | length')"
echo "[9/9] 商城订单数量：${ORDER_COUNT}"

DETAIL_ORDER_ID="$(printf '%s' "$orders_resp" | jq -r '.data[0].orderId // empty')"
if [[ -n "$CREATED_ORDER_ID" ]]; then
  DETAIL_ORDER_ID="$CREATED_ORDER_ID"
fi
if [[ -n "$DETAIL_ORDER_ID" ]]; then
  detail_resp="$(request_json GET "/api/family/mall/orders/${DETAIL_ORDER_ID}" "" "$FAMILY_TOKEN")"
  assert_ok "$detail_resp" "查询商城订单详情"
  DETAIL_SUMMARY_ID="$(printf '%s' "$detail_resp" | jq -r '.data.summary.orderId // empty')"
  DETAIL_ORDER_NO="$(printf '%s' "$detail_resp" | jq -r '.data.summary.orderNo // empty')"
  DETAIL_CAN_CANCEL="$(printf '%s' "$detail_resp" | jq -r '.data.summary.canCancel // false')"
  DETAIL_CAN_REFUND="$(printf '%s' "$detail_resp" | jq -r '.data.summary.canRefund // false')"
  if [[ -z "$DETAIL_SUMMARY_ID" || "$DETAIL_SUMMARY_ID" != "$DETAIL_ORDER_ID" ]]; then
    echo "[失败] 商城订单详情断言失败：summary.orderId 与请求不一致"
    printf '%s\n' "$detail_resp" | jq .
    exit 1
  fi
  echo "      订单详情查询成功：orderId=${DETAIL_ORDER_ID} orderNo=${DETAIL_ORDER_NO}"

  if to_bool "$RUN_CANCEL"; then
    if [[ "$DETAIL_CAN_CANCEL" == "true" ]]; then
      cancel_resp="$(request_json POST "/api/family/mall/orders/${DETAIL_ORDER_ID}/cancel" \
        "{\"reason\":\"回归脚本取消测试\"}" "$FAMILY_TOKEN")"
      assert_ok "$cancel_resp" "商城订单取消"
      CANCEL_SUCCESS="$(printf '%s' "$cancel_resp" | jq -r '.data.success // false')"
      if [[ "$CANCEL_SUCCESS" != "true" ]]; then
        echo "[失败] 商城订单取消接口返回 success=false"
        printf '%s\n' "$cancel_resp" | jq .
        exit 1
      fi
      post_cancel_detail_resp="$(request_json GET "/api/family/mall/orders/${DETAIL_ORDER_ID}" "" "$FAMILY_TOKEN")"
      assert_ok "$post_cancel_detail_resp" "取消后查询订单详情"
      POST_CANCEL_STATUS="$(printf '%s' "$post_cancel_detail_resp" | jq -r '.data.summary.orderStatus // empty')"
      if [[ "$POST_CANCEL_STATUS" != "4" ]]; then
        echo "[失败] 订单取消后状态断言失败，期望 4，实际 ${POST_CANCEL_STATUS}"
        printf '%s\n' "$post_cancel_detail_resp" | jq .
        exit 1
      fi
      echo "      订单取消成功并已确认状态：orderId=${DETAIL_ORDER_ID}"
    else
      CANCEL_HINT="$(printf '%s' "$detail_resp" | jq -r '.data.summary.cancelHint // "当前订单不可取消"')"
      echo "      跳过取消：${CANCEL_HINT}"
    fi
  fi

  if to_bool "$RUN_REFUND"; then
    refund_pre_detail_resp="$(request_json GET "/api/family/mall/orders/${DETAIL_ORDER_ID}" "" "$FAMILY_TOKEN")"
    assert_ok "$refund_pre_detail_resp" "退款前查询订单详情"
    REFUND_CAN_APPLY="$(printf '%s' "$refund_pre_detail_resp" | jq -r '.data.summary.canRefund // false')"
    if [[ "$REFUND_CAN_APPLY" == "true" ]]; then
      refund_resp="$(request_json POST "/api/family/mall/orders/${DETAIL_ORDER_ID}/refund" \
        "{\"reason\":\"回归脚本退款测试\"}" "$FAMILY_TOKEN")"
      assert_ok "$refund_resp" "商城订单退款"
      REFUND_SUCCESS="$(printf '%s' "$refund_resp" | jq -r '.data.success // false')"
      if [[ "$REFUND_SUCCESS" != "true" ]]; then
        echo "[失败] 商城订单退款接口返回 success=false"
        printf '%s\n' "$refund_resp" | jq .
        exit 1
      fi
      post_refund_detail_resp="$(request_json GET "/api/family/mall/orders/${DETAIL_ORDER_ID}" "" "$FAMILY_TOKEN")"
      assert_ok "$post_refund_detail_resp" "退款后查询订单详情"
      POST_REFUND_STATUS="$(printf '%s' "$post_refund_detail_resp" | jq -r '.data.summary.orderStatus // empty')"
      if [[ "$POST_REFUND_STATUS" != "5" ]]; then
        echo "[失败] 订单退款后状态断言失败，期望 5，实际 ${POST_REFUND_STATUS}"
        printf '%s\n' "$post_refund_detail_resp" | jq .
        exit 1
      fi
      echo "      订单退款成功并已确认状态：orderId=${DETAIL_ORDER_ID}"
    else
      REFUND_HINT="$(printf '%s' "$refund_pre_detail_resp" | jq -r '.data.summary.refundHint // "当前订单不可退款"')"
      echo "      跳过退款：${REFUND_HINT}"
    fi
  fi
fi

if to_bool "$RUN_RESET"; then
  send_reset_resp="$(request_json POST "/api/auth/family/sms-code/send" \
    "{\"orgId\":${ORG_ID},\"phone\":\"${FAMILY_PHONE}\",\"scene\":\"RESET_PASSWORD\"}")"
  assert_ok "$send_reset_resp" "发送找回密码验证码"
  if [[ -z "$RESET_CODE" ]]; then
    RESET_CODE="$(printf '%s' "$send_reset_resp" | jq -r '.data.debugCode // empty')"
  fi
  if [[ -z "$RESET_CODE" ]]; then
    echo "[失败] 无法自动获取重置验证码，请通过 RESET_CODE 环境变量传入。"
    exit 1
  fi

  reset_resp="$(request_json POST "/api/auth/family/password/reset" \
    "{\"orgId\":${ORG_ID},\"phone\":\"${FAMILY_PHONE}\",\"verifyCode\":\"${RESET_CODE}\",\"newPassword\":\"${FAMILY_NEW_PASSWORD}\"}")"
  assert_ok "$reset_resp" "找回密码重置"

  relogin_resp="$(request_json POST "/api/auth/family/login" \
    "{\"orgId\":${ORG_ID},\"phone\":\"${FAMILY_PHONE}\",\"password\":\"${FAMILY_NEW_PASSWORD}\"}")"
  assert_ok "$relogin_resp" "找回后新密码登录"
  echo "[附加] 找回密码链路验证通过"
fi

echo
echo "✅ 回归完成：注册/登录/密码校验/商城查询链路已验证"
