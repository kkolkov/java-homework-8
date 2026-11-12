#!/usr/bin/env bash
set -e

pid="${1:?Укажите PID приложения}"
cd "$(dirname "$0")/.."

for endpoint in nplus1 optimized; do
    bash scripts/load.sh "$endpoint" > /dev/null
done

for endpoint in nplus1 optimized; do
    echo "Запись $endpoint: 3000 запросов"
    jcmd "$pid" JFR.start "name=hw8-$endpoint" "settings=$PWD/docs/profiling/cpu.jfc"
    trap 'jcmd "$pid" JFR.stop "name=hw8-$endpoint" >/dev/null 2>&1 || true' EXIT
    : > "docs/profiling/$endpoint-times.csv"
    for ((i = 1; i <= 30; i++)); do
        bash scripts/load.sh "$endpoint" >> "docs/profiling/$endpoint-times.csv"
    done
    jcmd "$pid" JFR.stop "name=hw8-$endpoint" "filename=$PWD/docs/profiling/$endpoint-http.jfr"
    trap - EXIT
done

echo 'Оба профиля сохранены'
