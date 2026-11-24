#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p validation/stage19_build
kotlinc -nowarn validation/stage17_stubs/ComposeRuntime.kt validation/stage17_stubs/compose/*.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  app/src/main/java/com/example/lifedashboard/ui/components/LocalDatePickerField.kt \
  app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt \
  app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarScreen.kt \
  -d validation/stage19_build/calendar-ui.jar
echo "Calendar screen and LocalDatePickerField compile against compile-only Compose/Material stubs."
