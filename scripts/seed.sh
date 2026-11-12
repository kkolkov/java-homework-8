#!/usr/bin/env bash
set -e

for ((i = 1; i <= 300; i++)); do
    curl --fail --silent --show-error --max-time 15 \
        -H 'Content-Type: application/json' \
        --data "{\"content\":\"Message $i\",\"username\":\"author-$i\"}" \
        http://localhost:8088/api/messages > /dev/null
done

echo 'Создано 300 сообщений'
