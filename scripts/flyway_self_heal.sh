#!/usr/bin/env bash
if [ -z "${BASH_VERSION:-}" ]; then
  exec bash "$0" "$@"
fi
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_ARGS=()
NO_BUILD=0
LOG_TAIL=160
WAIT_SECONDS=10

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
    --wait-seconds)
      WAIT_SECONDS="$2"
      shift 2
      ;;
    -h|--help)
      cat <<'EOF'
Usage:
  ./scripts/flyway_self_heal.sh [--env-file <file>] [-f <compose-file>] [--no-build] [--tail <n>] [--wait-seconds <n>]

Description:
  1) 检查本地 Flyway 版本号是否重复
  2) 检查数据库 flyway_schema_history 中 success=0 的失败迁移
  3) 自动删除失败记录（仅 success=0）
  4) 重启（或重建）backend 触发 Flyway 重新迁移
  5) 循环检查 backend 日志并给出判定（成功/失败/待继续观察）
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

mysql_ready() {
  compose exec -T mysql sh -lc "mysqladmin ping -uroot -p\"\$MYSQL_ROOT_PASSWORD\" --silent" >/dev/null 2>&1
}

wait_mysql_ready() {
  local max_wait="$1"
  local elapsed=0
  while (( elapsed < max_wait )); do
    if mysql_ready; then
      echo "[OK] mysql 已就绪"
      return 0
    fi
    sleep 1
    ((elapsed+=1))
  done
  echo "[FAIL] mysql ${max_wait}s 内未就绪"
  return 1
}

history_table_exists() {
  local exists
  exists="$(mysql_exec "SELECT COUNT(1) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flyway_schema_history';" || echo "0")"
  [[ "${exists}" == "1" ]]
}

has_rg() {
  command -v rg >/dev/null 2>&1
}

log_match() {
  local logs="$1"
  local pattern="$2"
  if has_rg; then
    echo "${logs}" | rg -q "${pattern}"
  else
    echo "${logs}" | grep -Eq "${pattern}"
  fi
}

log_extract_last() {
  local logs="$1"
  local pattern="$2"
  if has_rg; then
    echo "${logs}" | rg -o "${pattern}" | tail -n 1
  else
    echo "${logs}" | grep -Eo "${pattern}" | tail -n 1
  fi
}

delete_failed_version_record() {
  local version="$1"
  [[ -z "${version}" ]] && return 0
  mysql_exec "DELETE FROM flyway_schema_history WHERE version='${version}' AND success=0;"
}

echo "[STEP 1/5] 检查本地迁移版本冲突"
./scripts/check_flyway_versions.sh

echo "[STEP 2/5] 启动 mysql（如未启动）"
compose up -d mysql >/dev/null
wait_mysql_ready "${WAIT_SECONDS}"

echo "[STEP 3/5] 检查失败迁移（success=0）"
if ! history_table_exists; then
  echo "[WARN] flyway_schema_history 尚不存在，跳过失败记录清理"
  FAILED_ROWS=""
else
  FAILED_ROWS="$(mysql_exec "SELECT CONCAT(version, '\t', description) FROM flyway_schema_history WHERE success=0 ORDER BY installed_rank;" || true)"
fi

if [[ -n "${FAILED_ROWS:-}" ]]; then
  echo "[WARN] 发现失败迁移："
  declare -a FAILED_VERSIONS=()
  while IFS=$'\t' read -r version description; do
    [[ -z "${version}" ]] && continue
    echo "  - version=${version} description=${description}"
    FAILED_VERSIONS+=("${version}")
  done <<< "${FAILED_ROWS}"
  echo "[ACTION] 精确删除失败迁移记录（仅上述版本）"
  for version in "${FAILED_VERSIONS[@]}"; do
    mysql_exec "DELETE FROM flyway_schema_history WHERE version='${version}' AND success=0;"
  done
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
FAIL_PATTERN="Validate failed|Migrations have failed validation|Found more than one migration|Application run failed|Script V[0-9]+__.* failed|SQL State"
SUCCESS_PATTERN="Started CareApplication|Tomcat started on port 8080"

BACKEND_LOGS=""
for _ in $(seq 1 "${WAIT_SECONDS}"); do
  sleep 1
  BACKEND_LOGS="$(compose logs --tail="${LOG_TAIL}" backend || true)"
  if log_match "${BACKEND_LOGS}" "${SUCCESS_PATTERN}"; then
    echo "${BACKEND_LOGS}"
    echo
    echo "[OK] backend 启动成功，Flyway 自检+修复完成"
    exit 0
  fi
  if log_match "${BACKEND_LOGS}" "${FAIL_PATTERN}"; then
    break
  fi
done

echo "${BACKEND_LOGS}"

if log_match "${BACKEND_LOGS}" "${FAIL_PATTERN}"; then
  FAILED_SCRIPT="$(log_extract_last "${BACKEND_LOGS}" "V[0-9]+__[^[:space:]]+\\.sql" || true)"
  FAILED_VERSION="$(log_extract_last "${BACKEND_LOGS}" "version [0-9]+" | awk '{print $2}' || true)"
  echo
  echo "[FAIL] 仍存在 Flyway/启动错误"
  if [[ -n "${FAILED_SCRIPT}" ]]; then
    echo "[HINT] 失败迁移脚本: ${FAILED_SCRIPT}"
  fi
  if [[ -n "${FAILED_VERSION}" ]]; then
    echo "[ACTION] 自动清理本轮失败记录: version=${FAILED_VERSION}"
    delete_failed_version_record "${FAILED_VERSION}" || true
    echo "[HINT] 可在脚本修复后重试本命令；若仍卡校验，可清理该失败记录:"
    echo "       DELETE FROM flyway_schema_history WHERE version='${FAILED_VERSION}' AND success=0;"
  fi
  exit 1
fi

compose_args_text=""
if ((${#COMPOSE_ARGS[@]})); then
  compose_args_text="${COMPOSE_ARGS[*]}"
fi

echo
echo "[WARN] ${WAIT_SECONDS}s 内未检测到明确成功/失败日志，请继续观察：docker compose ${compose_args_text} logs -f backend"
exit 0
