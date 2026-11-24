#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
python3 validation/validate_stage24.py
bash validation/run_stage23_checks.sh
python3 validation/validate_kotlin_delimiters.py
printf 'Stage 24 focused checks passed.\n'
