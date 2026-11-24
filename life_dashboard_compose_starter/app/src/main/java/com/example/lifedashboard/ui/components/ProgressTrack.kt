package com.example.lifedashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun ProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = AppColors.White.copy(alpha = 0.72f),
    progressColor: Color = AppColors.Ink
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safeProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5.dp))
                .background(progressColor)
        )
    }
}
