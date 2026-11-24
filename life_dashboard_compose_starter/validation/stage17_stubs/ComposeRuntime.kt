package androidx.compose.runtime
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.PROPERTY_GETTER)
annotation class Composable
class MutableState<T>(var value:T)
operator fun <T> MutableState<T>.getValue(thisRef:Any?, property:kotlin.reflect.KProperty<*>):T = value
operator fun <T> MutableState<T>.setValue(thisRef:Any?, property:kotlin.reflect.KProperty<*>, newValue:T) { value = newValue }
fun <T> mutableStateOf(value:T) = MutableState(value)
@Composable fun <T> remember(vararg keys:Any?, calculation:()->T):T = calculation()

@Composable fun LaunchedEffect(vararg keys:Any?, block:suspend ()->Unit) {}
