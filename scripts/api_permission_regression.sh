#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

TEST_CLASS="ApiPermissionIsolationRegressionTest"

if command -v mvn >/dev/null 2>&1; then
  echo "[api-permission-regression] running with mvn: ${TEST_CLASS}"
  mvn -Dtest="${TEST_CLASS}" test
  exit 0
fi

if [[ -x "$ROOT_DIR/mvnw" ]]; then
  echo "[api-permission-regression] running with mvnw: ${TEST_CLASS}"
  "$ROOT_DIR/mvnw" -Dtest="${TEST_CLASS}" test
  exit 0
fi

echo "[api-permission-regression] ERROR: 未检测到 Maven（mvn/mvnw）。"
echo "请先安装 Maven，或在项目根目录提供 mvnw 后再执行："
echo "  bash scripts/api_permission_regression.sh"
exit 127
