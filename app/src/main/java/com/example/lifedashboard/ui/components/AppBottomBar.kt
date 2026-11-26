package com.example.lifedashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import com.example.lifedashboard.ui.theme.AppColors

enum class RootDestination { HOME, MENU, SEARCH, SETTINGS }

@Composable
fun AppBottomBar(
    selected: RootDestination,
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            color = AppColors.White,
            shape = CircleShape,
            shadowElevation = 6.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                BottomBarItem(
                    icon = Icons.Outlined.Home,
                    label = stringResource(R.string.tab_home),
                    selected = selected == RootDestination.HOME,
                    onClick = onHomeClick,
                    modifier = Modifier.weight(1f)
                )
                BottomBarItem(
                    icon = Icons.Outlined.GridView,
                    label = stringResource(R.string.tab_sections),
                    selected = selected == RootDestination.MENU,
                    onClick = onMenuClick,
                    modifier = Modifier.weight(1f)
                )
                BottomBarItem(
                    icon = Icons.Outlined.Search,
                    label = stringResource(R.string.tab_search),
                    selected = selected == RootDestination.SEARCH,
                    onClick = onSearchClick,
                    modifier = Modifier.weight(1f)
                )
                BottomBarItem(
                    icon = Icons.Outlined.Tune,
                    label = stringResource(R.string.tab_settings),
                    selected = selected == RootDestination.SETTINGS,
                    onClick = onSettingsClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onAddClick),
            color = AppColors.Yellow,
            shape = CircleShape,
            shadowElevation = 6.dp,
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.action_add),
                    tint = AppColors.Ink,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(28.dp))
            .background(if (selected) AppColors.Lavender else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) AppColors.Ink else AppColors.Muted,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = if (selected) AppColors.Ink else AppColors.Muted,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}