#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
python3 validation/validate_stage21.py
kotlinc -nowarn app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/TaskRecurrenceCalculator.kt \
  validation/TaskRecurrenceSmoke.kt -include-runtime -d /tmp/lifedashboard-stage21-recurrence.jar
java -jar /tmp/lifedashboard-stage21-recurrence.jar
bash validation/compile_stage19_data.sh
bash validation/compile_stage19_editor_ui.sh
bash validation/compile_stage20_previews.sh
python3 validation/validate_kotlin_delimiters.py
python3 validation/validate_stage20_setup.py
