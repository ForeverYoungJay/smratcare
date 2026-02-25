#!/usr/bin/env bash
set -euo pipefail

dir="src/main/resources/db/migration"
if [[ ! -d "$dir" ]]; then
  echo "migration directory not found: $dir" >&2
  exit 1
fi

versions=$(find "$dir" -maxdepth 1 -type f -name 'V*__*.sql' -exec basename {} \; \
  | sed -E -n 's/^(V[0-9]+)__.*\.sql$/\1/p')

dup=$(echo "$versions" | sort | uniq -d)
if [[ -z "$dup" ]]; then
  echo "OK: no duplicate Flyway versions"
  exit 0
fi

echo "Duplicate Flyway versions found:"
while IFS= read -r v; do
  [[ -z "$v" ]] && continue
  echo "- $v"
  find "$dir" -maxdepth 1 -type f -name "${v}__*.sql" -exec basename {} \; | sort | sed 's/^/  - /'
done <<< "$dup"

exit 2
