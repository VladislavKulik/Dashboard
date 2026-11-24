package androidx.compose.ui.graphics
data class Color(val value:Long) { fun copy(alpha:Float=1f)=this; companion object { val Transparent=Color(0L) } }
fun Color.toArgb(): Int = value.toInt()
