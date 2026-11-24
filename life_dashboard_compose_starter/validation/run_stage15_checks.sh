#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KOTLIN_BIN="$(command -v kotlinc)"
KOTLIN_LIB="$(cd "$(dirname "$KOTLIN_BIN")/../lib" && pwd)"
COROUTINES="$KOTLIN_LIB/kotlinx-coroutines-core-jvm.jar"
cd "$ROOT"

python validation/validate_stage15.py
python validation/validate_kotlin_delimiters.py
python validation/HabitReminderMigrationTest.py
python validation/validate_stage14.py

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
  -include-runtime -d /tmp/lifedashboard-settings-stage15.jar
java -cp "/tmp/lifedashboard-settings-stage15.jar:$COROUTINES" SettingsRepositoryTestKt

kotlinc -nowarn -cp "$COROUTINES" \
  validation/stage14_stubs/AndroidStubs.kt \
  validation/stage14_stubs/OsStubs.kt \
  validation/stage14_stubs/UriStubs.kt \
  validation/stage14_stubs/ContentStubs.kt \
  validation/stage14_stubs/PackageManagerStubs.kt \
  validation/stage14_stubs/PendingIntentStubs.kt \
  validation/stage15_stubs/DataStoreCoreStubs.kt \
  validation/stage15_stubs/PreferencesStubs.kt \
  validation/stage15_stubs/PreferencesDataStoreExtensionStub.kt \
  validation/stage14_stubs/AppStubs.kt \
  validation/stage14_stubs/RoomPersistenceStub.kt \
  validation/stage14_stubs/TaskReminderStub.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/SettingsRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/local/SettingsDataStore.kt \
  app/src/main/java/com/example/lifedashboard/reminders/ReminderTimeCalculator.kt \
  app/src/main/java/com/example/lifedashboard/reminders/HabitReminderScheduler.kt \
  app/src/main/java/com/example/lifedashboard/reminders/HabitReminderReceiver.kt \
  app/src/main/java/com/example/lifedashboard/reminders/HabitReminderRescheduleReceiver.kt \
  -d /tmp/lifedashboard-reminders-stage15.jar
printf '%s\n' 'Reminder receivers + DataStore Kotlin compile with Android/Room/DataStore stubs: passed'

kotlinc -nowarn -cp "$COROUTINES" \
  validation/stage14_stubs/AndroidStubs.kt \
  validation/stage14_stubs/OsStubs.kt \
  validation/stage14_stubs/UriStubs.kt \
  validation/stage14_stubs/ContentStubs.kt \
  validation/stage14_stubs/PackageManagerStubs.kt \
  validation/stage14_stubs/PendingIntentStubs.kt \
  validation/stage14_stubs/LifecycleStubs.kt \
  validation/stage14_stubs/ReminderReceiverStub.kt \
  validation/stage14_stubs/TaskReminderStub.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/data/DateKeys.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/*.kt \
  app/src/main/java/com/example/lifedashboard/presentation/DashboardUiState.kt \
  app/src/main/java/com/example/lifedashboard/presentation/DashboardViewModel.kt \
  app/src/main/java/com/example/lifedashboard/reminders/ReminderTimeCalculator.kt \
  app/src/main/java/com/example/lifedashboard/reminders/HabitReminderScheduler.kt \
  -d /tmp/lifedashboard-vm-stage15.jar
printf '%s\n' 'DashboardViewModel Kotlin compile with AndroidX/Android stubs: passed'

