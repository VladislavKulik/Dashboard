package com.example.lifedashboard.feature.notes

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors
import com.example.lifedashboard.ui.theme.DashboardShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    notes: List<DashboardNote>,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateNote: () -> Unit,
    onTogglePinned: (String) -> Unit,
    operationError: String? = null,
    onDeleteNote: (String) -> Unit = {},
    attachments: List<NoteAttachment> = emptyList(),
    projects: List<DashboardProject> = emptyList(),
    onOpenSearch: () -> Unit = {}
) {
    var selectedCategory by rememberSaveable { mutableStateOf<NoteCategory?>(null) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

    val filteredNotes = notes.filter { note ->
        selectedCategory == null || note.category == selectedCategory
    }

    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.isPinned } }
    val otherNotes = remember(filteredNotes) { filteredNotes.filterNot { it.isPinned } }
    val attachmentsByNote = remember(attachments) { attachments.groupBy { it.noteId } }
    val projectNames = remember(projects) { projects.associate { it.id to it.name } }

    fun toggleSelection(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
        if (selectedIds.isEmpty()) selectionMode = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "Отменить выбор",
                        onClick = {
                            selectionMode = false
                            selectedIds = emptySet()
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Выбрано: ${selectedIds.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleIconButton(
                            icon = Icons.Outlined.PushPin,
                            contentDescription = "Закрепить или открепить",
                            onClick = {
                                selectedIds.forEach(onTogglePinned)
                                selectionMode = false
                                selectedIds = emptySet()
                            }
                        )
                        CircleIconButton(
                            icon = Icons.Outlined.DeleteOutline,
                            contentDescription = "Удалить выбранные",
                            onClick = { showBulkDeleteDialog = true }
                        )
                    }
                }
            } else {
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
                        text = "Заметки",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleIconButton(
                            icon = Icons.Outlined.GridView,
                            contentDescription = "Режим выбора",
                            onClick = {
                                if (notes.isNotEmpty()) selectionMode = true
                            }
                        )
                        CircleIconButton(
                            icon = Icons.Outlined.Search,
                            contentDescription = "Поиск заметок",
                            onClick = onOpenSearch
                        )
                    }
                }
            }

            operationError?.let { message ->
                Text(message, color = AppColors.Error, fontSize = 12.sp)
            }

            if (!selectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPill(
                        label = "Все",
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                    NoteCategory.values().forEach { category ->
                        if (notes.any { it.category == category }) {
                            FilterPill(
                                label = category.label,
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                NotesEmptyState(
                    hasFilter = selectedCategory != null,
                    onCreateNote = onCreateNote,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 92.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(Icons.Outlined.PushPin, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(14.dp))
                                Text("Закреплённые", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        items(pinnedNotes, key = { "pin-${it.id}" }) { note ->
                            NoteCard(
                                note = note,
                                noteAttachments = attachmentsByNote[note.id].orEmpty(),
                                projectName = projectNames[note.projectId],
                                selectionMode = selectionMode,
                                selected = note.id in selectedIds,
                                onClick = {
                                    if (selectionMode) toggleSelection(note.id) else onOpenNote(note.id)
                                },
                                onLongClick = {
                                    selectionMode = true
                                    selectedIds = selectedIds + note.id
                                }
                            )
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    text = "Другие",
                                    color = AppColors.Muted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        items(otherNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                noteAttachments = attachmentsByNote[note.id].orEmpty(),
                                projectName = projectNames[note.projectId],
                                selectionMode = selectionMode,
                                selected = note.id in selectedIds,
                                onClick = {
                                    if (selectionMode) toggleSelection(note.id) else onOpenNote(note.id)
                                },
                                onLongClick = {
                                    selectionMode = true
                                    selectedIds = selectedIds + note.id
                                }
                            )
                        }
                    }
                }
            }
        }

        if (!selectionMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onCreateNote),
                color = AppColors.Yellow,
                shape = CircleShape,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Создать заметку",
                        tint = AppColors.Ink,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }

    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text("Удалить выбранные заметки?", color = AppColors.Ink) },
            text = { Text("Будет удалено заметок: ${selectedIds.size}.", color = AppColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedIds.forEach(onDeleteNote)
                        selectedIds = emptySet()
                        selectionMode = false
                        showBulkDeleteDialog = false
                    }
                ) { Text("Удалить", color = AppColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) { Text("Отмена", color = AppColors.Muted) }
            },
            containerColor = AppColors.White
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: DashboardNote,
    noteAttachments: List<NoteAttachment>,
    projectName: String?,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val firstImage = remember(noteAttachments) { noteAttachments.firstOrNull { it.isImage } }
    val previewBitmap by produceState<Bitmap?>(initialValue = null, key1 = firstImage?.localPath) {
        value = firstImage?.let { NoteAttachmentStorage.loadThumbnail(it.localPath, maxEdgePx = 380) }
    }
    val realCount = maxOf(note.attachmentCount, noteAttachments.size)
    val cardBg = if (previewBitmap != null || (note.category == NoteCategory.BLOG && realCount > 0)) {
        AppColors.White
    } else {
        noteColor(note.color)
    }

    Box {
        PastelCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 136.dp)
                .then(
                    if (selected) Modifier.border(2.dp, AppColors.Ink, DashboardShapes.large)
                    else Modifier
                )
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            color = cardBg,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (previewBitmap != null) {
                Image(
                    bitmap = previewBitmap!!.asImageBitmap(),
                    contentDescription = firstImage?.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else if (note.category == NoteCategory.BLOG && realCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.Blue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Вложение",
                        tint = AppColors.Ink,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = note.title,
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (note.body.isNotBlank() && previewBitmap == null && !(note.category == NoteCategory.BLOG && realCount > 0)) {
                Text(
                    text = note.body,
                    color = AppColors.Muted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (realCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(14.dp))
                    Text("$realCount влож.", color = AppColors.Muted, fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, AppColors.Ink.copy(alpha = 0.14f), CircleShape)
                        .background(AppColors.White.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = projectName ?: note.category.label,
                        color = AppColors.Ink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        if (selectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.background(AppColors.Ink)
                        else Modifier.background(AppColors.White.copy(alpha = 0.9f))
                            .border(1.5.dp, AppColors.Ink, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Outlined.Check, contentDescription = "Выбрано", tint = AppColors.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun NotesEmptyState(
    hasFilter: Boolean,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    PastelCard(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.White,
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            tint = AppColors.Muted,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = if (hasFilter) "В этой категории пусто" else "Заметок пока нет",
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Ink
        )
        Text(
            text = if (hasFilter) "Выбери другую категорию или создай заметку." else "Создай первую заметку — она появится здесь.",
            color = AppColors.Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Создать заметку",
            color = AppColors.Ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Yellow)
                .clickable(onClick = onCreateNote)
                .padding(horizontal = 16.dp, vertical = 11.dp)
        )
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