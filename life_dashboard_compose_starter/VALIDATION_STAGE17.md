# Stage 17 — результаты проверки

## Пройдены

1. `validation/validate_stage17.py` — проверены модель, Entity, DAO, Room version/migration, Repository, ViewModel, экран, навигация, Menu и backup format v2.
2. `validation/GoalMigrationTest.py` — исполняемый SQLite-тест SQL миграции 4 → 5: существующие строки задач/привычек/проектов остаются; `goals.sortOrder` получает default 0; цель может быть отвязана от удаляемого проекта без удаления самой цели.
3. `validation/GoalModelTest.kt` — progress, completion и clamp progress-модели.
4. Kotlin compile с тестовыми заглушками Android/Room/Lifecycle: DashboardViewModel + репозитории.
5. Kotlin compile с тестовыми заглушками Android/Room/SQLite: Room entities, DAO, database migration и `RoomDashboardPersistence`.
6. Kotlin compile с Room/JSON заглушками: `RoomDashboardBackupRepository`, включая экспорт/импорт целей и поддержку формата v1.
7. Kotlin compile с Compose API-заглушками: `GoalsScreen`.
8. `validation/validate_kotlin_delimiters.py` — баланс delimiters всех Kotlin-файлов.

## Не проверено

- Полная Android Gradle-сборка и запуск на устройстве: исходные Gradle Wrapper/build files и конфигурация Android SDK проекта не предоставлены.
- Полный instrumentation test реального Room/KSP и реальный round-trip JSON через Android `org.json` на устройстве.
- Визуальная проверка экрана на эмуляторе и разных размерах дисплея.

## Ручная проверка после подключения к Android-проекту

1. Создать цель без проекта и цель, связанную с проектом.
2. Изменить название, цвет, срок, целевое и текущее значения; перезапустить приложение и проверить сохранение.
3. Нажать `+` до достижения target и `-` обратно; значения не должны выходить за диапазон 0..target.
4. Удалить связанный проект и убедиться, что цель остаётся без привязки.
5. Удалить цель и проверить подтверждение.
6. Создать backup v2, восстановить его и проверить цели/связи.
7. Импортировать backup v1 из Stage 16: старый формат должен приниматься, список целей должен замениться пустым.
