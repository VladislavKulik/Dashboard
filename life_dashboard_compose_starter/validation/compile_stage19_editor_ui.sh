#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p validation/stage19_build
kotlinc -nowarn validation/stage17_stubs/ComposeRuntime.kt validation/stage17_stubs/compose/*.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  app/src/main/java/com/example/lifedashboard/ui/components/LocalDatePickerField.kt \
  app/src/main/java/com/example/lifedashboard/feature/goals/GoalsScreen.kt \
  app/src/main/java/com/example/lifedashboard/feature/tasks/TasksScreen.kt \
  app/src/main/java/com/example/lifedashboard/feature/projects/ProjectEditorDialog.kt \
  -d validation/stage19_build/task-goal-project-ui.jar
echo "Tasks, Goals and Project Editor screens compile against compile-only Compose/Material stubs."
