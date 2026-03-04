#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_ARGS=()
NO_BUILD=0
LOG_TAIL=160

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
    --no-build)
      NO_BUILD=1
      shift
      ;;
    --tail)
      LOG_TAIL="$2"
      shift 2
      ;;
    -h|--help)
      cat <<'EOF'
Usage:
  ./scripts/flyway_self_heal.sh [--env-file <file>] [-f <compose-file>] [--no-build] [--tail <n>]

Description:
  1) 检查本地 Flyway 版本号是否重复
  2) 检查数据库 flyway_schema_history 中 success=0 的失败迁移
  3) 自动删除失败记录（仅 success=0）
  4) 重启（或重建）backend 触发 Flyway 重新迁移
  5) 打印 backend 最近日志并给出判定
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

echo "[STEP 1/5] 检查本地迁移版本冲突"
./scripts/check_flyway_versions.sh

echo "[STEP 2/5] 启动 mysql（如未启动）"
compose up -d mysql >/dev/null

echo "[STEP 3/5] 检查失败迁移（success=0）"
FAILED_ROWS="$(mysql_exec "SELECT CONCAT(version, '\t', description) FROM flyway_schema_history WHERE success=0 ORDER BY installed_rank;" || true)"
if [[ -n "${FAILED_ROWS}" ]]; then
  echo "[WARN] 发现失败迁移："
  while IFS=$'\t' read -r version description; do
    [[ -z "${version}" ]] && continue
    echo "  - version=${version} description=${description}"
  done <<< "${FAILED_ROWS}"
  echo "[ACTION] 删除失败迁移记录（仅 success=0）"
  mysql_exec "DELETE FROM flyway_schema_history WHERE success=0;"
else
  echo "[OK] 未发现失败迁移记录"
fi

echo "[STEP 4/5] 重启 backend 并触发 Flyway"
if [[ "$NO_BUILD" -eq 1 ]]; then
  compose up -d --no-deps backend >/dev/null
else
  compose up -d --build --no-deps backend >/dev/null
fi

echo "[STEP 5/5] 检查 backend 日志"
sleep 4
BACKEND_LOGS="$(compose logs --tail="${LOG_TAIL}" backend || true)"
echo "${BACKEND_LOGS}"

if echo "${BACKEND_LOGS}" | rg -q "Validate failed|Migrations have failed validation|Found more than one migration|Application run failed"; then
  echo
  echo "[FAIL] 仍存在 Flyway/启动错误，请根据日志继续处理"
  exit 1
fi

if echo "${BACKEND_LOGS}" | rg -q "Started CareApplication"; then
  echo
  echo "[OK] backend 启动成功，Flyway 自检+修复完成"
  exit 0
fi

compose_args_text=""
if ((${#COMPOSE_ARGS[@]})); then
  compose_args_text="${COMPOSE_ARGS[*]}"
fi

echo
echo "[WARN] 未检测到明确成功日志，请执行：docker compose ${compose_args_text} logs -f backend"
exit 0
