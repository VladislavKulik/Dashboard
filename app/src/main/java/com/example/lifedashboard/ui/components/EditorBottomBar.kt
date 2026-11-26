package com.example.lifedashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun EditorBottomBar(
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    tools: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            color = AppColors.White,
            shape = CircleShape,
            shadowElevation = 6.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                content = tools
            )
        }

        Spacer(Modifier.width(12.dp))

        Surface(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable(enabled = confirmEnabled, role = Role.Button, onClick = onConfirm),
            color = if (confirmEnabled) AppColors.Yellow else Color(0xFFDAD9E0),
            shape = CircleShape,
            shadowElevation = 6.dp,
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.action_save),
                    tint = if (confirmEnabled) AppColors.Ink else AppColors.Muted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/** Bottom bar tool icon with active-state highlight pill (Google Keep style). */
@Composable
fun EditorToolButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isActive) AppColors.Lavender else Color.Transparent)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = when {
                !enabled -> AppColors.Muted.copy(alpha = 0.38f)
                isActive -> AppColors.Ink
                else -> AppColors.Ink
            },
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Compact interactive chip displayed on the editor canvas for active properties. */
@Composable
fun EditorPropertyChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
    backgroundColor: Color = AppColors.White
) {
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = CircleShape,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                end = if (onClear != null) 8.dp else 14.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Ink,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = AppColors.Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (onClear != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AppColors.Background)
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.action_reset),
                        tint = AppColors.Muted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

data class PaletteColorOption<T>(
    val value: T,
    val label: String,
    val color: Color
)

/** Reusable Google Keep-style color picker bottom sheet for Notes, Projects, Goals, and Habits. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T> ColorPaletteSheet(
    title: String = stringResource(R.string.card_color),
    options: List<PaletteColorOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.Ink
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option.value == selected
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(option.color)
                            .then(
                                if (isSelected) Modifier.border(2.dp, AppColors.Ink, CircleShape)
                                else Modifier
                            )
                            .clickable {
                                onSelect(option.value)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.state_selected),
                                tint = AppColors.Ink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = option.label,
                            color = AppColors.Ink,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/** Reusable bottom sheet for selecting a linked Project in Task, Note, or Goal editors. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectPickerSheet(
    projects: List<DashboardProject>,
    selectedProjectId: String?,
    onSelectProject: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.link_to_project),
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.Ink
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ProjectChoiceRow(
                        name = stringResource(R.string.no_project),
                        badgeColor = AppColors.Background,
                        selected = selectedProjectId == null,
                        onClick = {
                            onSelectProject(null)
                            onDismiss()
                        }
                    )
                }
                items(projects.filterNot { it.isArchived }, key = { it.id }) { project ->
                    ProjectChoiceRow(
                        name = project.name,
                        badgeColor = Color(project.colorArgb),
                        selected = selectedProjectId == project.id,
                        onClick = {
                            onSelectProject(project.id)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectChoiceRow(
    name: String,
    badgeColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.White)
            .then(
                if (selected) Modifier.border(2.dp, AppColors.Ink, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = AppColors.Ink,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            color = AppColors.Ink,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = stringResource(R.string.state_selected),
                tint = AppColors.Ink,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}