package androidx.compose.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.RowScope

class TextStyle
class Typography {
    val titleMedium = TextStyle()
    val titleLarge = TextStyle()
    val bodySmall = TextStyle()
    val bodyMedium = TextStyle()
}
class ColorScheme { val error = Color(0) }
fun lightColorScheme(
    primary: Color, onPrimary: Color, secondary: Color, onSecondary: Color,
    tertiary: Color, onTertiary: Color, background: Color, onBackground: Color,
    surface: Color, onSurface: Color, surfaceVariant: Color, onSurfaceVariant: Color,
    outline: Color, error: Color
) = ColorScheme()
fun darkColorScheme(
    primary: Color, onPrimary: Color, secondary: Color, onSecondary: Color,
    tertiary: Color, onTertiary: Color, background: Color, onBackground: Color,
    surface: Color, onSurface: Color, surfaceVariant: Color, onSurfaceVariant: Color,
    outline: Color, error: Color
) = ColorScheme()
object MaterialTheme {
    val typography = Typography()
    val colorScheme = ColorScheme()
    @Composable operator fun invoke(colorScheme: ColorScheme, typography: Any, shapes: Any, content: @Composable () -> Unit) = content()
}
@Composable fun Text(text: String, modifier: Modifier = Modifier, color: Color = Color(0), style: TextStyle = TextStyle()) {}
@Composable fun Switch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {}
@Composable fun TextButton(onClick: () -> Unit, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) { object : RowScope {}.content() }
@Composable fun Button(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) { object : RowScope {}.content() }
