from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8')
models = read('app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt')
entities = read('app/src/main/java/com/example/lifedashboard/data/local/DashboardEntities.kt')
db = read('app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt')
persistence = read('app/src/main/java/com/example/lifedashboard/data/local/RoomDashboardPersistence.kt')
backup = read('app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt')
tasks = read('app/src/main/java/com/example/lifedashboard/feature/tasks/TasksScreen.kt')
projects = read('app/src/main/java/com/example/lifedashboard/feature/projects/ProjectEditorDialog.kt')
goals = read('app/src/main/java/com/example/lifedashboard/feature/goals/GoalsScreen.kt')
calendar = read('app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt')
calendar_screen = read('app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarScreen.kt')
date_field = read('app/src/main/java/com/example/lifedashboard/ui/components/LocalDatePickerField.kt')
assert 'val dueDate: LocalDate?' in models and 'val deadlineDate: LocalDate?' in models
assert 'val dueDate: LocalDate?' in entities and 'val deadlineDate: LocalDate?' in entities
assert '@TypeConverters(DateConverters::class)' in db and 'version = 8' in db and 'MIGRATION_5_6' in db
assert 'DateConverters::class' in db and 'LocalDate.parse' in read('app/src/main/java/com/example/lifedashboard/data/local/DateConverters.kt')
assert 'dueDate = dueDate' in persistence and 'deadlineDate = deadlineDate' in persistence
assert 'FORMAT_VERSION = 5' in backup and 'formatVersion >= 3' in backup
assert 'dueDate?.toString()' in backup and 'deadlineDate?.toString()' in backup
assert 'LocalDatePickerField(' in tasks and 'LocalDatePickerField(' in projects and 'LocalDatePickerField(' in goals
assert 'TaskFilter.OVERDUE' in tasks and 'CalendarPeriod.NEXT_7_DAYS' in calendar_screen and 'CalendarPeriod.OVERDUE' in calendar_screen
assert 'task.dueDate?.toString()' in calendar and 'project.deadlineDate?.toString()' in calendar
assert 'DatePickerDialog' in date_field and 'rememberDatePickerState' in date_field
assert 'MIGRATION_5_6' in persistence
print('Stage 19 date-model, picker, filters, backup and integration checks passed')
