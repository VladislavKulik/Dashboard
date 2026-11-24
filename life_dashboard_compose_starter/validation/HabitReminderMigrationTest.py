"""SQLite smoke test for the Stage 13 Room migration (v3 -> v4)."""
import sqlite3

connection = sqlite3.connect(":memory:")
connection.execute(
    """CREATE TABLE habits (
        id TEXT NOT NULL PRIMARY KEY,
        title TEXT NOT NULL,
        completionDates TEXT NOT NULL,
        color TEXT NOT NULL,
        reminderLabel TEXT NOT NULL
    )"""
)
connection.executemany(
    "INSERT INTO habits (id, title, completionDates, color, reminderLabel) VALUES (?, ?, ?, ?, ?)",
    [
        ("habit-a", "Чтение", "2026-11-20,2026-11-21", "BLUE", "Каждый день"),
        ("habit-b", "Зарядка", "2026-11-22", "PINK", "По будням"),
    ],
)

connection.execute("ALTER TABLE `habits` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 0")
connection.execute("ALTER TABLE `habits` ADD COLUMN `reminderTime` TEXT NOT NULL DEFAULT '09:00'")
connection.execute("ALTER TABLE `habits` ADD COLUMN `reminderDays` TEXT NOT NULL DEFAULT '1,2,3,4,5,6,7'")

rows = connection.execute(
    "SELECT id, title, completionDates, reminderEnabled, reminderTime, reminderDays FROM habits ORDER BY id"
).fetchall()
assert rows == [
    ("habit-a", "Чтение", "2026-11-20,2026-11-21", 0, "09:00", "1,2,3,4,5,6,7"),
    ("habit-b", "Зарядка", "2026-11-22", 0, "09:00", "1,2,3,4,5,6,7"),
]

columns = {row[1]: row[4] for row in connection.execute("PRAGMA table_info(habits)")}
assert columns["reminderEnabled"] == "0"
assert columns["reminderTime"] == "'09:00'"
assert columns["reminderDays"] == "'1,2,3,4,5,6,7'"

print("PASS: v3 -> v4 keeps existing habits and completion history.")
print("PASS: legacy habits receive disabled reminders and valid default schedule fields.")
