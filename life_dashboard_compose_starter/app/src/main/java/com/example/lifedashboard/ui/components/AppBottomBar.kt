package com.example.lifedashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.lifedashboard.ui.theme.AppColors

enum class RootDestination { HOME, PROJECTS, MENU }

@Composable
fun AppBottomBar(
    selected: RootDestination,
    onHomeClick: () -> Unit,
    onProjectsClick: () -> Unit,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(68.dp),
        color = AppColors.White,
        shape = RoundedCornerShape(34.dp),
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomBarItem(
                icon = Icons.Outlined.Home,
                label = "Главная",
                selected = selected == RootDestination.HOME,
                onClick = onHomeClick
            )
            BottomBarItem(
                icon = Icons.Outlined.FolderOpen,
                label = "Проекты",
                selected = selected == RootDestination.PROJECTS,
                onClick = onProjectsClick
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AppColors.Yellow)
                    .clickable(role = Role.Button, onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Добавить",
                    tint = AppColors.Ink,
                    modifier = Modifier.size(24.dp)
                )
            }
            BottomBarItem(
                icon = Icons.Outlined.GridView,
                label = "Меню",
                selected = selected == RootDestination.MENU,
                onClick = onMenuClick
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(if (selected) AppColors.Ink else AppColors.White)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = if (selected) 14.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) AppColors.White else AppColors.Ink,
            modifier = Modifier.size(22.dp)
        )
        if (selected) {
            Spacer(Modifier.size(6.dp))
            Text(
                text = label,
                color = AppColors.White,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
            )
        }
    }
}
