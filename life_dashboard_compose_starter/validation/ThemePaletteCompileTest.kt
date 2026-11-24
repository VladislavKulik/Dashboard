import androidx.compose.runtime.Composable
import com.example.lifedashboard.ui.theme.AppColors
import com.example.lifedashboard.ui.theme.LifeDashboardTheme

@Composable
fun ThemePaletteCompileTest() {
    LifeDashboardTheme(darkTheme = true) {
        val background = AppColors.Background
        val foreground = AppColors.Ink
        check(background != foreground)
    }
}
