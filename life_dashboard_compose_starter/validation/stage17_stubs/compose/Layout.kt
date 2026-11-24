package androidx.compose.foundation.layout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
class PaddingValues(val start:Dp=Dp(0f), val top:Dp=Dp(0f), val end:Dp=Dp(0f), val bottom:Dp=Dp(0f), horizontal:Dp?=null, vertical:Dp?=null)
object Arrangement { class Vertical; class Horizontal; fun spacedBy(value:Dp):Any=Any(); val End:Any=Any(); val Center:Any=Any() }
interface ColumnScope { fun Modifier.weight(value:Float):Modifier=this }
interface RowScope { fun Modifier.weight(value:Float):Modifier=this }
interface BoxScope { fun Modifier.align(alignment:Any):Modifier=this }
@Composable fun Column(modifier:Modifier=Modifier, verticalArrangement:Any=Any(), horizontalAlignment:Any=Any(), content:@Composable ColumnScope.()->Unit) { object:ColumnScope{}.content() }
@Composable fun Row(modifier:Modifier=Modifier, horizontalArrangement:Any=Any(), verticalAlignment:Any=Any(), content:@Composable RowScope.()->Unit) { object:RowScope{}.content() }
@Composable fun Box(modifier:Modifier=Modifier, contentAlignment:Any=Any(), content:@Composable BoxScope.()->Unit={}) { object:BoxScope{}.content() }
@Composable fun Spacer(modifier:Modifier=Modifier) {}
fun Modifier.fillMaxSize()=this
fun Modifier.fillMaxWidth(fraction:Float=1f)=this
fun Modifier.fillMaxHeight()=this
fun Modifier.height(value:Dp)=this
fun Modifier.heightIn(max:Dp)=this
fun Modifier.width(value:Dp)=this
fun Modifier.size(value:Dp)=this
fun Modifier.padding(all:Dp)=this
fun Modifier.padding(start:Dp=Dp(0f), top:Dp=Dp(0f), end:Dp=Dp(0f), bottom:Dp=Dp(0f), horizontal:Dp?=null, vertical:Dp?=null)=this
fun Modifier.padding(paddingValues:PaddingValues)=this
