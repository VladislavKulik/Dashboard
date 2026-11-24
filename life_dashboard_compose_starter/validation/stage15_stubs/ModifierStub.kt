package androidx.compose.ui

import androidx.compose.ui.unit.Dp

open class Modifier { companion object : Modifier() }
fun Modifier.fillMaxSize() = this
fun Modifier.fillMaxWidth() = this
fun Modifier.padding(all: Dp) = this
fun Modifier.padding(start: Dp, top: Dp, end: Dp, bottom: Dp) = this
