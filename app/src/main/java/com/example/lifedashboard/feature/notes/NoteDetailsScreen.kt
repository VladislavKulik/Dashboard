package com.example.lifedashboard.feature.notes

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.local.NoteAttachmentStorage
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.NoteAttachment
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import java.util.UUID

@Composable
fun NoteDetailsScreen(
    note: DashboardNote?,
    onBack: () -> Unit,
    onSave: (DashboardNote) -> Unit,
    onDelete: (String) -> Unit,
    operationError: String? = null,
    projects: List<DashboardProject> = emptyList(),
    initialProjectId: String? = null,
    attachments: List<NoteAttachment> = emptyList(),
    onAddAttachment: (String, Uri) -> Unit = { _, _ -> },
    onDeleteAttachment: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val stableNoteId = rememberSaveable(note?.id) { note?.id ?: UUID.randomUUID().toString() }

    var title by rememberSaveable(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by rememberSaveable(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    var category by rememberSaveable(note?.id) { mutableStateOf(note?.category ?: NoteCategory.APP) }
    var color by rememberSaveable(note?.id) { mutableStateOf(note?.color ?: NoteColor.YELLOW) }
    var isPinned by rememberSaveable(note?.id) { mutableStateOf(note?.isPinned ?: false) }
    var projectId by rememberSaveable(note?.id, initialProjectId) { mutableStateOf(note?.projectId ?: initialProjectId) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val noteAttachments = remember(stableNoteId, attachments) {
        attachments.filter { it.noteId == stableNoteId }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onAddAttachment(stableNoteId, uri)
            }
        }
    )

    val canSave = title.isNotBlank() || body.isNotBlank() || noteAttachments.isNotEmpty()

    fun saveCurrent() {
        if (!canSave) return
        onSave(
            DashboardNote(
                id = stableNoteId,
                title = title,
                body = body,
                category = category,
                color = color,
                isPinned = isPinned,
                updatedLabel = "Только что",
                attachmentCount = noteAttachments.size.coerceAtLeast(note?.attachmentCount ?: 0),
                projectId = projectId
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "Закрыть",
                        onClick = onBack
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (note == null) "Новая заметка" else "Заметка",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    if (note != null) {
                        CircleIconButton(
                            icon = Icons.Outlined.DeleteOutline,
                            contentDescription = "Удалить",
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }

            if (operationError != null) {
                item { Text(operationError, color = AppColors.Error, fontSize = 12.sp) }
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
                    placeholder = { Text("Недельный обзор") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.White,
                        unfocusedContainerColor = AppColors.White,
                        focusedBorderColor = AppColors.Ink,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            item {
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Текст") },
                    placeholder = { Text("Запиши свои мысли...") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.White,
                        unfocusedContainerColor = AppColors.White,
                        focusedBorderColor = AppColors.Ink,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    minLines = 6,
                    maxLines = 16,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Вложения (${noteAttachments.size})", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(
                            text = "+ Прикрепить",
                            color = AppColors.Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { filePickerLauncher.launch(arrayOf("image/*", "*/*")) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (noteAttachments.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            noteAttachments.forEach { attachment ->
                                AttachmentPreviewItem(
                                    attachment = attachment,
                                    onOpen = { NoteAttachmentStorage.openAttachment(context, attachment) },
                                    onDelete = { onDeleteAttachment(attachment.id) }
                                )
                            }
                        }
                    }
                }
            }

            if (projects.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Проект", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterPill("Без проекта", projectId == null, { projectId = null })
                            projects.forEach { project ->
                                FilterPill(project.name, projectId == project.id, { projectId = project.id })
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Категория", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
                    Text("Цвет карточки", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }

        EditorBottomBar(
            confirmEnabled = canSave,
            onConfirm = ::saveCurrent,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IconButton(onClick = { filePickerLauncher.launch(arrayOf("image/*", "*/*")) }) {
                Icon(Icons.Outlined.Image, contentDescription = "Прикрепить фото или файл", tint = AppColors.Ink)
            }
            IconButton(
                onClick = {
                    val prefix = if (body.isBlank() || body.endsWith("\n")) "" else "\n"
                    body += "$prefix☐ "
                }
            ) {
                Icon(Icons.Outlined.Checklist, contentDescription = "Добавить пункт списка", tint = AppColors.Ink)
            }
            IconButton(
                onClick = {
                    val values = NoteCategory.values()
                    category = values[(category.ordinal + 1) % values.size]
                }
            ) {
                Icon(Icons.Outlined.LocalOffer, contentDescription = "Сменить категорию", tint = AppColors.Ink)
            }
            IconButton(onClick = { isPinned = !isPinned }) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Открепить" else "Закрепить",
                    tint = if (isPinned) AppColors.Purple else AppColors.Ink
                )
            }
        }
    }

    if (showDeleteDialog && note != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить заметку?", color = AppColors.Ink) },
            text = { Text("Заметка и прикреплённые к ней файлы будут удалены.", color = AppColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(note.id)
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
private fun AttachmentPreviewItem(
    attachment: NoteAttachment,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = attachment.localPath) {
        value = if (attachment.isImage) NoteAttachmentStorage.loadThumbnail(attachment.localPath, 320) else null
    }

    Box(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.White)
            .clickable(onClick = onOpen)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = attachment.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Blue.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (attachment.isImage) Icons.Outlined.Image else Icons.Outlined.InsertDriveFile,
                        contentDescription = null,
                        tint = AppColors.Ink
                    )
                }
            }
            Text(
                text = attachment.displayName,
                color = AppColors.Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = NoteAttachmentStorage.formatSize(attachment.sizeBytes),
                color = AppColors.Muted,
                fontSize = 10.sp
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(AppColors.Ink.copy(alpha = 0.78f))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Close, contentDescription = "Удалить вложение", tint = AppColors.White, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
private fun ColorChoice(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (selected) Modifier.border(2.dp, AppColors.Ink, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Outlined.Check, contentDescription = "Выбрано", tint = AppColors.Ink, modifier = Modifier.size(16.dp))
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