# Подключение Patch Stage 17

Патч предназначен для проекта из Stage 16.

1. Сделай копию текущей базы `life_dashboard.db` и исходников.
2. Скопируй файлы из архива поверх соответствующих путей проекта, сохранив структуру `app/src/main/java`.
3. Убедись, что `GoalsScreen.kt` добавлен в `feature/goals`.
4. Не удаляй и не заменяй `AndroidManifest.xml`, `MainActivity.kt`, Gradle-файлы или собственные настройки проекта.
5. Убедись, что `RoomDashboardPersistence` передаёт `MIGRATION_4_5` в `addMigrations()`; не используй destructive migration.
6. `DashboardViewModel.Factory` теперь принимает `GoalsRepository` последним необязательным параметром. В `LifeDashboardApp` он передаётся тем же `RoomDashboardPersistence`.
7. Формат JSON backup теперь v2; импорт v1 из Stage 16 поддержан.
8. Собери проект в Android Studio и проверь ручные сценарии из `VALIDATION_STAGE17.md` на копии базы прежде, чем использовать основную базу.
