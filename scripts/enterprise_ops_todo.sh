#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PROFILE_FILE="admin-web/src/constants/enterpriseProfile.ts"

if [[ ! -f "$PROFILE_FILE" ]]; then
  echo "❌ 未找到文件：$PROFILE_FILE"
  exit 1
fi

last_updated="$(rg "lastUpdated:" "$PROFILE_FILE" | tail -n1 | sed -E "s/.*lastUpdated:[[:space:]]*'([^']+)'.*/\\1/")"
next_review="$(rg "nextReviewDate:" "$PROFILE_FILE" | tail -n1 | sed -E "s/.*nextReviewDate:[[:space:]]*'([^']+)'.*/\\1/")"
maintainer="$(rg "maintainer:" "$PROFILE_FILE" | tail -n1 | sed -E "s/.*maintainer:[[:space:]]*'([^']+)'.*/\\1/")"

placeholder_count="$( (rg -n "示例|XXXXXXXX|:\\s*'#'" "$PROFILE_FILE" || true) | wc -l | tr -d ' ')"
faq_count="$(rg -n "question:\\s*'.+'" "$PROFILE_FILE" | wc -l | tr -d ' ')"

if [[ "${placeholder_count}" -eq 0 ]]; then
  todo_1="1) 占位信息已清零，继续保持"
else
  todo_1="1) 替换所有“示例/XXXXXXXX/#”占位信息"
fi

cat <<EOF
【企业首页运营待办】
- 配置文件：$PROFILE_FILE
- 最近更新：${last_updated:-未填写}
- 下次复核：${next_review:-未填写}
- 维护人：${maintainer:-未填写}
- 占位风险项：${placeholder_count}
- FAQ条目：${faq_count}

建议本周处理：
${todo_1}
2) 更新 newsList/communityUpdates/residentActivities
3) 核验资质证照有效期与签发单位
4) 校验地图导航链接可用性
5) 更新图片版权核验日期与维护人
EOF
