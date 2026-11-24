package com.example.lifedashboard.feature.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.Capitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors
import java.util.UUID

@Composable
fun NoteDetailsScreen(
    note: DashboardNote?,
    onBack: () -> Unit,
    onSave: (DashboardNote) -> Unit,
    onDelete: (String) -> Unit,
    operationError: String? = null
) {
    var title by rememberSaveable(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by rememberSaveable(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    var category by rememberSaveable(note?.id) { mutableStateOf(note?.category ?: NoteCategory.APP) }
    var color by rememberSaveable(note?.id) { mutableStateOf(note?.color ?: NoteColor.YELLOW) }
    var isPinned by rememberSaveable(note?.id) { mutableStateOf(note?.isPinned ?: false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val canSave = title.isNotBlank() || body.isNotBlank()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ScreenTopBar(
                title = if (note == null) "Новая заметка" else "Заметка",
                actionIcon = Icons.Outlined.Check,
                actionDescription = "Сохранить заметку",
                onActionClick = {
                    if (canSave) {
                        onSave(
                            DashboardNote(
                                id = note?.id ?: UUID.randomUUID().toString(),
                                title = title,
                                body = body,
                                category = category,
                                color = color,
                                isPinned = isPinned,
                                updatedLabel = "Только что",
                                attachmentCount = note?.attachmentCount ?: 0
                            )
                        )
                    }
                },
                onBackClick = onBack
            )
        }

        if (operationError != null) {
            item {
                Text(
                    text = operationError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 1,
                maxLines = 3,
                label = { Text("Название") },
                placeholder = { Text("Например, идеи на неделю") },
                shape = RoundedCornerShape(22.dp),
                keyboardOptions = KeyboardOptions(capitalization = Capitalization.Sentences)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Категория",
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteCategory.values().forEach { item ->
                        FilterPill(
                            label = item.label,
                            selected = category == item,
                            onClick = { category = item }
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Цвет карточки",
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    NoteColor.values().forEach { item ->
                        ColorChoice(
                            color = noteColor(item),
                            selected = color == item,
                            onClick = { color = item }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Текст заметки") },
                placeholder = { Text("Запиши свои мысли...") },
                shape = RoundedCornerShape(24.dp),
                minLines = 8,
                maxLines = 18,
                keyboardOptions = KeyboardOptions(capitalization = Capitalization.Sentences)
            )
        }

        item {
            PastelCard(
                color = AppColors.White,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (isPinned) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = AppColors.Ink
                    )
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Закрепить заметку", color = AppColors.Ink, fontWeight = FontWeight.SemiBold)
                        Text("Она будет отображаться в начале списка", color = AppColors.Muted, fontSize = 12.sp)
                    }
                    Switch(checked = isPinned, onCheckedChange = { isPinned = it })
                }
            }
        }

        if (note != null) {
            item {
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = AppColors.Error
                    )
                    Text("Удалить заметку", color = AppColors.Error)
                }
            }
        }

        if (!canSave) {
            item {
                Text(
                    text = "Чтобы сохранить заметку, добавь название или текст.",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showDeleteDialog && note != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить заметку?") },
            text = { Text("Заметка будет удалена из списка.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(note.id)
                    }
                ) {
                    Text("Удалить", color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена", color = AppColors.Ink)
                }
            },
            containerColor = AppColors.White
        )
    }
}

@Composable
private fun ColorChoice(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(2.dp, AppColors.Ink, CircleShape)
                else Modifier.border(1.dp, AppColors.Outline.copy(alpha = 0.55f), CircleShape)
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AppColors.Ink)
            )
        }
    }
}

@Composable
private fun noteColor(color: NoteColor): Color = when (color) {
    NoteColor.YELLOW -> AppColors.Yellow
    NoteColor.PINK -> AppColors.Pink
    NoteColor.MINT -> AppColors.Mint
    NoteColor.LAVENDER -> AppColors.Lavender
    NoteColor.BLUE -> AppColors.Blue
}
