package androidx.compose.ui.unit
data class Dp(val value:Float)
data class TextUnit(val value:Float)
val Int.dp get()=Dp(toFloat())
val Float.dp get()=Dp(this)
val Int.sp get()=TextUnit(toFloat())
val Float.sp get()=TextUnit(this)
