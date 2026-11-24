#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KOTLIN_BIN="$(command -v kotlinc)"
KOTLIN_LIB="$(cd "$(dirname "$KOTLIN_BIN")/../lib" && pwd)"
COROUTINES="$KOTLIN_LIB/kotlinx-coroutines-core-jvm.jar"
cd "$ROOT"

python3 validation/validate_stage17.py
python3 validation/GoalMigrationTest.py
python3 validation/validate_kotlin_delimiters.py
kotlinc -nowarn app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt app/src/main/java/com/example/lifedashboard/data/DateKeys.kt validation/GoalModelTest.kt -include-runtime -d /tmp/lifedashboard-goal-model-test.jar
java -jar /tmp/lifedashboard-goal-model-test.jar

kotlinc -nowarn -cp "$COROUTINES" \
  validation/stage14_stubs/AndroidStubs.kt validation/stage14_stubs/OsStubs.kt \
  validation/stage14_stubs/UriStubs.kt validation/stage14_stubs/ContentStubs.kt \
  validation/stage14_stubs/PackageManagerStubs.kt validation/stage14_stubs/PendingIntentStubs.kt \
  validation/stage15_stubs/DataStoreCoreStubs.kt validation/stage15_stubs/PreferencesStubs.kt \
  validation/stage15_stubs/PreferencesDataStoreExtensionStub.kt validation/stage14_stubs/AppStubs.kt \
  validation/stage14_stubs/RoomPersistenceStub.kt validation/stage14_stubs/LifecycleStubs.kt \
  validation/stage14_stubs/ReminderReceiverStub.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/data/DateKeys.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/*.kt \
  app/src/main/java/com/example/lifedashboard/presentation/DashboardUiState.kt \
  app/src/main/java/com/example/lifedashboard/presentation/DashboardViewModel.kt \
  app/src/main/java/com/example/lifedashboard/reminders/ReminderTimeCalculator.kt \
  app/src/main/java/com/example/lifedashboard/reminders/HabitReminderScheduler.kt \
  -d /tmp/lifedashboard-vm-stage17.jar

kotlinc -nowarn -cp "$COROUTINES" \
  validation/stage17_stubs/AndroidContextStub.kt validation/stage17_stubs/RoomApiStubs.kt \
  validation/stage17_stubs/SqliteStub.kt validation/stage17_stubs/MigrationStub.kt \
  validation/stage17_stubs/BackupCreatorStub.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/data/DateKeys.kt \
  app/src/main/java/com/example/lifedashboard/data/DashboardSeedData.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/*.kt \
  app/src/main/java/com/example/lifedashboard/data/local/DashboardEntities.kt \
  app/src/main/java/com/example/lifedashboard/data/local/DashboardDao.kt \
  app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt \
  app/src/main/java/com/example/lifedashboard/data/local/RoomDashboardPersistence.kt \
  -d /tmp/lifedashboard-room-stage17.jar

kotlinc -nowarn -cp "$COROUTINES" \
  validation/stage16_stubs/RoomBackupStubs.kt validation/stage16_stubs/LocalDataStubs.kt \
  validation/stage16_stubs/ModelStubs.kt validation/stage16_stubs/JsonStubs.kt \
  validation/stage16_stubs/BackupRepositoryStub.kt \
  app/src/main/java/com/example/lifedashboard/data/DateKeys.kt \
  app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt \
  -d /tmp/lifedashboard-backup-stage17.jar

kotlinc -nowarn \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  validation/stage17_stubs/ComposeRuntime.kt validation/stage17_stubs/compose/*.kt \
  app/src/main/java/com/example/lifedashboard/feature/goals/GoalsScreen.kt \
  -d /tmp/lifedashboard-goals-ui-stage17.jar

echo "Stage 17 checks completed. Android Gradle/device verification is still required in the original project."
