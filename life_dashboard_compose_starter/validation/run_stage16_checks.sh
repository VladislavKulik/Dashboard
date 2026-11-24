#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KOTLIN_BIN="$(command -v kotlinc)"
KOTLIN_LIB="$(cd "$(dirname "$KOTLIN_BIN")/../lib" && pwd)"
COROUTINES="$KOTLIN_LIB/kotlinx-coroutines-core-jvm.jar"
cd "$ROOT"

python validation/validate_stage16.py
python validation/validate_stage15.py
python validation/validate_kotlin_delimiters.py

kotlinc -nowarn -cp "$COROUTINES" \
  validation/stage16_stubs/RoomBackupStubs.kt \
  validation/stage16_stubs/LocalDataStubs.kt \
  validation/stage16_stubs/ModelStubs.kt \
  validation/stage16_stubs/JsonStubs.kt \
  validation/stage16_stubs/BackupRepositoryStub.kt \
  app/src/main/java/com/example/lifedashboard/data/DateKeys.kt \
  app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt \
  -d /tmp/lifedashboard-stage16-backup.jar
printf '%s\n' 'Room backup repository Kotlin compile with Room/JSON API stubs: passed'

kotlinc -nowarn -cp "$COROUTINES" \
  validation/stage15_stubs/DataStoreCoreStubs.kt \
  validation/stage15_stubs/PreferencesStubs.kt \
  validation/stage15_stubs/PreferencesDataStoreExtensionStub.kt \
  validation/stage14_stubs/UriStubs.kt \
  validation/stage14_stubs/ContentStubs.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/SettingsRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/local/SettingsDataStore.kt \
  validation/SettingsRepositoryTest.kt \
  -include-runtime -d /tmp/lifedashboard-settings-stage16.jar
java -cp "/tmp/lifedashboard-settings-stage16.jar:$COROUTINES" SettingsRepositoryTestKt

bash validation/compile_settings_ui.sh
