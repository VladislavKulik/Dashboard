package com.example.lifedashboard.feature.notes

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.res.stringResource
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
import com.example.lifedashboard.ui.components.ColorPaletteSheet
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.EditorPropertyChip
import com.example.lifedashboard.ui.components.EditorToolButton
import com.example.lifedashboard.ui.components.PaletteColorOption
import com.example.lifedashboard.ui.components.ProjectPickerSheet
import com.example.lifedashboard.ui.theme.AppColors
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
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
    onDeleteAttachment: (String) -> Unit = {},
    allAvailableLabels: List<String> = emptyList()
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val stableNoteId = rememberSaveable(note?.id) { note?.id ?: UUID.randomUUID().toString() }

    var title by rememberSaveable(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by rememberSaveable(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    var category by rememberSaveable(note?.id) { mutableStateOf(note?.category ?: NoteCategory.APP) }
    var color by rememberSaveable(note?.id) { mutableStateOf(note?.color ?: NoteColor.YELLOW) }
    var isPinned by rememberSaveable(note?.id) { mutableStateOf(note?.isPinned ?: false) }
    var projectId by rememberSaveable(note?.id, initialProjectId) { mutableStateOf(note?.projectId ?: initialProjectId) }
    var selectedLabels by remember(note?.id) {
        mutableStateOf(note?.labels.orEmpty())
    }

    val historyStack = remember(note?.id) { mutableStateListOf(note?.body.orEmpty()) }
    var historyIndex by remember(note?.id) { mutableIntStateOf(0) }

    fun updateBodyWithHistory(newBody: String) {
        if (newBody == body) return
        body = newBody
        while (historyStack.lastIndex > historyIndex) {
            historyStack.removeAt(historyStack.lastIndex)
        }
        historyStack.add(newBody)
        if (historyStack.size > 40) {
            historyStack.removeAt(0)
        } else {
            historyIndex = historyStack.lastIndex
        }
    }

    val canUndo = historyIndex > 0
    val canRedo = historyIndex < historyStack.lastIndex

    var showInsertSheet by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showLabelsSheet by remember { mutableStateOf(false) }
    var showProjectSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val noteAttachments = remember(stableNoteId, attachments) {
        attachments.filter { it.noteId == stableNoteId }
    }
    val linkedProject = remember(projectId, projects) {
        projects.firstOrNull { it.id == projectId }
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

    fun handleCancel() {
        if (note == null && noteAttachments.isNotEmpty()) {
            noteAttachments.forEach { onDeleteAttachment(it.id) }
        }
        onBack()
    }

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
                updatedLabel = note?.updatedLabel ?: "Сегодня",
                attachmentCount = noteAttachments.size.coerceAtLeast(note?.attachmentCount ?: 0),
                projectId = projectId,
                labels = selectedLabels
            )
        )
    }

    val colorOptions = listOf(
        PaletteColorOption(NoteColor.YELLOW, "Жёлтый", AppColors.Yellow),
        PaletteColorOption(NoteColor.PINK, "Розовый", AppColors.Pink),
        PaletteColorOption(NoteColor.MINT, "Мятный", AppColors.Mint),
        PaletteColorOption(NoteColor.LAVENDER, "Лавандовый", AppColors.Lavender),
        PaletteColorOption(NoteColor.BLUE, "Голубой", AppColors.Blue)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "Закрыть",
                        onClick = ::handleCancel
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (note == null) "Новая заметка" else "Заметка",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    CircleIconButton(
                        icon = Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) "Открепить" else "Закрепить",
                        background = if (isPinned) AppColors.Lavender else AppColors.White,
                        tint = AppColors.Ink,
                        onClick = { isPinned = !isPinned }
                    )
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
                    placeholder = { Text("Заголовок заметки") },
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
                    onValueChange = ::updateBodyWithHistory,
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
                    minLines = 8,
                    maxLines = 22,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            // Custom labels & project chips right below text (Google Keep style)
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedLabels.forEach { label ->
                        EditorPropertyChip(
                            icon = Icons.Outlined.LocalOffer,
                            label = label,
                            backgroundColor = noteColor(color).copy(alpha = 0.75f),
                            onClick = { showLabelsSheet = true },
                            onClear = { selectedLabels = selectedLabels - label }
                        )
                    }
                    if (linkedProject != null) {
                        EditorPropertyChip(
                            icon = Icons.Outlined.FolderOpen,
                            label = linkedProject.name,
                            backgroundColor = Color(linkedProject.colorArgb).copy(alpha = 0.65f),
                            onClick = { showProjectSheet = true },
                            onClear = { projectId = null }
                        )
                    }
                }
            }

            if (noteAttachments.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Вложения (${noteAttachments.size})",
                            color = AppColors.Muted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
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
        }

        EditorBottomBar(
            confirmEnabled = canSave,
            onConfirm = ::saveCurrent,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            EditorToolButton(
                icon = Icons.Outlined.AddBox,
                contentDescription = "Добавить вложение или список",
                isActive = noteAttachments.isNotEmpty(),
                onClick = { showInsertSheet = true }
            )
            EditorToolButton(
                icon = Icons.Outlined.Palette,
                contentDescription = "Цвет карточки",
                onClick = { showPaletteSheet = true }
            )
            EditorToolButton(
                icon = Icons.Outlined.Undo,
                contentDescription = "Отменить ввод",
                enabled = canUndo,
                onClick = {
                    if (canUndo) {
                        historyIndex -= 1
                        body = historyStack[historyIndex]
                    }
                }
            )
            EditorToolButton(
                icon = Icons.Outlined.Redo,
                contentDescription = "Повторить ввод",
                enabled = canRedo,
                onClick = {
                    if (canRedo) {
                        historyIndex += 1
                        body = historyStack[historyIndex]
                    }
                }
            )
            EditorToolButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "Ещё (ярлыки, проект, копировать, удалить)",
                isActive = selectedLabels.isNotEmpty() || projectId != null,
                onClick = { showMoreSheet = true }
            )
        }
    }

    if (showInsertSheet) {
        NoteInsertBottomSheet(
            onDismiss = { showInsertSheet = false },
            onPickAttachment = {
                showInsertSheet = false
                filePickerLauncher.launch(arrayOf("image/*", "*/*"))
            },
            onInsertChecklistItem = {
                showInsertSheet = false
                val prefix = if (body.isBlank() || body.endsWith("\n")) "" else "\n"
                updateBodyWithHistory("$body$prefix☐ ")
            }
        )
    }

    if (showPaletteSheet) {
        ColorPaletteSheet(
            options = colorOptions,
            selected = color,
            onSelect = { color = it },
            onDismiss = { showPaletteSheet = false }
        )
    }

    if (showMoreSheet) {
        NoteMoreActionsSheet(
            updatedLabel = note?.updatedLabel?.takeUnless { it.equals("Только что", ignoreCase = true) } ?: "Новая заметка",
            labelsSummary = selectedLabels.takeIf { it.isNotEmpty() }?.joinToString(", "),
            projectName = linkedProject?.name,
            canDelete = note != null,
            onDismiss = { showMoreSheet = false },
            onOpenLabels = {
                showMoreSheet = false
                showLabelsSheet = true
            },
            onOpenProject = {
                showMoreSheet = false
                showProjectSheet = true
            },
            onCopyText = {
                showMoreSheet = false
                val textToCopy = listOf(title.trim(), body.trim()).filter { it.isNotBlank() }.joinToString("\n\n")
                if (textToCopy.isNotBlank()) {
                    clipboard.setText(AnnotatedString(textToCopy))
                }
            },
            onShare = {
                showMoreSheet = false
                val shareText = listOf(title.trim(), body.trim()).filter { it.isNotBlank() }.joinToString("\n\n")
                if (shareText.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, title.trim())
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    runCatching { context.startActivity(Intent.createChooser(intent, "Отправить заметку")) }
                }
            },
            onDelete = {
                showMoreSheet = false
                showDeleteDialog = true
            }
        )
    }

    if (showLabelsSheet) {
        NoteCustomLabelsSheet(
            allAvailableLabels = (allAvailableLabels + selectedLabels + NoteCategory.values().map { stringResource(id = it.labelResId) })
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct(),
            selectedLabels = selectedLabels,
            onUpdateLabels = { selectedLabels = it },
            onDismiss = { showLabelsSheet = false }
        )
    }

    if (showProjectSheet) {
        ProjectPickerSheet(
            projects = projects,
            selectedProjectId = projectId,
            onSelectProject = { projectId = it },
            onDismiss = { showProjectSheet = false }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteInsertBottomSheet(
    onDismiss: () -> Unit,
    onPickAttachment: () -> Unit,
    onInsertChecklistItem: () -> Unit
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
            Text("Добавить", style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)
            NoteSheetRow(
                icon = Icons.Outlined.AttachFile,
                title = "Фото или файл",
                subtitle = "До 25 МБ из памяти устройства",
                onClick = onPickAttachment
            )
            NoteSheetRow(
                icon = Icons.Outlined.Checklist,
                title = "Чек-лист",
                subtitle = "Добавить интерактивный пункт «☐»",
                onClick = onInsertChecklistItem
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteMoreActionsSheet(
    updatedLabel: String,
    labelsSummary: String?,
    projectName: String?,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onOpenLabels: () -> Unit,
    onOpenProject: () -> Unit,
    onCopyText: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Изменено: $updatedLabel",
                color = AppColors.Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))

            NoteMenuLine(
                icon = Icons.Outlined.LocalOffer,
                label = "Ярлыки",
                trailing = labelsSummary,
                onClick = onOpenLabels
            )
            NoteMenuLine(
                icon = Icons.Outlined.FolderOpen,
                label = "Проект",
                trailing = projectName,
                onClick = onOpenProject
            )
            NoteMenuLine(
                icon = Icons.Outlined.ContentCopy,
                label = "Скопировать",
                onClick = onCopyText
            )
            NoteMenuLine(
                icon = Icons.Outlined.Share,
                label = "Отправить",
                onClick = onShare
            )
            if (canDelete) {
                NoteMenuLine(
                    icon = Icons.Outlined.DeleteOutline,
                    label = "Удалить",
                    tint = AppColors.Error,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun NoteMenuLine(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    tint: Color = AppColors.Ink,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (!trailing.isNullOrBlank()) {
            Text(
                text = trailing,
                color = AppColors.Muted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun NoteSheetRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = AppColors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = AppColors.Muted, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCustomLabelsSheet(
    allAvailableLabels: List<String>,
    selectedLabels: List<String>,
    onUpdateLabels: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchOrNew by remember { mutableStateOf("") }
    val trimmed = searchOrNew.trim()

    val matchingLabels = remember(allAvailableLabels, trimmed) {
        if (trimmed.isBlank()) allAvailableLabels
        else allAvailableLabels.filter { it.contains(trimmed, ignoreCase = true) }
    }
    val exactExists = remember(allAvailableLabels, trimmed) {
        trimmed.isNotBlank() && allAvailableLabels.any { it.equals(trimmed, ignoreCase = true) }
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ярлыки", style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)

            OutlinedTextField(
                value = searchOrNew,
                onValueChange = { searchOrNew = it.take(32) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Введите название нового ярлыка...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.White,
                    unfocusedContainerColor = AppColors.White,
                    focusedBorderColor = AppColors.Ink,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            if (trimmed.isNotBlank() && !exactExists) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.Yellow)
                        .clickable {
                            onUpdateLabels((selectedLabels + trimmed).distinct())
                            searchOrNew = ""
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Создать ярлык «$trimmed»",
                        color = AppColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(matchingLabels, key = { it }) { label ->
                    val checked = selectedLabels.any { it.equals(label, ignoreCase = true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.White)
                            .clickable {
                                val next = if (checked) {
                                    selectedLabels.filterNot { it.equals(label, ignoreCase = true) }
                                } else {
                                    (selectedLabels + label).distinct()
                                }
                                onUpdateLabels(next)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.LocalOffer, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = AppColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                val next = if (!isChecked) {
                                    selectedLabels.filterNot { it.equals(label, ignoreCase = true) }
                                } else {
                                    (selectedLabels + label).distinct()
                                }
                                onUpdateLabels(next)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentPreviewItem(
    attachment: NoteAttachment,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val initialCached = remember(attachment.localPath) {
        if (attachment.isImage) NoteAttachmentStorage.getCachedThumbnail(attachment.localPath, 320) else null
    }
    val bitmap by produceState<Bitmap?>(initialValue = initialCached, key1 = attachment.localPath) {
        if (value == null && attachment.isImage) {
            value = NoteAttachmentStorage.loadThumbnail(attachment.localPath, 320)
        }
    }

    Box(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.White)
            .clickable(onClick = onOpen)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Blue.copy(alpha = 0.45f)),
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
                text = NoteAttachmentStorage.formatSize(LocalContext.current, attachment.sizeBytes),
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
private fun noteColor(color: NoteColor): Color = when (color) {
    NoteColor.YELLOW -> AppColors.Yellow
    NoteColor.PINK -> AppColors.Pink
    NoteColor.MINT -> AppColors.Mint
    NoteColor.LAVENDER -> AppColors.Lavender
    NoteColor.BLUE -> AppColors.Blue
}