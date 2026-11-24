from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = '{http://schemas.android.com/apk/res/android}'

manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
permissions = {node.attrib.get(ANDROID + 'name') for node in manifest.findall('uses-permission')}
required_permissions = {
    'android.permission.POST_NOTIFICATIONS',
    'android.permission.RECEIVE_BOOT_COMPLETED',
    'android.permission.SCHEDULE_EXACT_ALARM',
}
assert required_permissions <= permissions

receivers = {
    node.attrib.get(ANDROID + 'name'): node
    for node in manifest.findall('./application/receiver')
}
assert '.reminders.HabitReminderReceiver' in receivers
rescheduler = receivers['.reminders.HabitReminderRescheduleReceiver']
actions = {
    action.attrib.get(ANDROID + 'name')
    for intent_filter in rescheduler.findall('intent-filter')
    for action in intent_filter.findall('action')
}
assert {
    'android.intent.action.BOOT_COMPLETED',
    'android.intent.action.TIME_SET',
    'android.intent.action.TIMEZONE_CHANGED',
    'android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED',
} <= actions

vm = (ROOT / 'app/src/main/java/com/example/lifedashboard/presentation/DashboardViewModel.kt').read_text()
assert 'habitReminderScheduler?.schedule(normalized)' in vm
assert 'habitReminderScheduler?.cancel(normalized.id)' in vm
assert 'habitReminderScheduler?.cancel(habitId)' in vm
assert 'syncHabitReminders(settings.notificationsEnabled)' in vm
assert 'enabledHabits.forEach(scheduler::schedule)' in vm

repo = (ROOT / 'app/src/main/java/com/example/lifedashboard/data/repository/HabitsRepository.kt').read_text()
dao = (ROOT / 'app/src/main/java/com/example/lifedashboard/data/local/DashboardDao.kt').read_text()
assert 'suspend fun getEnabledHabits()' in repo
assert 'WHERE reminderEnabled = 1' in dao

app = (ROOT / 'app/src/main/java/com/example/lifedashboard/navigation/LifeDashboardApp.kt').read_text()
assert 'ActivityResultContracts.RequestPermission()' in app
assert 'Manifest.permission.POST_NOTIFICATIONS' in app
assert 'Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM' in app

# This historical check remains valid after forward schema migrations.
db = (ROOT / 'app/src/main/java/com/example/lifedashboard/data/local/LifeDashboardDatabase.kt').read_text()
assert any(f'version = {version}' in db for version in range(4, 10))
assert 'MIGRATION_3_4' in db

print('Stage 14 source/manifest checks passed')
