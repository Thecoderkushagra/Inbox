#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "=========================================================="
echo "          RUNNING TIER 6: LOAD & CAPACITY SUITE           "
echo "=========================================================="

cd "${TESTS_ROOT}"

if command -v k6 &> /dev/null; then
  echo "[+] Running k6 room broadcast benchmark..."
  k6 run load/room_broadcast_scale.js
elif command -v artillery &> /dev/null || npx --no-install artillery -v &> /dev/null; then
  echo "[+] Running Artillery socket flood benchmark..."
  npx artillery run load/socket_flood.yml
else
  echo "[!] Neither k6 nor artillery found installed globally. Running via npx artillery..."
  npx artillery run load/socket_flood.yml
fi
