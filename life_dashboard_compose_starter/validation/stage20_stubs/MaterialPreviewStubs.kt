package androidx.compose.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Surface(color: Color, content: @Composable () -> Unit) = content()
