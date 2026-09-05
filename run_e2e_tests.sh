#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export PYTHONPATH="${DIR}:${PYTHONPATH}"

echo "================================================================================"
echo "    LAUNCHING PTS MOBILE 4-TIER E2E AUTOMATED TEST SUITE                        "
echo "================================================================================"

python3 "${DIR}/e2e_tests/main.py" "$@"
