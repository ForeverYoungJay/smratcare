#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PROFILE_FILE="admin-web/src/constants/enterpriseProfile.ts"

if [[ ! -f "$PROFILE_FILE" ]]; then
  echo "❌ 未找到文件：$PROFILE_FILE"
  exit 1
fi

today="$(date +%Y-%m-%d)"
last_updated="$(rg "lastUpdated:" "$PROFILE_FILE" | tail -n1 | sed -E "s/.*lastUpdated:[[:space:]]*'([^']+)'.*/\\1/")"
next_review="$(rg "nextReviewDate:" "$PROFILE_FILE" | tail -n1 | sed -E "s/.*nextReviewDate:[[:space:]]*'([^']+)'.*/\\1/")"
latest_news_date="$(rg "date:\\s*'20[0-9]{2}-[0-9]{2}-[0-9]{2}'" "$PROFILE_FILE" | head -n1 | sed -E "s/.*date:[[:space:]]*'([^']+)'.*/\\1/")"

to_epoch() {
  local input="$1"
  if command -v gdate >/dev/null 2>&1; then
    gdate -d "$input" +%s
    return
  fi
  if date -d "$input" +%s >/dev/null 2>&1; then
    date -d "$input" +%s
    return
  fi
  date -j -f "%Y-%m-%d" "$input" +%s
}

today_epoch="$(to_epoch "$today")"

days_since_update=999
if [[ -n "${last_updated:-}" ]]; then
  days_since_update="$(( ( today_epoch - $(to_epoch "$last_updated") ) / 86400 ))"
fi

days_since_news=999
if [[ -n "${latest_news_date:-}" ]]; then
  days_since_news="$(( ( today_epoch - $(to_epoch "$latest_news_date") ) / 86400 ))"
fi

review_overdue="否"
if [[ -n "${next_review:-}" ]] && [[ "$today_epoch" -gt "$(to_epoch "$next_review")" ]]; then
  review_overdue="是"
fi

status="✅ 状态良好"
if [[ "$days_since_update" -gt 30 ]] || [[ "$days_since_news" -gt 45 ]] || [[ "$review_overdue" == "是" ]]; then
  status="⚠️ 存在内容时效风险"
fi

cat <<EOF
== 企业首页内容时效巡检 ==
- 今日日期：$today
- 最近更新：${last_updated:-未填写}
- 更新间隔：${days_since_update} 天
- 下次复核：${next_review:-未填写}
- 是否逾期：${review_overdue}
- 最新新闻日期：${latest_news_date:-未填写}
- 新闻时效间隔：${days_since_news} 天
- 结论：${status}

建议：
1) 最近更新建议不超过30天
2) 新闻资讯建议不超过45天
3) nextReviewDate 到期前完成一次内容复核
EOF
