from pathlib import Path

root = Path(__file__).resolve().parents[1]
home = (root / "app/src/main/java/com/example/lifedashboard/feature/home/HomeScreen.kt").read_text()
nav = (root / "app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt").read_text()

checks = {
    "dashboard receives activity events": "activityEvents: List<DashboardActivityEvent>" in home,
    "activity chart uses seven days": "(6 downTo 0).map" in home,
    "activity chart derives counts from timestamps": "occurredAtEpochMillis" in home and "counts = days.map" in home,
    "recent activity is sorted newest first": "sortedByDescending { it.occurredAtEpochMillis }" in home,
    "empty activity state exists": "Здесь появится твоя активность" in home,
    "completion percentage is calculated": "completedToday * 100 / todayTasks.size" in home,
    "fake hours removed": 'StatPill("Часов", "14"' not in home,
    "sample activity text removed": "Задача выполнена\\n10:20" not in home and "Новая заметка\\nвчера" not in home,
    "calendar callback wired": "onOpenCalendar = { navController.navigate(Destinations.CALENDAR) }" in nav,
    "activity cards open calendar": "WeekActivityCard(weekEvents" in home and "EventsCard(recentEvents" in home,
}
for label, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {label}")
if not all(checks.values()):
    raise SystemExit(1)
