package androidx.compose.foundation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
class ScrollState
@Composable fun rememberScrollState()=ScrollState()
fun Modifier.horizontalScroll(state:ScrollState)=this
fun Modifier.verticalScroll(state:ScrollState)=this
fun Modifier.background(color:Color, shape:Any?=null)=this
fun Modifier.clickable(enabled:Boolean=true, role:Any?=null, onClick:()->Unit)=this
