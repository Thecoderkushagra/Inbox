#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "=========================================================="
echo "          RUNNING TIER 3: CHAOS & RESILIENCE SUITE        "
echo "=========================================================="

cd "${TESTS_ROOT}"
npx vitest run chaos/
