package androidx.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
class ScrollState
@Composable fun rememberScrollState() = ScrollState()
fun Modifier.verticalScroll(state: ScrollState) = this
