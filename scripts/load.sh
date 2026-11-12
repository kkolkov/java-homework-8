#!/usr/bin/env bash
set -e

endpoint="${1:-nplus1}"

for ((i = 1; i <= 100; i++)); do
    curl --fail --silent --show-error --max-time 30 \
        --output /dev/null --write-out '%{time_total}\n' \
        "http://localhost:8088/api/messages/$endpoint"
done
