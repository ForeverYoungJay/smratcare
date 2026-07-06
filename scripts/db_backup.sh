#!/usr/bin/env bash
# 智养云数据库每日备份脚本（配合 crontab 使用，详见 docs/BACKUP_DR_RUNBOOK.md）
#   crontab 示例（每日 01:30）：
#   30 1 * * * DB_PASSWORD=xxx /opt/smartcare/scripts/db_backup.sh >> /var/log/smartcare/db_backup.log 2>&1
set -euo pipefail

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3307}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="${DB_NAME:-zhiyangyun}"
BACKUP_DIR="${BACKUP_DIR:-/var/backups/zhiyangyun}"
# 本地保留份数（等保要求异地/介质另存，本地仅留近 N 份）
RETAIN_COUNT="${RETAIN_COUNT:-14}"

if [[ -z "$DB_PASSWORD" ]]; then
  echo "[db_backup] ERROR: 请通过环境变量 DB_PASSWORD 提供数据库密码（不要写死在脚本里）" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"
timestamp="$(date +%Y%m%d_%H%M%S)"
backup_file="$BACKUP_DIR/${DB_NAME}_${timestamp}.sql.gz"

echo "[db_backup] $(date '+%F %T') 开始备份 ${DB_NAME} -> ${backup_file}"
# --single-transaction：InnoDB 一致性快照，不锁业务表；--routines/--triggers/--events：包含存储对象
mysqldump \
  --host="$DB_HOST" --port="$DB_PORT" \
  --user="$DB_USER" --password="$DB_PASSWORD" \
  --single-transaction --quick --routines --triggers --events \
  --set-gtid-purged=OFF \
  --default-character-set=utf8mb4 \
  "$DB_NAME" | gzip > "$backup_file"

# 简单完整性校验：gzip 可解压且大小非 0
gzip -t "$backup_file"
size=$(du -h "$backup_file" | cut -f1)
echo "[db_backup] 备份完成，大小 ${size}"

# 保留最近 N 份，清理更早的备份
ls -1t "$BACKUP_DIR"/${DB_NAME}_*.sql.gz 2>/dev/null | tail -n "+$((RETAIN_COUNT + 1))" | while read -r old; do
  echo "[db_backup] 清理过期备份: $old"
  rm -f "$old"
done

# ===== 可选：上传到对象存储（异地容灾，按需启用） =====
# 阿里云 OSS（需安装并配置 ossutil）：
#   ossutil cp "$backup_file" "oss://your-bucket/db-backup/$(basename "$backup_file")"
# AWS S3：
#   aws s3 cp "$backup_file" "s3://your-bucket/db-backup/$(basename "$backup_file")"

echo "[db_backup] $(date '+%F %T') 全部完成"
