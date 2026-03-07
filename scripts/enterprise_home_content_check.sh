#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PROFILE_FILE="admin-web/src/constants/enterpriseProfile.ts"

if [[ ! -f "$PROFILE_FILE" ]]; then
  echo "❌ 未找到企业首页配置文件：$PROFILE_FILE"
  exit 1
fi

echo "== 企业首页内容发布检查 =="
echo

echo "[1/6] 基础字段完整性"
required_fields=(
  "name:"
  "slogan:"
  "heroTitle:"
  "contact:"
  "legal:"
  "publishMeta:"
  "compliance:"
  "faq:"
)
for field in "${required_fields[@]}"; do
  if ! rg -nF "$field" "$PROFILE_FILE" >/dev/null; then
    echo "❌ 缺少字段：$field"
    exit 1
  fi
done
echo "✅ 基础字段完整"
echo

echo "[2/6] 链接占位检查"
if rg -n "vrCommunityUrl:\\s*'#'|joinUsUrl:\\s*'#'|navigationUrl:\\s*'#'" "$PROFILE_FILE"; then
  echo "⚠️ 存在占位链接 '#'，上线前请替换为真实链接。"
else
  echo "✅ 关键链接已配置为真实地址"
fi
echo

echo "[3/6] 示例文案检查"
if rg -n "示例|XXXXXXXX" "$PROFILE_FILE"; then
  echo "⚠️ 仍有示例文案/占位编号，建议上线前替换。"
else
  echo "✅ 未发现示例占位文案"
fi
echo

echo "[4/6] 联系方式格式检查"
if ! rg -n "phone:\\s*'[^']+'" "$PROFILE_FILE" >/dev/null; then
  echo "❌ contact.phone 未配置"
  exit 1
fi
if ! rg -n "email:\\s*'[^']+@[^']+'" "$PROFILE_FILE" >/dev/null; then
  echo "❌ contact.email 格式异常"
  exit 1
fi
echo "✅ 联系方式字段格式正常"
echo

echo "[5/6] 资质证照条目检查"
qual_count="$(rg -n "name:\\s*'.+'" "$PROFILE_FILE" | rg "备案|许可|消防|证" | wc -l | tr -d ' ')"
if [[ "${qual_count:-0}" -lt 2 ]]; then
  echo "⚠️ 资质证照条目较少（<2），建议补充完整。"
else
echo "✅ 资质证照条目数量正常（$qual_count）"
fi
echo

echo "[6/6] FAQ条目检查"
faq_count="$(rg -n "question:\\s*'.+'" "$PROFILE_FILE" | wc -l | tr -d ' ')"
if [[ "${faq_count:-0}" -lt 3 ]]; then
  echo "⚠️ FAQ条目偏少（<3），建议补充常见问题。"
else
  echo "✅ FAQ条目数量正常（$faq_count）"
fi
echo

echo "== 检查完成 =="
