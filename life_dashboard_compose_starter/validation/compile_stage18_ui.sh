#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p validation/stage18_build
STUBS=(validation/stage17_stubs/ComposeRuntime.kt validation/stage17_stubs/compose/*.kt)

kotlinc -nowarn "${STUBS[@]}" \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  app/src/main/java/com/example/lifedashboard/ui/components/LocalDatePickerField.kt \
  app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt \
  app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarScreen.kt \
  -d validation/stage18_build/calendar-ui.jar

kotlinc -nowarn "${STUBS[@]}" \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  app/src/main/java/com/example/lifedashboard/ui/components/LocalDatePickerField.kt \
  app/src/main/java/com/example/lifedashboard/feature/goals/GoalsScreen.kt \
  app/src/main/java/com/example/lifedashboard/feature/tasks/TasksScreen.kt \
  app/src/main/java/com/example/lifedashboard/feature/projects/ProjectEditorDialog.kt \
  -d validation/stage18_build/task-goal-ui.jar

echo "Calendar, Tasks and Goals Compose screens compiled against compile-only stubs."
