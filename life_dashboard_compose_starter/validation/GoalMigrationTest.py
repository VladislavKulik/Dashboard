from pathlib import Path
import re
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt").read_text()
match = re.search(r'val MIGRATION_4_5 = object : Migration\(4, 5\) \{.*?database\.execSQL\(\s*"""(.*?)"""\s*\)', source, re.S)
assert match, "Could not locate Room 4 -> 5 migration SQL"
sql = match.group(1)

db = sqlite3.connect(":memory:")
db.executescript("""
CREATE TABLE projects (id TEXT PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE tasks (id TEXT PRIMARY KEY, title TEXT NOT NULL, projectId TEXT);
CREATE TABLE habits (id TEXT PRIMARY KEY, title TEXT NOT NULL, completionDates TEXT);
INSERT INTO projects VALUES ('p1', 'Existing project');
INSERT INTO tasks VALUES ('t1', 'Existing task', 'p1');
INSERT INTO habits VALUES ('h1', 'Existing habit', '2026-11-22');
""")
db.execute(sql)
columns = {row[1]: row for row in db.execute("PRAGMA table_info(goals)")}
expected = {"id", "title", "description", "dueDate", "currentValue", "targetValue", "unit", "projectId", "color", "sortOrder"}
assert expected.issubset(columns.keys())
assert columns["sortOrder"][4] == "0", columns["sortOrder"]
assert db.execute("SELECT name FROM projects WHERE id='p1'").fetchone()[0] == "Existing project"
assert db.execute("SELECT title FROM tasks WHERE id='t1'").fetchone()[0] == "Existing task"
assert db.execute("SELECT completionDates FROM habits WHERE id='h1'").fetchone()[0] == "2026-11-22"

db.execute("INSERT INTO goals (id,title,description,dueDate,currentValue,targetValue,unit,projectId,color) VALUES ('g1','Read 12 books','',NULL,2,12,'books','p1','BLUE')")
db.execute("UPDATE goals SET projectId = NULL WHERE projectId = ?", ("p1",))
row = db.execute("SELECT currentValue,targetValue,projectId,sortOrder FROM goals WHERE id='g1'").fetchone()
assert row == (2, 12, None, 0), row
print("Room migration 4 -> 5 SQLite preservation/default/detach test passed")
