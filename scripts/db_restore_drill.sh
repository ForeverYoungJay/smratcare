#!/usr/bin/env bash
# 智养云数据库恢复演练脚本：把最近（或指定）备份恢复到临时库并校验表数量。
# 用法：
#   DB_PASSWORD=xxx ./scripts/db_restore_drill.sh                  # 使用最近一份备份
#   DB_PASSWORD=xxx ./scripts/db_restore_drill.sh /path/backup.sql.gz
# 环境变量：
#   KEEP_DRILL_DB=1  演练后保留临时库（默认演练完成即删除）
#   MIN_TABLE_COUNT  期望的最少表数量（默认对比源库表数的 95%）
set -euo pipefail

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3307}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="${DB_NAME:-zhiyangyun}"
BACKUP_DIR="${BACKUP_DIR:-/var/backups/zhiyangyun}"
KEEP_DRILL_DB="${KEEP_DRILL_DB:-0}"

if [[ -z "$DB_PASSWORD" ]]; then
  echo "[restore_drill] ERROR: 请通过环境变量 DB_PASSWORD 提供数据库密码" >&2
  exit 1
fi

mysql_cmd() {
  mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --password="$DB_PASSWORD" \
    --default-character-set=utf8mb4 -N -s -e "$1"
}

backup_file="${1:-}"
if [[ -z "$backup_file" ]]; then
  backup_file=$(ls -1t "$BACKUP_DIR"/${DB_NAME}_*.sql.gz 2>/dev/null | head -n 1 || true)
fi
if [[ -z "$backup_file" || ! -f "$backup_file" ]]; then
  echo "[restore_drill] ERROR: 未找到备份文件（目录 $BACKUP_DIR）" >&2
  exit 1
fi

drill_db="${DB_NAME}_drill_$(date +%Y%m%d_%H%M%S)"
echo "[restore_drill] $(date '+%F %T') 使用备份: $backup_file"
echo "[restore_drill] 恢复到临时库: $drill_db"

mysql_cmd "CREATE DATABASE \`$drill_db\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

cleanup() {
  if [[ "$KEEP_DRILL_DB" != "1" ]]; then
    echo "[restore_drill] 清理临时库 $drill_db"
    mysql_cmd "DROP DATABASE IF EXISTS \`$drill_db\`;" || true
  else
    echo "[restore_drill] 已保留临时库 $drill_db（KEEP_DRILL_DB=1）"
  fi
}
trap cleanup EXIT

gunzip -c "$backup_file" | mysql --host="$DB_HOST" --port="$DB_PORT" \
  --user="$DB_USER" --password="$DB_PASSWORD" --default-character-set=utf8mb4 "$drill_db"

# ===== 校验：表数量与关键表行数 =====
source_tables=$(mysql_cmd "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME';")
drill_tables=$(mysql_cmd "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$drill_db';")
min_expected="${MIN_TABLE_COUNT:-$((source_tables * 95 / 100))}"

echo "[restore_drill] 源库表数=$source_tables，恢复库表数=$drill_tables，最低要求=$min_expected"
if (( drill_tables < min_expected )); then
  echo "[restore_drill] FAILED: 恢复库表数量不足，备份可能不完整！" >&2
  exit 2
fi

for t in org staff elder flyway_schema_history; do
  cnt=$(mysql_cmd "SELECT COUNT(*) FROM \`$drill_db\`.\`$t\`;" 2>/dev/null || echo "N/A")
  echo "[restore_drill] 关键表 $t 行数: $cnt"
done

echo "[restore_drill] $(date '+%F %T') PASSED: 恢复演练成功"
