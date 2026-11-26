package com.example.lifedashboard.feature.notes

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.local.NoteAttachmentStorage
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.NoteAttachment
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.ui.components.AppFab
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun NoteViewScreen(
    note: DashboardNote,
    projects: List<DashboardProject>,
    attachments: List<NoteAttachment>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onTogglePinned: () -> Unit,
    onDelete: () -> Unit,
    onOpenProject: (String) -> Unit,
    onUpdateNoteBody: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val linkedProject = remember(note.projectId, projects) {
        projects.firstOrNull { it.id == note.projectId }
    }
    val noteAttachments = remember(note.id, attachments) {
        attachments.filter { it.noteId == note.id }
    }
    val bodyLines = remember(note.body) { note.body.lines() }
    val displayUpdatedLabel = remember(note.updatedLabel) {
        if (note.updatedLabel.equals("Только что", ignoreCase = true) || note.updatedLabel.isBlank()) {
            "Сегодня"
        } else {
            note.updatedLabel
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Назад",
                        onClick = onBack
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Заметка",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    CircleIconButton(
                        icon = Icons.Outlined.PushPin,
                        contentDescription = if (note.isPinned) "Открепить" else "Закрепить",
                        background = if (note.isPinned) AppColors.Lavender else AppColors.White,
                        tint = AppColors.Ink,
                        onClick = onTogglePinned
                    )
                    Spacer(Modifier.width(8.dp))
                    Box {
                        CircleIconButton(
                            icon = Icons.Outlined.MoreHoriz,
                            contentDescription = "Действия",
                            onClick = { showOverflowMenu = true }
                        )
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                onClick = {
                                    showOverflowMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (note.isPinned) "Открепить" else "Закрепить") },
                                onClick = {
                                    showOverflowMenu = false
                                    onTogglePinned()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить заметку", color = AppColors.Error) },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            item {
                PastelCard(
                    color = noteViewColor(note.color),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.labels.isNotEmpty()) {
                            note.labels.forEach { customLabel ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(AppColors.White.copy(alpha = 0.85f))
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = customLabel,
                                        color = AppColors.Ink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AppColors.White.copy(alpha = 0.85f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = stringResource(id = note.category.labelResId),
                                    color = AppColors.Ink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (linkedProject != null) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AppColors.Ink)
                                    .clickable { onOpenProject(linkedProject.id) }
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = AppColors.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = linkedProject.name,
                                    color = AppColors.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppColors.Ink
                    )

                    Text(
                        text = "Изменено: $displayUpdatedLabel",
                        color = AppColors.Muted,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                PastelCard(
                    color = AppColors.White,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (note.body.isBlank()) {
                        Text(
                            text = "Текст заметки пуст.",
                            color = AppColors.Muted,
                            fontSize = 14.sp
                        )
                    } else {
                        bodyLines.forEachIndexed { index, line ->
                            val trimmed = line.trimStart()
                            when {
                                trimmed.startsWith("☐ ") || trimmed.startsWith("☑ ") -> {
                                    val checked = trimmed.startsWith("☑ ")
                                    val itemText = trimmed.drop(2)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(enabled = onUpdateNoteBody != null) {
                                                val updatedLines = bodyLines.toMutableList()
                                                updatedLines[index] = if (checked) {
                                                    line.replaceFirst("☑ ", "☐ ")
                                                } else {
                                                    line.replaceFirst("☐ ", "☑ ")
                                                }
                                                onUpdateNoteBody?.invoke(updatedLines.joinToString("\n"))
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    if (checked) Modifier.background(AppColors.Ink)
                                                    else Modifier.border(1.8.dp, AppColors.Ink, CircleShape)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (checked) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = null,
                                                    tint = AppColors.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = itemText,
                                            color = if (checked) AppColors.Muted else AppColors.Ink,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }
                                }
                                line.isBlank() -> Spacer(Modifier.height(4.dp))
                                else -> {
                                    Text(
                                        text = line,
                                        color = AppColors.Ink,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (noteAttachments.isNotEmpty()) {
                item {
                    Text(
                        text = "Вложения (${noteAttachments.size})",
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                items(noteAttachments, key = { it.id }) { attachment ->
                    NoteViewAttachmentCard(
                        attachment = attachment,
                        onOpen = { NoteAttachmentStorage.openAttachment(context, attachment) }
                    )
                }
            }
        }

        AppFab(
            icon = Icons.Outlined.Edit,
            contentDescription = "Редактировать заметку",
            onClick = onEdit,
            iconSize = 24.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить заметку?", color = AppColors.Ink) },
            text = { Text("Заметка и прикреплённые к ней файлы будут удалены.", color = AppColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) { Text("Удалить", color = AppColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена", color = AppColors.Muted) }
            },
            containerColor = AppColors.White
        )
    }
}

@Composable
private fun NoteViewAttachmentCard(
    attachment: NoteAttachment,
    onOpen: () -> Unit
) {
    val initialCached = remember(attachment.localPath) {
        if (attachment.isImage) NoteAttachmentStorage.getCachedThumbnail(attachment.localPath, 720) else null
    }
    val bitmap by produceState<Bitmap?>(initialValue = initialCached, key1 = attachment.localPath) {
        if (value == null && attachment.isImage) {
            value = NoteAttachmentStorage.loadThumbnail(attachment.localPath, 720)
        }
    }

    PastelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        color = AppColors.White,
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Fixed 180.dp container reserved from Frame 0 when attachment is an image
        if (attachment.isImage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = attachment.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = AppColors.Muted.copy(alpha = 0.55f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.Blue.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (attachment.isImage) Icons.Outlined.Image else Icons.Outlined.InsertDriveFile,
                    contentDescription = null,
                    tint = AppColors.Ink,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = attachment.displayName,
                    color = AppColors.Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = NoteAttachmentStorage.formatSize(LocalContext.current, attachment.sizeBytes),
                    color = AppColors.Muted,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = "Открыть файл",
                tint = AppColors.Muted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun noteViewColor(color: NoteColor): Color = when (color) {
    NoteColor.YELLOW -> AppColors.Yellow
    NoteColor.PINK -> AppColors.Pink
    NoteColor.MINT -> AppColors.Mint
    NoteColor.LAVENDER -> AppColors.Lavender
    NoteColor.BLUE -> AppColors.Blue
}