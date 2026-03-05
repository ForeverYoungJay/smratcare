#!/usr/bin/env bash
if [ -z "${BASH_VERSION:-}" ]; then
  exec bash "$0" "$@"
fi
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

WITH_FRONTEND=1
WITH_FLYWAY=1

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-frontend)
      WITH_FRONTEND=0
      shift
      ;;
    --skip-flyway)
      WITH_FLYWAY=0
      shift
      ;;
    -h|--help)
      cat <<'EOF'
Usage:
  ./scripts/project_self_check.sh [--skip-frontend] [--skip-flyway]

Description:
  1) Flyway 本地脚本版本冲突检查
  2) 前端导航路由检查

Notes:
  - 不依赖 docker，可先做快速静态自检。
  - 如需数据库失败迁移检查，请另外执行：
      ./scripts/flyway_status.sh [--env-file ...] [-f ...]
EOF
      exit 0
      ;;
    *)
      echo "[ERROR] Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

echo "[INFO] Project self check started at $(date '+%Y-%m-%d %H:%M:%S')"

if [[ "$WITH_FLYWAY" -eq 1 ]]; then
  echo "[CHECK 1/2] Flyway 版本号冲突"
  bash ./scripts/check_flyway_versions.sh
else
  echo "[SKIP] Flyway check skipped"
fi

if [[ "$WITH_FRONTEND" -eq 1 ]]; then
  echo "[CHECK 2/2] 前端导航路由检查"
  (
    cd admin-web
    npm run -s check:navigation
  )
else
  echo "[SKIP] Frontend check skipped"
fi

echo "[OK] Project self check passed"
