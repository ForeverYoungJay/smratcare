#!/usr/bin/env bash

set -euo pipefail

SITE="www.gfyy.org.cn"
TOKEN="ehbvANBUDpzkwNRY"
ENDPOINT="http://data.zz.baidu.com/urls?site=${SITE}&token=${TOKEN}"
URL_FILE="${1:-./scripts/urls.txt}"

if [[ ! -f "${URL_FILE}" ]]; then
  echo "URL file not found: ${URL_FILE}" >&2
  echo "Usage: bash ./scripts/push-baidu-urls.sh ./scripts/urls.txt" >&2
  exit 1
fi

echo "Submitting URLs to Baidu:"
echo "  site: ${SITE}"
echo "  file: ${URL_FILE}"
echo "  endpoint: ${ENDPOINT}"

curl -H 'Content-Type:text/plain' --data-binary "@${URL_FILE}" "${ENDPOINT}"
echo
