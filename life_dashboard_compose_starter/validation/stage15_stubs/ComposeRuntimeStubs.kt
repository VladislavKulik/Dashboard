package androidx.compose.runtime

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.PROPERTY_GETTER)
annotation class Composable

class ProvidedValue<T>(val value: T)
class CompositionLocal<T>(val defaultFactory: () -> T) {
    val current: T get() = defaultFactory()
    infix fun provides(value: T) = ProvidedValue(value)
}
fun <T> staticCompositionLocalOf(defaultFactory: () -> T) = CompositionLocal(defaultFactory)
@Composable fun CompositionLocalProvider(vararg values: ProvidedValue<*>, content: @Composable () -> Unit) = content()
