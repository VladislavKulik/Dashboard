#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
COROUTINES="$(dirname "$(dirname "$(command -v kotlinc)")")/lib/kotlinx-coroutines-core-jvm.jar"
mkdir -p validation/stage19_build
kotlinc -nowarn -classpath "$COROUTINES" \
  validation/stage19_stubs/RoomStubs.kt \
  validation/stage19_stubs/ContextStub.kt \
  validation/stage19_stubs/MigrationStubs.kt \
  validation/stage19_stubs/SqliteStubs.kt \
  validation/stage19_stubs/JsonStubs.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/model/DashboardSettings.kt \
  app/src/main/java/com/example/lifedashboard/data/DateKeys.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  app/src/main/java/com/example/lifedashboard/data/TaskRecurrenceCalculator.kt \
  app/src/main/java/com/example/lifedashboard/data/local/DashboardEntities.kt \
  app/src/main/java/com/example/lifedashboard/data/local/DateConverters.kt \
  app/src/main/java/com/example/lifedashboard/data/local/DashboardDao.kt \
  app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/BackupRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/DashboardRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/TaskRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/NotesRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/HabitsRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/ProjectsRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/ActivityRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/repository/GoalsRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/DashboardSeedData.kt \
  app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt \
  app/src/main/java/com/example/lifedashboard/data/local/RoomDashboardPersistence.kt \
  -d validation/stage19_build/data-layer.jar
echo "Stage 21 recurrence, Room entities, converters, migrations and backup layer compile against compile-only Android/Room/JSON stubs."
