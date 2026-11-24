# Stage 12 — проверки и ограничения

## Выполнено

1. Kotlin compilation-check слоя data/repository/ViewModel выполнен локальным `kotlinc` с compile-only заглушками Android Context, Room, SQLite и Lifecycle. Компиляция прошла; заглушки не являются заменой Android Gradle build.
2. SQLite smoke-test выполнил SQL миграций на двух сценариях: исходная база v1 прошла цепочку v1 → v2 → v3, а база Stage 11 версии v2 — напрямую v2 → v3 (в ней `tasks.sortOrder` уже существовал без SQL default). В обоих случаях проверено сохранение task/project/stage строк, порядка задач, порядка этапов 1, 2, 3 и SQL default `0`, согласованного с `@ColumnInfo(defaultValue = "0")`.
3. Выполнен синтаксический проход Kotlin-файлов компилятором; ошибок разбора Kotlin не обнаружено. Полная типовая проверка Compose UI не проводилась, поскольку Compose/Android Gradle зависимости проекта не включены.
4. Проверены вызовы новых repository-методов из ViewModel, передача callbacks в UI и цепочка миграций Room.
5. Проверены ZIP-архивы на целостность после сборки.

## Не выполнено

- Нет полной Android Gradle-сборки: исходный пакет не содержит `settings.gradle(.kts)`, `build.gradle(.kts)`, Gradle Wrapper, AndroidManifest и конфигурацию SDK.
- Не запускался эмулятор/устройство, поэтому визуальный smoke-test и реальное поведение диалогов не подтверждены.
- SQLite smoke-test воспроизводит SQL миграций, но не заменяет тест `MigrationTestHelper` с реальным Room schema validation.
- Не проверены крайние сценарии с параллельным редактированием проекта из нескольких экранов. Записи внутри одного экземпляра persistence сериализуются Mutex и Room transactions.

Перед использованием с важными данными сделай резервную копию `life_dashboard.db` и проверь миграцию на копии базы.
