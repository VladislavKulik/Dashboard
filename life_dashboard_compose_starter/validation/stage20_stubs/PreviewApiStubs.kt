package androidx.compose.ui.tooling.preview

@Target(AnnotationTarget.FUNCTION)
annotation class Preview(
    val name: String = "",
    val showBackground: Boolean = false,
    val widthDp: Int = -1,
    val heightDp: Int = -1
)
