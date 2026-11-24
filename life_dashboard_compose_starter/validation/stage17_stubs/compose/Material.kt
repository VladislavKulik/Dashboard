package androidx.compose.material3
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardOptions
class TextStyle
class Typography { val bodySmall=TextStyle(); val bodyMedium=TextStyle(); val titleLarge=TextStyle() }
object MaterialTheme { val typography=Typography() }
@Composable fun Text(text:String, modifier:Modifier=Modifier, color:Color=Color(0), fontSize:TextUnit=TextUnit(14f), fontWeight:FontWeight?=null, textAlign:TextAlign?=null, maxLines:Int=Int.MAX_VALUE, style:TextStyle?=null) {}
@Composable fun Icon(imageVector:ImageVector, contentDescription:String?=null, tint:Color=Color(0), modifier:Modifier=Modifier) {}
@Composable fun Checkbox(checked:Boolean, onCheckedChange:((Boolean)->Unit)?, enabled:Boolean=true) {}
@Composable fun IconButton(onClick:()->Unit, enabled:Boolean=true, content:@Composable ()->Unit) { content() }
@Composable fun TextButton(onClick:()->Unit, enabled:Boolean=true, content:@Composable ()->Unit) { content() }
@Composable fun OutlinedTextField(value:String,onValueChange:(String)->Unit,label:(@Composable ()->Unit)?=null,modifier:Modifier=Modifier,singleLine:Boolean=false,minLines:Int=1,keyboardOptions:KeyboardOptions=KeyboardOptions(),placeholder:(@Composable ()->Unit)?=null,shape:Any?=null,readOnly:Boolean=false,trailingIcon:(@Composable ()->Unit)?=null,supportingText:(@Composable ()->Unit)?=null) { label?.invoke(); placeholder?.invoke(); trailingIcon?.invoke(); supportingText?.invoke() }
@Composable fun AlertDialog(onDismissRequest:()->Unit,title:@Composable ()->Unit,text:@Composable ()->Unit,confirmButton:@Composable ()->Unit,dismissButton:@Composable ()->Unit,containerColor:Color=Color(0)) { title();text();confirmButton();dismissButton() }

@RequiresOptIn annotation class ExperimentalMaterial3Api
class DatePickerState(var selectedDateMillis: Long? = null)
@Composable fun rememberDatePickerState(initialSelectedDateMillis: Long? = null) = DatePickerState(initialSelectedDateMillis)
@Composable fun DatePickerDialog(onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit, dismissButton: @Composable () -> Unit, content: @Composable () -> Unit) { content(); confirmButton(); dismissButton() }
@Composable fun DatePicker(state: DatePickerState) {}
