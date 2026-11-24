package com.example.lifedashboard.ui.components
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
@Composable fun CircleIconButton(icon:ImageVector,contentDescription:String,onClick:()->Unit,modifier:Modifier=Modifier,background:Color=Color(0),tint:Color=Color(0),size:Dp=48.dp,iconSize:Dp=22.dp) {}
@Composable fun FilterPill(label:String,selected:Boolean,onClick:()->Unit,modifier:Modifier=Modifier,count:String?=null) {}
@Composable fun PastelCard(modifier:Modifier=Modifier,color:Color=Color(0),contentPadding:PaddingValues=PaddingValues(),verticalArrangement:Any=Any(),content:@Composable ColumnScope.()->Unit) { object:ColumnScope{}.content() }
@Composable fun ProgressTrack(progress:Float,modifier:Modifier=Modifier,trackColor:Color=Color(0),progressColor:Color=Color(0)) {}
@Composable fun ScreenTopBar(title:String,actionIcon:ImageVector?=null,actionDescription:String="",onActionClick:(()->Unit)?=null,onBackClick:(()->Unit)?=null) {}

@Composable fun TaskRow(title:String, completed:Boolean, onToggle:()->Unit, modifier:Modifier=Modifier, trailing:String?=null) {}
