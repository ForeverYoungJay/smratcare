#!/usr/bin/env bash
if [ -z "${BASH_VERSION:-}" ]; then
  exec bash "$0" "$@"
fi
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --env-file)
      COMPOSE_ARGS+=(--env-file "$2")
      shift 2
      ;;
    -f|--file)
      COMPOSE_ARGS+=(-f "$2")
      shift 2
      ;;
    -h|--help)
      cat <<'EOF'
Usage:
  ./scripts/flyway_status.sh [--env-file <file>] [-f <compose-file>]

Description:
  1) 检查本地 Flyway 脚本版本号是否重复
  2) 检查数据库 flyway_schema_history 中失败迁移(success=0)
  3) 输出最近迁移记录，便于定位当前状态
EOF
      exit 0
      ;;
    *)
      echo "[ERROR] Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

compose() {
  if ((${#COMPOSE_ARGS[@]})); then
    docker compose "${COMPOSE_ARGS[@]}" "$@"
  else
    docker compose "$@"
  fi
}

mysql_exec() {
  local sql="$1"
  compose exec -T mysql sh -lc "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" -D \"\${MYSQL_DATABASE:-zhiyangyun}\" -N -e \"$sql\""
}

echo "[CHECK 1/3] 本地 Flyway 版本冲突"
./scripts/check_flyway_versions.sh

echo "[CHECK 2/3] 启动 mysql（如未启动）"
compose up -d mysql >/dev/null

echo "[CHECK 3/3] 数据库 Flyway 状态"
FAILED_ROWS="$(mysql_exec "SELECT CONCAT(version, '\t', description) FROM flyway_schema_history WHERE success=0 ORDER BY installed_rank;" || true)"
if [[ -n "${FAILED_ROWS}" ]]; then
  echo "[FAIL] 发现失败迁移："
  while IFS=$'\t' read -r version description; do
    [[ -z "${version}" ]] && continue
    echo "  - version=${version} description=${description}"
  done <<< "${FAILED_ROWS}"
else
  echo "[OK] 未发现失败迁移"
fi

echo
echo "[INFO] 最近10条迁移："
mysql_exec "SELECT installed_rank, version, description, success, installed_on FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;"

if [[ -n "${FAILED_ROWS}" ]]; then
  compose_args_text=""
  if ((${#COMPOSE_ARGS[@]})); then
    compose_args_text="${COMPOSE_ARGS[*]}"
  fi
  echo
  echo "[SUGGEST] 可执行：./scripts/flyway_self_heal.sh ${compose_args_text}"
  exit 1
fi

exit 0
