#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

for validator in validation/validate_stage14.py validation/validate_stage15.py validation/validate_stage16.py validation/validate_stage17.py validation/validate_stage18.py validation/validate_stage19.py; do
  python3 "$validator"
done
python3 validation/GoalMigrationTest.py
python3 validation/DateMigrationTest.py
python3 validation/validate_kotlin_delimiters.py

mkdir -p validation/stage19_build
kotlinc -nowarn app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  validation/DateInputTest.kt -include-runtime -d validation/stage19_build/date-input-tests.jar
java -jar validation/stage19_build/date-input-tests.jar
kotlinc -nowarn validation/stage19_stubs/TypeConverterStub.kt \
  app/src/main/java/com/example/lifedashboard/data/local/DateConverters.kt \
  validation/DateConvertersTest.kt -include-runtime -d validation/stage19_build/date-converter-tests.jar
java -jar validation/stage19_build/date-converter-tests.jar
kotlinc -nowarn app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt \
  app/src/main/java/com/example/lifedashboard/data/DateInput.kt \
  app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt \
  validation/CalendarEventsTest.kt -include-runtime -d validation/stage19_build/calendar-tests.jar
java -jar validation/stage19_build/calendar-tests.jar

bash validation/compile_stage19_data.sh
bash validation/compile_stage19_calendar_ui.sh
bash validation/compile_stage19_editor_ui.sh

echo "Stage 19 checks passed (compile-only stubs are not a substitute for a real Android Gradle build)."
