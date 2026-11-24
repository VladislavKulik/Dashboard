#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

python3 validation/validate_stage18.py
python3 validation/validate_stage17.py
python3 validation/GoalMigrationTest.py
python3 validation/validate_kotlin_delimiters.py

mkdir -p validation/stage18_build
kotlinc -nowarn \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt \
  validation/CalendarEventsTest.kt \
  -include-runtime -d validation/stage18_build/calendar-tests.jar
java -jar validation/stage18_build/calendar-tests.jar

echo "Stage 18 logic and integration checks passed. Run compile_stage18_ui.sh for Compose stub compilation."
