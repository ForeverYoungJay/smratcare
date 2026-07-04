#!/usr/bin/env bash
if [ -z "${BASH_VERSION:-}" ]; then
  exec bash "$0" "$@"
fi
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "[ERROR] $1" >&2
  exit 1
}

check_cmd() {
  local cmd="$1"
  local hint="$2"
  command -v "$cmd" >/dev/null 2>&1 || fail "$cmd 未安装。$hint"
}

parse_major() {
  sed -E 's/[^0-9]*([0-9]+).*/\1/'
}

check_cmd node "请安装 Node.js 20（或至少 18）后重试。"
NODE_VERSION="$(node -v)"
NODE_MAJOR="$(printf '%s' "$NODE_VERSION" | parse_major)"
if [ "${NODE_MAJOR:-0}" -lt 18 ]; then
  fail "当前 Node.js 版本为 $NODE_VERSION，项目要求 Node.js 18+（推荐 20，见 .nvmrc）。"
fi

check_cmd java "请安装 Java 17 运行时。"
JAVA_VERSION_RAW="$(java -version 2>&1 | head -n 1)"
JAVA_MAJOR="$(printf '%s' "$JAVA_VERSION_RAW" | sed -E 's/.*version "([0-9]+).*/\1/')"
if [ "${JAVA_MAJOR:-0}" -lt 17 ]; then
  fail "当前 Java 版本为: $JAVA_VERSION_RAW，项目要求 Java 17+。"
fi

check_cmd mvn "请安装 Maven 3.9+，或使用与 Dockerfile 一致的 Maven 3.9 环境。"
MAVEN_VERSION_RAW="$(mvn -v | head -n 1)"
MAVEN_VERSION="$(printf '%s' "$MAVEN_VERSION_RAW" | sed -E 's/.* ([0-9]+\.[0-9]+\.[0-9]+).*/\1/')"
MAVEN_MAJOR="$(printf '%s' "$MAVEN_VERSION" | cut -d. -f1)"
MAVEN_MINOR="$(printf '%s' "$MAVEN_VERSION" | cut -d. -f2)"
if [ "${MAVEN_MAJOR:-0}" -lt 3 ] || { [ "${MAVEN_MAJOR:-0}" -eq 3 ] && [ "${MAVEN_MINOR:-0}" -lt 9 ]; }; then
  fail "当前 Maven 版本为 $MAVEN_VERSION，项目要求 Maven 3.9+。"
fi

check_cmd docker "请安装 Docker Desktop / Docker Engine。"
docker compose version >/dev/null 2>&1 || fail "docker compose 不可用。请安装支持 Compose V2 的 Docker 环境。"

echo "[OK] Runtime requirements satisfied"
echo "  node:   $NODE_VERSION"
echo "  java:   $JAVA_VERSION_RAW"
echo "  maven:  $MAVEN_VERSION_RAW"
echo "  docker: $(docker --version)"
