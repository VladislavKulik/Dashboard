#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

for validator in \
  validation/validate_stage14.py \
  validation/validate_stage15.py \
  validation/validate_stage16.py \
  validation/validate_stage17.py \
  validation/validate_stage18.py \
  validation/validate_stage19.py \
  validation/validate_stage20_setup.py \
  validation/validate_stage21.py \
  validation/validate_stage23.py \
  validation/validate_stage24.py \
  validation/validate_stage25.py \
  validation/validate_stage26.py; do
  python3 "$validator"
done

bash validation/run_stage14_checks.sh
bash validation/run_stage15_checks.sh
bash validation/run_stage23_checks.sh
bash validation/run_stage24_checks.sh
bash validation/run_stage25_checks.sh
python3 validation/validate_kotlin_delimiters.py
bash validation/compile_stage19_data.sh
bash validation/compile_stage19_editor_ui.sh
bash validation/compile_stage20_previews.sh

echo "Current focused review checks passed. These checks do not replace a real Android Gradle build or device testing."
