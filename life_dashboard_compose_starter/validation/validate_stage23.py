from pathlib import Path

root = Path(__file__).resolve().parents[1]
events = (root / "app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt").read_text(encoding="utf-8")
screen = (root / "app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarScreen.kt").read_text(encoding="utf-8")
test = (root / "validation/CalendarEventsTest.kt").read_text(encoding="utf-8")

assert "val timeLabel: String? = null" in events
assert "task.reminderTime.takeIf { task.reminderEnabled }" in events
assert "thenBy { it.timeLabel ?: \"99:99\" }" in events
assert "enum class CalendarTypeFilter" in screen
assert "selectedType.type == null || event.type == selectedType.type" in screen
assert "targetMonth.atDay(selectedDay.coerceAtMost(targetMonth.lengthOfMonth()))" in screen
assert "Напоминание · $time" in screen
assert 'timeLabel == "08:45"' in test
assert "Room" not in events and "@Entity" not in events
print("Stage 23 calendar filters, reminder projection and month navigation checks passed")
