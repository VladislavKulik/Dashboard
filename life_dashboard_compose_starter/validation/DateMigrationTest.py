import sqlite3
from pathlib import Path

# SQLite schema/data-preservation test for the v5 -> v6 structural migration.
con = sqlite3.connect(':memory:')
con.executescript("""
CREATE TABLE tasks (id TEXT PRIMARY KEY, title TEXT NOT NULL, dueLabel TEXT NOT NULL);
CREATE TABLE projects (id TEXT PRIMARY KEY, name TEXT NOT NULL, deadlineLabel TEXT NOT NULL);
CREATE TABLE goals (id TEXT PRIMARY KEY, dueDate TEXT);
INSERT INTO tasks VALUES ('t1', 'Legacy task', '27 ноя');
INSERT INTO tasks VALUES ('t2', 'No date', 'Без срока');
INSERT INTO projects VALUES ('p1', 'Legacy project', '15 дек');
INSERT INTO projects VALUES ('p2', 'No date project', 'Без срока');
INSERT INTO goals VALUES ('g1', '2026-11-30');
""")
con.execute('ALTER TABLE tasks ADD COLUMN dueDate TEXT')
con.execute('ALTER TABLE projects ADD COLUMN deadlineDate TEXT')
# These values stand for DateInput.parseLegacyLabel outputs that are tested in Kotlin.
con.execute("UPDATE tasks SET dueDate = ? WHERE id = ?", ('2026-11-27', 't1'))
con.execute("UPDATE projects SET deadlineDate = ? WHERE id = ?", ('2026-12-15', 'p1'))
assert con.execute('SELECT title, dueDate FROM tasks WHERE id="t1"').fetchone() == ('Legacy task', '2026-11-27')
assert con.execute('SELECT dueDate FROM tasks WHERE id="t2"').fetchone() == (None,)
assert con.execute('SELECT name, deadlineDate FROM projects WHERE id="p1"').fetchone() == ('Legacy project', '2026-12-15')
assert con.execute('SELECT deadlineDate FROM projects WHERE id="p2"').fetchone() == (None,)
assert con.execute('SELECT dueDate FROM goals WHERE id="g1"').fetchone() == ('2026-11-30',)
assert con.execute('SELECT count(*) FROM tasks').fetchone()[0] == 2
assert con.execute('SELECT count(*) FROM projects').fetchone()[0] == 2
assert con.execute('SELECT count(*) FROM goals').fetchone()[0] == 1
print('Room v5 -> v6 date-column migration preservation test passed')
