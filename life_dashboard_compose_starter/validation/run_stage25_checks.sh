#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
python3 validation/validate_stage25.py
python3 validation/validate_kotlin_delimiters.py
printf 'Stage 25 focused checks passed.\n'
