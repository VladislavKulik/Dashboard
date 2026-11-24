package androidx.compose.foundation.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp

class PaddingValues(vararg values: Dp)
object Arrangement { fun spacedBy(value: Dp): Any = Any() }
interface ColumnScope { fun Modifier.weight(value: Float): Modifier = this }
interface RowScope { fun Modifier.weight(value: Float): Modifier = this }
@Composable fun Column(modifier: Modifier = Modifier, verticalArrangement: Any = Any(), content: @Composable ColumnScope.() -> Unit) {
    object : ColumnScope {}.content()
}
@Composable fun Row(modifier: Modifier = Modifier, horizontalArrangement: Any = Any(), verticalAlignment: Alignment.CenterVertically = Alignment.CenterVertically, content: @Composable RowScope.() -> Unit) {
    object : RowScope {}.content()
}
fun Modifier.fillMaxSize() = this
fun Modifier.fillMaxWidth() = this
fun Modifier.padding(all: Dp) = this
fun Modifier.padding(start: Dp, top: Dp, end: Dp, bottom: Dp) = this
