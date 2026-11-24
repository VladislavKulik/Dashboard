from pathlib import Path
import sqlite3
ROOT = Path(__file__).resolve().parents[1]
model = (ROOT/'app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt').read_text()
entity = (ROOT/'app/src/main/java/com/example/lifedashboard/data/local/DashboardEntities.kt').read_text()
db = (ROOT/'app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt').read_text()
persistence = (ROOT/'app/src/main/java/com/example/lifedashboard/data/local/RoomDashboardPersistence.kt').read_text()
backup = (ROOT/'app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt').read_text()
ui = (ROOT/'app/src/main/java/com/example/lifedashboard/feature/tasks/TasksScreen.kt').read_text()
assert all(x in model for x in ('DAILY("Каждый день")','WEEKLY("Каждую неделю")','MONTHLY("Каждый месяц")'))
assert all(x in entity for x in ('recurrence: String','recurrenceDays: String','recurrenceSeriesId: String?','recurrenceAnchorDay: Int'))
assert 'version = 8' in db and 'MIGRATION_6_7' in db and 'MIGRATION_7_8' in db
assert 'TaskRecurrenceCalculator.nextDate' in persistence and 'TASK_SKIPPED' in persistence
assert 'FORMAT_VERSION = 5' in backup and 'formatVersion >= 4' in backup
assert 'Пропустить повторение?' in ui and 'Повторение' in ui
# Exercise the exact SQL operations from migration 6 -> 7.
conn = sqlite3.connect(':memory:')
conn.execute('''CREATE TABLE tasks (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, trailingLabel TEXT, dueLabel TEXT NOT NULL, dueDate TEXT, projectId TEXT, dayCategory TEXT NOT NULL, priority TEXT NOT NULL, isCompleted INTEGER NOT NULL, showOnHome INTEGER NOT NULL, sortOrder INTEGER NOT NULL DEFAULT 0)''')
conn.execute("INSERT INTO tasks(id,title,dueLabel,dueDate,dayCategory,priority,isCompleted,showOnHome,sortOrder) VALUES ('existing','Existing','27 ноя','2026-11-27','UPCOMING','MEDIUM',0,0,4)")
for sql in [
    "ALTER TABLE `tasks` ADD COLUMN `recurrence` TEXT NOT NULL DEFAULT 'NONE'",
    "ALTER TABLE `tasks` ADD COLUMN `recurrenceDays` TEXT NOT NULL DEFAULT ''",
    "ALTER TABLE `tasks` ADD COLUMN `recurrenceSeriesId` TEXT",
    "ALTER TABLE `tasks` ADD COLUMN `recurrenceAnchorDay` INTEGER NOT NULL DEFAULT 0",
]: conn.execute(sql)
row = conn.execute('SELECT id,title,dueDate,recurrence,recurrenceDays,recurrenceSeriesId,recurrenceAnchorDay FROM tasks').fetchone()
assert row == ('existing','Existing','2026-11-27','NONE','',None,0), row
print('Stage 21 static validation and SQLite migration 6→7 passed.')
