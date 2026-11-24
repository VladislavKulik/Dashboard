package androidx.compose.ui.graphics

data class Color(val value: Long) {
    fun copy(alpha: Float = 1f): Color = this
}
