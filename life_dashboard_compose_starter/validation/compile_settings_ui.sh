#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
kotlinc -nowarn \
  validation/stage15_stubs/ComposeRuntimeStubs.kt \
  validation/stage15_stubs/ComposeColorStubs.kt \
  validation/stage15_stubs/ModifierStub.kt \
  validation/stage15_stubs/UnitsStub.kt \
  validation/stage15_stubs/AlignmentStub.kt \
  validation/stage15_stubs/LayoutStubs.kt \
  validation/stage15_stubs/ScrollStubs.kt \
  validation/stage15_stubs/MaterialThemeStubs.kt \
  validation/stage15_stubs/ThemeTokensStub.kt \
  validation/stage15_stubs/SettingsComponentsStubs.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/ui/theme/Color.kt \
  app/src/main/java/com/example/lifedashboard/ui/theme/Theme.kt \
  app/src/main/java/com/example/lifedashboard/feature/settings/SettingsScreen.kt \
  validation/ThemePaletteCompileTest.kt \
  -d /tmp/lifedashboard-settings-ui-stage15.jar
printf '%s\n' 'Settings screen + theme palette Kotlin compile with Compose API stubs: passed'
