package com.example.lifedashboard.feature.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors

private data class MenuItemUi(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun MenuScreen(
    onNavigate: (String) -> Unit,
    onSettingsClick: () -> Unit,
    noteCount: Int = 24,
    goalCount: Int = 0,
    completedGoalCount: Int = 0
) {
    val items = listOf(
        MenuItemUi("Проекты", "3 активных", Icons.Outlined.FolderOpen, AppColors.Lavender, "projects"),
        MenuItemUi("Заметки", noteCountLabel(noteCount), Icons.Outlined.Description, AppColors.Yellow, "notes"),
        MenuItemUi("Аналитика", "+12% за неделю", Icons.Outlined.BarChart, AppColors.Mint, "analytics"),
        MenuItemUi("Привычки", "Серия 14 дней", Icons.Outlined.Check, AppColors.Pink, "habits"),
        MenuItemUi("Календарь", "Сроки и активность", Icons.Outlined.CalendarMonth, AppColors.Yellow, "calendar")
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 146.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Разделы",
                    modifier = Modifier.weight(1f),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    color = AppColors.Ink
                )
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppColors.White),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.IconButton(onClick = onSettingsClick, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Настройки", tint = AppColors.Ink)
                    }
                }
            }
        }

        items(items) { item ->
            MenuTile(
                item = item,
                modifier = Modifier.fillMaxWidth().height(176.dp),
                onClick = { onNavigate(item.route) }
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            MenuTile(
                item = MenuItemUi("Цели", "$completedGoalCount из $goalCount достигнуто", Icons.Outlined.Flag, AppColors.Blue, "goals"),
                modifier = Modifier.fillMaxWidth().height(120.dp),
                onClick = { onNavigate("goals") }
            )
        }
    }
}

@Composable
private fun MenuTile(item: MenuItemUi, modifier: Modifier = Modifier, onClick: () -> Unit) {
    PastelCard(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        color = item.color,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.White.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(22.dp))
            }
            Icon(Icons.Outlined.OpenInNew, contentDescription = "Открыть раздел", tint = AppColors.Ink, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, color = AppColors.Ink)
            Text(item.subtitle, fontSize = 13.sp, color = AppColors.Muted)
        }
    }
}



private fun noteCountLabel(count: Int): String {
    val lastTwo = count % 100
    val last = count % 10
    val word = when {
        lastTwo in 11..14 -> "записей"
        last == 1 -> "запись"
        last in 2..4 -> "записи"
        else -> "записей"
    }
    return "$count $word"
}
