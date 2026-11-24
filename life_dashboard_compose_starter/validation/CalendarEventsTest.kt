import com.example.lifedashboard.data.model.*
import com.example.lifedashboard.feature.calendar.CalendarEventFactory
import com.example.lifedashboard.feature.calendar.CalendarEventType
import java.util.Calendar
import java.time.LocalDate

fun main() {
    val now = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.NOVEMBER, 23, 12, 0, 0)
    }.timeInMillis

    check(CalendarEventFactory.parseDateLabel("Сегодня", now) == "2026-11-23")
    check(CalendarEventFactory.parseDateLabel("Завтра", now) == "2026-11-24")
    check(CalendarEventFactory.parseDateLabel("Послезавтра", now) == "2026-11-25")
    check(CalendarEventFactory.parseDateLabel("через 3 дня", now) == "2026-11-26")
    check(CalendarEventFactory.parseDateLabel("27 ноя", now) == "2026-11-27")
    check(CalendarEventFactory.parseDateLabel("27 ноя.", now) == "2026-11-27")
    check(CalendarEventFactory.parseDateLabel("27 ноября 2026", now) == "2026-11-27")
    check(CalendarEventFactory.parseDateLabel("15 дек", now) == "2026-12-15")
    check(CalendarEventFactory.parseDateLabel("8 янв", now) == "2027-01-08")
    check(CalendarEventFactory.parseDateLabel("05.11", now) == "2026-11-05")
    check(CalendarEventFactory.parseDateLabel("2026-11-28", now) == "2026-11-28")
    check(CalendarEventFactory.parseDateLabel("29.02.2025", now) == null)
    check(CalendarEventFactory.parseDateLabel("Без срока", now) == null)

    val activityTime = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.NOVEMBER, 22, 18, 30, 0)
    }.timeInMillis
    val events = CalendarEventFactory.build(
        tasks = listOf(DashboardTask("t1", "Task", dueLabel = "Завтра", projectId = "p1", reminderEnabled = true, reminderTime = "08:45")),
        projects = listOf(DashboardProject("p1", "Project", "", "27 ноя", "", 0.2f, 0, 1, 0)),
        goals = listOf(DashboardGoal("g1", "Goal", dueDate = LocalDate.of(2026, 11, 28))),
        habits = listOf(DashboardHabit("h1", "Habit", completionDates = setOf("2026-11-23"))),
        activityEvents = listOf(DashboardActivityEvent("a1", "TASK_COMPLETION", "t1", activityTime, true)),
        nowMillis = now
    )
    check(events.size == 5)
    check(events.any { it.type == CalendarEventType.TASK && it.dateKey == "2026-11-24" && it.projectId == "p1" && it.timeLabel == "08:45" })
    check(events.any { it.type == CalendarEventType.PROJECT && it.dateKey == "2026-11-27" })
    check(events.any { it.type == CalendarEventType.GOAL && it.dateKey == "2026-11-28" })
    check(events.any { it.type == CalendarEventType.HABIT && it.dateKey == "2026-11-23" })
    check(events.any { it.type == CalendarEventType.ACTIVITY && it.dateKey == "2026-11-22" })
    check(events == events.sortedBy { it.dateKey })

    val november = CalendarEventFactory.monthGrid(2026, Calendar.NOVEMBER)
    check(november.size == 42)
    check(november.filterNotNull().first() == "2026-11-01")
    check(november.filterNotNull().last() == "2026-11-30")
    println("Calendar parser, event aggregation and month grid tests passed")
}
