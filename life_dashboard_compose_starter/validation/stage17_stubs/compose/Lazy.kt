package androidx.compose.foundation.lazy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.PaddingValues
class LazyListState { suspend fun animateScrollToItem(index:Int) {} }
fun rememberLazyListState() = LazyListState()
interface LazyListScope {
 fun item(key:Any?=null, content:@Composable ()->Unit) {}
 fun <T> items(items:List<T>, key:((T)->Any)?=null, itemContent:@Composable (T)->Unit) {}
}
@Composable fun LazyColumn(state:LazyListState=LazyListState(), modifier:Modifier=Modifier, contentPadding:PaddingValues=PaddingValues(), verticalArrangement:Any=Any(), content:LazyListScope.()->Unit) { object:LazyListScope{}.content() }
fun <T> LazyListScope.items(items:List<T>, key:((T)->Any)?=null, itemContent:@Composable (T)->Unit) {}
