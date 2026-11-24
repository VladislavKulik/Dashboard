import com.example.lifedashboard.data.local.DateConverters
import java.time.LocalDate

fun main() {
    val converter = DateConverters()
    check(converter.fromLocalDate(LocalDate.of(2026, 11, 23)) == "2026-11-23")
    check(converter.toLocalDate("2026-02-28") == LocalDate.of(2026, 2, 28))
    check(converter.toLocalDate("2025-02-29") == null)
    check(converter.fromLocalDate(null) == null)
    check(converter.toLocalDate(null) == null)
    println("Room LocalDate converter round-trip tests passed")
}
