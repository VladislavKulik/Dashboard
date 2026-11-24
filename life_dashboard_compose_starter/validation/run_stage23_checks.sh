#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
python3 validation/validate_stage23.py
kotlinc -nowarn \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt \
  validation/CalendarEventsTest.kt \
  -include-runtime -d /tmp/lifedashboard-stage23-calendar.jar
java -jar /tmp/lifedashboard-stage23-calendar.jar
python3 validation/validate_kotlin_delimiters.py
echo "Stage 23 focused calendar checks passed."
