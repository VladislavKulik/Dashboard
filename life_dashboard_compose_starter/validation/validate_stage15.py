from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
read = lambda path: (ROOT / path).read_text()

settings_repo = read('app/src/main/java/com/example/lifedashboard/data/repository/SettingsRepository.kt')
settings_store = read('app/src/main/java/com/example/lifedashboard/data/local/SettingsDataStore.kt')
settings_screen = read('app/src/main/java/com/example/lifedashboard/feature/settings/SettingsScreen.kt')
vm = read('app/src/main/java/com/example/lifedashboard/presentation/DashboardViewModel.kt')
app = read('app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt')
home = read('app/src/main/java/com/example/lifedashboard/feature/home/HomeScreen.kt')
receiver = read('app/src/main/java/com/example/lifedashboard/reminders/HabitReminderReceiver.kt')
rescheduler = read('app/src/main/java/com/example/lifedashboard/reminders/HabitReminderRescheduleReceiver.kt')
main_activity = read('app/src/main/java/com/example/lifedashboard/MainActivity.kt')
habits_ui = read('app/src/main/java/com/example/lifedashboard/feature/habits/HabitsScreen.kt')
notes_ui = read('app/src/main/java/com/example/lifedashboard/feature/notes/NotesScreen.kt')
notes_details_ui = read('app/src/main/java/com/example/lifedashboard/feature/notes/NoteDetailsScreen.kt')
analytics_ui = read('app/src/main/java/com/example/lifedashboard/feature/analytics/AnalyticsScreen.kt')

assert 'fun observeSettings(): Flow<DashboardSettings>' in settings_repo
assert 'preferencesDataStore' in settings_store
assert 'setThemeMode' in settings_store and 'setNotificationsEnabled' in settings_store
assert 'setShowCompletedTasksOnHome' in settings_store
assert 'DashboardThemeMode.SYSTEM' in settings_store
assert 'ThemeModeOption("Система"' in settings_screen
assert 'ThemeModeOption("Светлая"' in settings_screen
assert 'ThemeModeOption("Тёмная"' in settings_screen
assert 'Напоминания о привычках' in settings_screen
assert 'Показывать выполненные задачи' in settings_screen
assert 'settingsRepository.observeSettings().collect' in vm
assert 'syncHabitReminders(settings.notificationsEnabled)' in vm
assert 'settings.notificationsEnabled' in vm
assert 'SettingsScreen(' in app and 'PlaceholderScreen("Настройки"' not in app
assert 'LifeDashboardTheme(darkTheme = useDarkTheme)' in app
assert 'showCompletedTasks = appSettings.showCompletedTasksOnHome' in app
assert 'showCompletedTasks || !it.isCompleted' in home
assert 'observeSettings().first().notificationsEnabled' in receiver
assert 'observeSettings().first().notificationsEnabled' in rescheduler
assert 'LifeDashboardApp()' in main_activity and 'LifeDashboardTheme' not in main_activity
assert '@Composable\nfun habitColor' in habits_ui
assert '@Composable\nprivate fun noteColor' in notes_ui and '@Composable\nprivate fun noteColor' in notes_details_ui
assert 'val lineColor = AppColors.Ink' in analytics_ui and 'color = lineColor' in analytics_ui
# Settings stage did not alter the schema; later stages may advance it.
db = read('app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt')
assert any(f'version = {version}' in db for version in range(4, 10))
print('Stage 15 source integration checks passed')
