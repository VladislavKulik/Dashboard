package com.example.lifedashboard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.ui.theme.AppColors

/** Unified circular FAB with consistent 6.dp shadow across all screens. */
@Composable
fun AppFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.Yellow,
    contentColor: Color = AppColors.Ink,
    size: Dp = 60.dp,
    iconSize: Dp = 26.dp,
    shadowElevation: Dp = 6.dp
) {
    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        color = containerColor,
        shape = CircleShape,
        shadowElevation = shadowElevation,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/** Unified pill-shaped Extended FAB with the same 6.dp shadow. */
@Composable
fun AppExtendedFab(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.Yellow,
    contentColor: Color = AppColors.Ink,
    shadowElevation: Dp = 6.dp
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        color = containerColor,
        shape = CircleShape,
        shadowElevation = shadowElevation,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}