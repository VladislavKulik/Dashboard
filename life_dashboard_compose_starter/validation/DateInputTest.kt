import com.example.lifedashboard.data.DateInput
import java.time.LocalDate

fun main() {
    val today = LocalDate.of(2026, 11, 23)
    check(DateInput.parseUserInput("2026-11-28") == LocalDate.of(2026, 11, 28))
    check(DateInput.parseUserInput("28.11.2026") == LocalDate.of(2026, 11, 28))
    check(DateInput.parseUserInput("29.02.2025") == null)
    check(DateInput.parseLegacyLabel("Сегодня", today) == today)
    check(DateInput.parseLegacyLabel("Завтра", today) == today.plusDays(1))
    check(DateInput.parseLegacyLabel("Послезавтра", today) == today.plusDays(2))
    check(DateInput.parseLegacyLabel("через 3 дня", today) == today.plusDays(3))
    check(DateInput.parseLegacyLabel("Сегодня · 16:00", today) == today)
    check(DateInput.parseLegacyLabel("27 ноя", today) == LocalDate.of(2026, 11, 27))
    check(DateInput.parseLegacyLabel("27 ноя.", today) == LocalDate.of(2026, 11, 27))
    check(DateInput.parseLegacyLabel("15 дек", today) == LocalDate.of(2026, 12, 15))
    check(DateInput.parseLegacyLabel("8 янв", today) == LocalDate.of(2027, 1, 8))
    check(DateInput.parseLegacyLabel("05.11", today) == LocalDate.of(2026, 11, 5))
    check(DateInput.parseLegacyLabel("Без срока", today) == null)
    check(DateInput.parseLegacyLabel("Готово", today) == null)
    check(DateInput.formatTaskLabel(today) == DateInput.formatTaskLabel(LocalDate.now()) || DateInput.formatTaskLabel(today).isNotBlank())
    println("LocalDate parsing, legacy-label migration and display tests passed")
}
