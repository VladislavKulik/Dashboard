import com.example.lifedashboard.data.TaskRecurrenceCalculator
import com.example.lifedashboard.data.model.TaskRecurrence
import java.time.LocalDate

fun main() {
    check(TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 23), TaskRecurrence.DAILY) == LocalDate.of(2026, 11, 24))
    check(TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 23), TaskRecurrence.WEEKLY, setOf(1, 3, 5)) == LocalDate.of(2026, 11, 25))
    check(TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 27), TaskRecurrence.WEEKLY, setOf(1, 3, 5)) == LocalDate.of(2026, 11, 30))
    check(TaskRecurrenceCalculator.nextDate(LocalDate.of(2027, 1, 31), TaskRecurrence.MONTHLY, anchorDayOfMonth = 31) == LocalDate.of(2027, 2, 28))
    check(TaskRecurrenceCalculator.nextDate(LocalDate.of(2027, 2, 28), TaskRecurrence.MONTHLY, anchorDayOfMonth = 31) == LocalDate.of(2027, 3, 31))
    check(TaskRecurrenceCalculator.nextDate(LocalDate.of(2026, 11, 23), TaskRecurrence.NONE) == null)
    println("6 recurrence date assertions passed")
}
