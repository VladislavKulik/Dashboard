from pathlib import Path
root = Path(__file__).resolve().parents[1]
read = lambda rel: (root / rel).read_text(encoding='utf-8')

calendar_events = read('app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarEvents.kt')
calendar_screen = read('app/src/main/java/com/example/lifedashboard/feature/calendar/CalendarScreen.kt')
menu = read('app/src/main/java/com/example/lifedashboard/feature/menu/MenuScreen.kt')
app = read('app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt')
destinations = read('app/src/main/java/com/example/lifedashboard/navigation/Destinations.kt')
tasks = read('app/src/main/java/com/example/lifedashboard/feature/tasks/TasksScreen.kt')
goals = read('app/src/main/java/com/example/lifedashboard/feature/goals/GoalsScreen.kt')
db = read('app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt')
backup = read('app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt')

assert 'const val CALENDAR = "calendar"' in destinations
assert 'MenuItemUi("Календарь"' in menu
assert 'composable(Destinations.CALENDAR)' in app and 'CalendarScreen(' in app
assert 'CalendarEventFactory.build(' in calendar_screen
assert 'tasks = dashboardState.tasks' in app and 'activityEvents = dashboardState.activityEvents' in app
assert 'goals = dashboardState.goals' in app and 'habits = dashboardState.habits' in app
assert 'CalendarEventType.TASK' in app and 'CalendarEventType.PROJECT' in app
assert 'CalendarEventType.GOAL' in app and 'CalendarEventType.HABIT' in app and 'CalendarEventType.ACTIVITY' in app
assert 'initialTaskId = taskId' in app and 'initialGoalId = goalId' in app
assert 'animateScrollToItem' in tasks and 'highlighted = task.id == initialTaskId' in tasks
assert 'animateScrollToItem' in goals and 'highlighted = goal.id == initialGoalId' in goals
assert 'fun parseDateLabel(' in calendar_events
for token in ('послезавтра', 'завтра', 'вчера', 'через\\\\s+', 'monthIndex', 'dateKeyForMillis'):
    assert token in calendar_events, token
assert 'version = 8' in db and 'MIGRATION_4_5' in db
assert 'FORMAT_VERSION = 5' in backup
assert 'Room' not in calendar_events and '@Entity' not in calendar_events
assert 'rememberLazyListState' in tasks and 'rememberLazyListState' in goals
print('Stage 18 calendar integration checks passed')
