package com.example.lifedashboard.feature.notes

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import com.example.lifedashboard.data.local.NoteAttachmentStorage
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.NoteAttachment
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.ui.components.AppFab
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.EmptySectionState
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
    var selectedCustomLabel by rememberSaveable { mutableStateOf<String?>(null) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

    val customLabels = remember(notes) {
        notes.flatMap { it.labels }.distinct().sorted()
    }

    val filteredNotes = notes.filter { note ->
        val matchesCategory = selectedCategory == null || note.category == selectedCategory
        val matchesLabel = selectedCustomLabel == null || note.labels.any { it.equals(selectedCustomLabel, ignoreCase = true) }
        matchesCategory && matchesLabel
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
                        contentDescription = stringResource(R.string.action_deselect),
                        onClick = {
                            selectionMode = false
                            selectedIds = emptySet()
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.selected_count, selectedIds.size),
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleIconButton(
                            icon = Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.pin_unpin),
                            onClick = {
                                selectedIds.forEach(onTogglePinned)
                                selectionMode = false
                                selectedIds = emptySet()
                            }
                        )
                        CircleIconButton(
                            icon = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_selected),
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
                        contentDescription = stringResource(R.string.action_back),
                        onClick = onBack
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.notes_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleIconButton(
                            icon = Icons.Outlined.GridView,
                            contentDescription = stringResource(R.string.selection_mode),
                            onClick = {
                                if (notes.isNotEmpty()) selectionMode = true
                            }
                        )
                        CircleIconButton(
                            icon = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.search_notes),
                            onClick = onOpenSearch
                        )
                    }
                }
            }

            operationError?.let { message ->
                Text(message, color = AppColors.Error, fontSize = 12.sp)
            }

            if (!selectionMode && notes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPill(
                        label = stringResource(R.string.filter_all),
                        selected = selectedCategory == null && selectedCustomLabel == null,
                        onClick = {
                            selectedCategory = null
                            selectedCustomLabel = null
                        }
                    )
                    customLabels.forEach { label ->
                        FilterPill(
                            label = label,
                            selected = selectedCustomLabel == label,
                            onClick = {
                                selectedCategory = null
                                selectedCustomLabel = if (selectedCustomLabel == label) null else label
                            }
                        )
                    }
                    NoteCategory.values().forEach { category ->
                        if (notes.any { it.category == category && it.labels.isEmpty() }) {
                            FilterPill(
                                label = stringResource(id = category.labelResId),
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCustomLabel = null
                                    selectedCategory = if (selectedCategory == category) null else category
                                }
                            )
                        }
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                val hasFilter = selectedCategory != null || selectedCustomLabel != null
                EmptySectionState(
                    icon = Icons.Outlined.Description,
                    iconBackground = AppColors.Yellow,
                    title = if (hasFilter) stringResource(R.string.notes_empty_filtered) else stringResource(R.string.goals_empty_title),
                    subtitle = if (hasFilter) {
                        stringResource(R.string.notes_empty_filtered_sub)
                    } else {
                        stringResource(R.string.notes_empty_sub)
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 96.dp),
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
                                Text(stringResource(R.string.pinned_section), color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                    text = stringResource(R.string.other_section),
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
            AppFab(
                icon = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.create_note),
                onClick = onCreateNote,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }

    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text(stringResource(R.string.bulk_delete_title), color = AppColors.Ink) },
            text = { Text(stringResource(R.string.bulk_delete_confirm, selectedIds.size), color = AppColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedIds.forEach(onDeleteNote)
                        selectedIds = emptySet()
                        selectionMode = false
                        showBulkDeleteDialog = false
                    }
                ) { Text(stringResource(R.string.action_delete), color = AppColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) { Text(stringResource(R.string.action_cancel), color = AppColors.Muted) }
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
    val hasImageSlot = firstImage != null
    val initialCachedBitmap = remember(firstImage?.localPath) {
        firstImage?.let { NoteAttachmentStorage.getCachedThumbnail(it.localPath, maxEdgePx = 380) }
    }
    val previewBitmap by produceState<Bitmap?>(initialValue = initialCachedBitmap, key1 = firstImage?.localPath) {
        if (value == null && firstImage != null) {
            value = NoteAttachmentStorage.loadThumbnail(firstImage.localPath, maxEdgePx = 380)
        }
    }

    val realCount = maxOf(note.attachmentCount, noteAttachments.size)
    val cardBg = if (hasImageSlot) AppColors.White else noteColor(note.color)

    val categoryLabel = stringResource(id = note.category.labelResId)
    val displayBadges = remember(note.labels, projectName, categoryLabel) {
        buildList {
            if (!projectName.isNullOrBlank()) add(projectName)
            if (note.labels.isNotEmpty()) {
                addAll(note.labels.take(2))
            } else if (projectName.isNullOrBlank()) {
                add(categoryLabel)
            }
        }
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
            // Fixed-height 92.dp image container reserved on Frame 0 so staggered grid NEVER jumps
            if (hasImageSlot) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.Background),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = firstImage?.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = AppColors.Muted.copy(alpha = 0.55f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Text(
                text = note.title,
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (note.body.isNotBlank() && !hasImageSlot) {
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
                    Text(stringResource(R.string.attachments_count_short, realCount), color = AppColors.Muted, fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                displayBadges.forEach { badge ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, AppColors.Ink.copy(alpha = 0.14f), CircleShape)
                            .background(AppColors.White.copy(alpha = 0.55f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badge,
                            color = AppColors.Ink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
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
                        else Modifier
                            .background(AppColors.White.copy(alpha = 0.9f))
                            .border(1.5.dp, AppColors.Ink, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Outlined.Check, contentDescription = stringResource(R.string.state_selected), tint = AppColors.White, modifier = Modifier.size(14.dp))
                }
            }
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