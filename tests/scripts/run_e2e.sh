#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "=========================================================="
echo "          RUNNING TIER 5: END-TO-END BROWSER SUITE        "
echo "=========================================================="

cd "${TESTS_ROOT}"
npx playwright test
