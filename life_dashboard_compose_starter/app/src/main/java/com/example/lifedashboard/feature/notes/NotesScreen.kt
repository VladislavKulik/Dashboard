package com.example.lifedashboard.feature.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    notes: List<DashboardNote>,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateNote: () -> Unit,
    onTogglePinned: (String) -> Unit,
    operationError: String? = null
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<NoteCategory?>(null) }
    val searchFocusRequester = remember { FocusRequester() }

    val filteredNotes = notes
        .filter { note ->
            val matchesCategory = selectedCategory == null || note.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                note.title.contains(searchQuery.trim(), ignoreCase = true) ||
                note.body.contains(searchQuery.trim(), ignoreCase = true)
            matchesCategory && matchesSearch
        }
        .sortedWith(compareByDescending<DashboardNote> { it.isPinned })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTopBar(
            title = "Заметки",
            actionIcon = Icons.Outlined.Search,
            actionDescription = "Перейти к поиску",
            onActionClick = { searchFocusRequester.requestFocus() },
            onBackClick = onBack
        )

        operationError?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .focusRequester(searchFocusRequester),
            singleLine = true,
            placeholder = {
                Text("Поиск по заметкам", color = AppColors.Muted, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = AppColors.Ink)
            },
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.White,
                unfocusedContainerColor = AppColors.White,
                disabledContainerColor = AppColors.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = AppColors.Ink
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill(
                label = "Все",
                selected = selectedCategory == null,
                count = notes.size.toString(),
                onClick = { selectedCategory = null }
            )
            NoteCategory.values().forEach { category ->
                val count = notes.count { it.category == category }
                if (count > 0) {
                    FilterPill(
                        label = category.label,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        count = count.toString()
                    )
                }
            }
        }

        if (filteredNotes.isEmpty()) {
            NotesEmptyState(
                hasSearch = searchQuery.isNotBlank(),
                onCreateNote = onCreateNote,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onOpenNote(note.id) },
                        onTogglePinned = { onTogglePinned(note.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: DashboardNote,
    onClick: () -> Unit,
    onTogglePinned: () -> Unit
) {
    PastelCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 164.dp)
            .clickable(onClick = onClick),
        color = noteColor(note.color),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = note.title,
                modifier = Modifier.weight(1f),
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onTogglePinned,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = if (note.isPinned) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (note.isPinned) "Открепить заметку" else "Закрепить заметку",
                    tint = AppColors.Ink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (note.body.isNotBlank()) {
            Text(
                text = note.body,
                color = AppColors.Muted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = if (note.attachmentCount > 0) 3 else 5,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (note.category == NoteCategory.BLOG && note.attachmentCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AppColors.White.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "Вложение",
                    tint = AppColors.Muted.copy(alpha = 0.45f),
                    modifier = Modifier.size(28.dp)
                )
            }
        } else if (note.attachmentCount > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = null,
                    tint = AppColors.Muted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${note.attachmentCount} вложения",
                    color = AppColors.Muted,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(AppColors.White.copy(alpha = 0.78f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = note.category.label,
                color = AppColors.Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NotesEmptyState(
    hasSearch: Boolean,
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
            text = if (hasSearch) "Ничего не найдено" else "Заметок пока нет",
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Ink
        )
        Text(
            text = if (hasSearch) "Попробуй изменить поисковый запрос или категорию." else "Создай первую заметку — она появится здесь.",
            color = AppColors.Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        if (!hasSearch) {
            Text(
                text = "Создать заметку",
                color = AppColors.Ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppColors.Yellow)
                    .clickable(onClick = onCreateNote)
                    .padding(horizontal = 16.dp, vertical = 11.dp)
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
