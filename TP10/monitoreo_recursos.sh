#!/usr/bin/env bash
set -euo pipefail

salida="${1:-resultados/recursos.csv}"
echo "timestamp,contenedor,cpu,memoria" > "$salida"

while true; do
  fecha="$(date +%Y-%m-%dT%H:%M:%S%z)"
  docker stats --no-stream --format "{{.Name}},{{.CPUPerc}},{{.MemUsage}}" | while IFS=, read -r nombre cpu memoria; do
    echo "$fecha,$nombre,$cpu,$memoria" >> "$salida"
  done
  sleep 5
done
