#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "=========================================================="
echo "    INBOX FULL-STACK RELIABILITY TEST ORCHESTRATOR        "
echo "=========================================================="

cd "${TESTS_ROOT}"

echo ""
echo "[Step 1/5] Executing Unit Tests..."
bash "${SCRIPT_DIR}/run_unit.sh"

echo ""
echo "[Step 2/5] Executing Integration & WebSocket Tests..."
bash "${SCRIPT_DIR}/run_integration.sh"

echo ""
echo "[Step 3/5] Executing Chaos & Resilience Tests..."
bash "${SCRIPT_DIR}/run_chaos.sh"

echo ""
echo "[Step 4/5] Executing Security & Penetration Tests..."
bash "${SCRIPT_DIR}/run_security.sh"

echo ""
echo "[Step 5/5] Executing End-to-End Multi-Browser Tests..."
bash "${SCRIPT_DIR}/run_e2e.sh"

echo ""
echo "=========================================================="
echo "    ALL TEST TIERS COMPLETED SUCCESSFULLY!                "
echo "=========================================================="
