#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p validation/stage20_build
kotlinc -nowarn \
  validation/stage17_stubs/ComposeRuntime.kt \
  validation/stage17_stubs/compose/Graphics.kt \
  validation/stage20_stubs/*.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/ui/preview/LifeDashboardScreenPreviews.kt \
  -d validation/stage20_build/screen-previews.jar
echo "All Compose screen previews compile against compile-only API stubs."
