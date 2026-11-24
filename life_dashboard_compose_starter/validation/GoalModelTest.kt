import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.GoalColor
import com.example.lifedashboard.data.DateKeys

fun main() {
    val empty = DashboardGoal("a", "Read", currentValue = 0, targetValue = 10)
    check(empty.progress == 0f)
    check(!empty.isCompleted)

    val half = empty.copy(currentValue = 5, color = GoalColor.MINT)
    check(half.progress == 0.5f)
    check(!half.isCompleted)

    val complete = half.copy(currentValue = 10)
    check(complete.progress == 1f)
    check(complete.isCompleted)

    val overTarget = empty.copy(currentValue = 20)
    check(overTarget.progress == 1f)
    check(overTarget.isCompleted)

    check(DateKeys.isValidDateKey("2024-02-29"))
    check(DateKeys.isValidDateKey("2026-11-23"))
    check(!DateKeys.isValidDateKey("2025-02-29"))
    check(!DateKeys.isValidDateKey("2026-13-01"))
    check(!DateKeys.isValidDateKey("23.11.2026"))
    println("Goal model progress/completion and strict date tests passed")
}
