from pathlib import Path
root = Path(__file__).resolve().parents[1]
home = (root / 'app/src/main/java/com/example/lifedashboard/feature/home/HomeScreen.kt').read_text()
nav = (root / 'app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt').read_text()
checks = {
    'Home shortcut section': 'Быстрый доступ' in home,
    'Tasks shortcut': 'title = "Задачи"' in home and 'onClick = onOpenTasks' in home,
    'Notes shortcut': 'title = "Заметки"' in home and 'onClick = onOpenNotes' in home,
    'Habits shortcut': 'title = "Привычки"' in home and 'onClick = onOpenHabits' in home,
    'Notes navigation wiring': 'onOpenNotes = { navController.navigate(Destinations.NOTES) }' in nav,
    'Habits navigation wiring': 'onOpenHabits = { navController.navigate(Destinations.HABITS) }' in nav,
    'No new persistence introduced': 'Room' not in (root / 'STAGE25_QUICK_ACCESS.md').read_text(),
}
for name, passed in checks.items():
    print(('PASS' if passed else 'FAIL') + ': ' + name)
if not all(checks.values()):
    raise SystemExit(1)
