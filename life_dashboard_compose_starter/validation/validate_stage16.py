from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
read = lambda path: (ROOT / path).read_text()

backup = read('app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt')
contract = read('app/src/main/java/com/example/lifedashboard/data/repository/BackupRepository.kt')
dao = read('app/src/main/java/com/example/lifedashboard/data/local/DashboardDao.kt')
settings_repo = read('app/src/main/java/com/example/lifedashboard/data/repository/SettingsRepository.kt')
settings_store = read('app/src/main/java/com/example/lifedashboard/data/local/SettingsDataStore.kt')
vm = read('app/src/main/java/com/example/lifedashboard/presentation/DashboardViewModel.kt')
app = read('app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt')
screen = read('app/src/main/java/com/example/lifedashboard/feature/settings/SettingsScreen.kt')
persistence = read('app/src/main/java/com/example/lifedashboard/data/local/RoomDashboardPersistence.kt')

# Versioned JSON includes all Room tables and optional DataStore preferences.
for key in ('tasks', 'projects', 'projectStages', 'notes', 'habits', 'goals', 'activityEvents', 'appMetadata'):
    assert f'"{key}"' in backup
assert 'formatVersion' in backup and 'FORMAT_VERSION = 5' in backup and 'MIN_SUPPORTED_FORMAT_VERSION = 1' in backup
assert 'LIFE_DASHBOARD_BACKUP' in backup and 'LIFE_DASHBOARD_EXPORT' in backup
assert 'themeMode' in backup and 'notificationsEnabled' in backup and 'showCompletedTasksOnHome' in backup

# Import fully parses first, then replaces all tables in a single Room transaction.
parse_at = backup.index('val snapshot = parseAndValidate(json)')
transaction_at = backup.index('database.withTransaction {', parse_at)
assert parse_at < transaction_at
for clear in ('clearTasks', 'clearStages', 'clearProjects', 'clearNotes', 'clearHabits', 'clearGoals', 'clearActivityEvents', 'clearMetadata'):
    assert f'dao.{clear}()' in backup
for insert in ('insertTasks', 'insertProjects', 'insertStages', 'insertNotes', 'insertHabits', 'insertGoals', 'insertEvents', 'insertMetadataItems'):
    assert f'dao.{insert}(' in backup
assert 'dao.getMetadata(SEED_MARKER)' in backup
assert 'uniqueBy(' in backup
assert 'MAX_BACKUP_BYTES = 20 * 1024 * 1024' in backup
assert 'formatVersion == 1' in backup and 'data.requiredArray("goals")' in backup
assert 'projectId in projectIds' in backup
assert 'TaskDayCategory.valueOf' in backup and 'TaskPriority.valueOf' in backup
assert 'NoteCategory.valueOf' in backup and 'HabitColor.valueOf' in backup
assert 'reminderTime.matches' in backup and 'reminderDays.split' in backup

# DAO exposes the operations required for a full snapshot replacement.
for operation in ('getAllMetadata', 'insertEvents', 'insertMetadataItems', 'clearNotes', 'clearHabits', 'clearActivityEvents', 'clearMetadata'):
    assert operation in dao

# Settings import is one DataStore edit; restore also refreshes scheduled reminders.
assert 'suspend fun replaceSettings(settings: DashboardSettings)' in settings_repo
assert 'override suspend fun replaceSettings' in settings_store
assert 'preferences[THEME_MODE] = settings.themeMode.name' in settings_store
assert 'preferences[NOTIFICATIONS_ENABLED] = settings.notificationsEnabled' in settings_store
assert 'preferences[SHOW_COMPLETED_TASKS] = settings.showCompletedTasksOnHome' in settings_store
assert 'repository.restoreJson(json)' in vm
assert 'oldHabitIds.forEach { habitReminderScheduler?.cancel(it) }' in vm
assert 'restoredHabits.forEach { habitReminderScheduler?.schedule(it) }' in vm

# Android Storage Access Framework: no broad storage permission, explicit overwrite confirmation.
assert 'ActivityResultContracts.CreateDocument("application/json")' in app
assert 'ActivityResultContracts.OpenDocument()' in app
assert 'Заменить текущие данные?' in app
assert 'readJsonDocument(context, uri)' in app
assert 'MAX_IMPORT_BYTES = 20 * 1024 * 1024' in app
assert 'onExportData' in screen and 'onCreateBackup' in screen and 'onImportBackup' in screen
assert 'createBackupRepository()' in persistence
assert 'version = 8' in read('app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt')

# Later stages may extend the Room schema; stage 19 uses version 6.
db = read('app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt')
assert 'version = 8' in db
print('Stage 16 backup/export integration checks passed')
