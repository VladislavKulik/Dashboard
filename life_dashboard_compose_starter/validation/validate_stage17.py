from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text()

models = read('app/src/main/java/com/example/lifedashboard/data/model/DashboardModels.kt')
entity = read('app/src/main/java/com/example/lifedashboard/data/local/DashboardEntities.kt')
dao = read('app/src/main/java/com/example/lifedashboard/data/local/DashboardDao.kt')
db = read('app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt')
persistence = read('app/src/main/java/com/example/lifedashboard/data/local/RoomDashboardPersistence.kt')
vm = read('app/src/main/java/com/example/lifedashboard/presentation/DashboardViewModel.kt')
state = read('app/src/main/java/com/example/lifedashboard/presentation/DashboardUiState.kt')
app = read('app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt')
ui = read('app/src/main/java/com/example/lifedashboard/feature/goals/GoalsScreen.kt')
backup = read('app/src/main/java/com/example/lifedashboard/data/backup/RoomDashboardBackupRepository.kt')
backup_contract = read('app/src/main/java/com/example/lifedashboard/data/repository/BackupRepository.kt')
menu = read('app/src/main/java/com/example/lifedashboard/feature/menu/MenuScreen.kt')

assert 'data class DashboardGoal' in models and 'enum class GoalColor' in models
assert 'val progress: Float' in models and 'val isCompleted: Boolean' in models
assert '@Entity(tableName = "goals")' in entity and 'val sortOrder: Int = 0' in entity
assert 'GoalEntity::class' in db and 'version = 8' in db and 'MIGRATION_4_5' in db
for op in ('observeGoals', 'getGoals', 'getGoal', 'insertGoal', 'insertGoals', 'deleteGoal', 'clearGoals', 'detachGoalsFromProject'):
    assert op in dao
assert 'GoalsRepository' in persistence and 'override suspend fun saveGoal' in persistence
assert 'override suspend fun changeProgress' in persistence
assert 'targetValue !in 1..1_000_000' in persistence
assert 'dao.detachGoalsFromProject(projectId)' in persistence
assert 'goals: List<DashboardGoal>' in state
assert 'GoalsRepository?' in vm and 'fun saveGoal' in vm and 'fun deleteGoal' in vm and 'fun changeGoalProgress' in vm
assert 'goalsRepository?.observeGoals()' in vm
assert 'GoalsScreen(' in app and 'dashboardViewModel::saveGoal' in app
assert 'goalCount = dashboardState.goals.size' in app
assert '$completedGoalCount из $goalCount достигнуто' in menu
assert 'formatVersion == 1' in backup and 'FORMAT_VERSION = 5' in backup
assert '.put("goals"' in backup and 'data.requiredArray("goals")' in backup
assert 'MIN_SUPPORTED_FORMAT_VERSION = 1' in backup
assert 'dao.clearGoals()' in backup and 'dao.insertGoals(snapshot.goals)' in backup
assert 'goalCount = snapshot.goals.size' in backup_contract or 'goalCount: Int = 0' in backup_contract
assert 'LocalDatePickerField'  in ui
print('Stage 17 integration checks passed')
