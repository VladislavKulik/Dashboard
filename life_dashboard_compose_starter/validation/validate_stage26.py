from pathlib import Path

root = Path(__file__).resolve().parents[1]
nav = (root / "app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt").read_text()
checks = {
    "task quick-create route": 'QuickAddType.TASK -> "${Destinations.TASKS}?create=true"' in nav and 'startWithCreate = createTask' in nav,
    "project quick-create route": 'QuickAddType.PROJECT -> "${Destinations.PROJECTS}?create=true"' in nav and 'startWithCreate = entry.arguments?.getBoolean("create") == true' in nav,
    "habit quick-create route": 'QuickAddType.HABIT -> "${Destinations.HABITS}?create=true"' in nav,
    "goal quick-create route": 'QuickAddType.GOAL -> "${Destinations.GOALS}?create=true"' in nav,
    "note editor route": 'QuickAddType.NOTE -> "${Destinations.NOTE_DETAILS}/new"' in nav,
}
for name, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {name}")
if not all(checks.values()):
    raise SystemExit(1)

for rel in [
    "app/src/main/java/com/example/lifedashboard/feature/tasks/TasksScreen.kt",
    "app/src/main/java/com/example/lifedashboard/feature/projects/ProjectsScreen.kt",
    "app/src/main/java/com/example/lifedashboard/feature/habits/HabitsScreen.kt",
    "app/src/main/java/com/example/lifedashboard/feature/goals/GoalsScreen.kt",
]:
    source = (root / rel).read_text()
    assert "startWithCreate: Boolean = false" in source, rel
print("PASS: all four existing editors are controlled by the creation argument")
