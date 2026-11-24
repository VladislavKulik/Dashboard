package com.example.lifedashboard.feature.search

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.presentation.DashboardUiState
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    state: DashboardUiState,
    onBack: (() -> Unit)? = null,
    onResultClick: (GlobalSearchResult) -> Unit,
    recentQueries: List<String> = emptyList(),
    onSaveRecentQuery: (String) -> Unit = {},
    onRemoveRecentQuery: (String) -> Unit = {},
    initialScope: SearchScope = SearchScope.ALL
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedScope by rememberSaveable(initialScope) { mutableStateOf(initialScope) }
    var selectedQuickFilter by rememberSaveable { mutableStateOf<SearchQuickFilter?>(null) }

    val hasActiveCriteria = query.isNotBlank() || selectedQuickFilter != null
    val scopeCounts = remember(query, selectedQuickFilter, state) {
        GlobalSearchMatcher.countByScope(query, state, selectedQuickFilter)
    }
    val results = remember(query, selectedScope, selectedQuickFilter, state) {
        GlobalSearchMatcher.search(query, state, selectedScope, selectedQuickFilter)
    }
    val grouped = remember(results) {
        SearchScope.values()
            .filter { it != SearchScope.ALL }
            .mapNotNull { scope ->
                val list = results.filter { it.scope == scope }
                if (list.isEmpty()) null else scope to list
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.White,
            shape = CircleShape,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (onBack != null) 6.dp else 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад", tint = AppColors.Ink)
                    }
                } else {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        val hint = when {
                            selectedQuickFilter != null -> selectedQuickFilter!!.label
                            selectedScope != SearchScope.ALL -> "Поиск в разделе «${selectedScope.label}»"
                            else -> "Поиск по всему приложению"
                        }
                        Text(
                            text = hint,
                            color = AppColors.Muted,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (it.isNotBlank()) selectedQuickFilter = null
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = AppColors.Ink,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(AppColors.Ink),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (hasActiveCriteria) {
                    IconButton(
                        onClick = {
                            query = ""
                            selectedQuickFilter = null
                            selectedScope = initialScope
                        }
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = "Очистить", tint = AppColors.Ink)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchScope.values().forEach { scope ->
                val count = if (!hasActiveCriteria) null else scopeCounts[scope]?.toString()
                FilterPill(
                    label = scope.label,
                    selected = selectedScope == scope,
                    onClick = { selectedScope = scope },
                    count = count
                )
            }
        }

        if (!hasActiveCriteria) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (recentQueries.isNotEmpty()) {
                    Text("Недавние", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    PastelCard(
                        color = AppColors.White,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recentQueries.forEachIndexed { index, recent ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedQuickFilter = null
                                        query = recent
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(recent, color = AppColors.Ink, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                IconButton(onClick = { onRemoveRecentQuery(recent) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Удалить", tint = AppColors.Muted, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (index < recentQueries.lastIndex) {
                                HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }

                Text("Быстрые фильтры", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchQuickFilter.values().forEach { quick ->
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    query = ""
                                    selectedScope = SearchScope.ALL
                                    selectedQuickFilter = quick
                                },
                            color = AppColors.White,
                            shape = CircleShape
                        ) {
                            Text(
                                text = quick.label,
                                color = AppColors.Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        } else if (grouped.isEmpty()) {
            SearchEmptyNotFound(
                onReset = {
                    query = ""
                    selectedQuickFilter = null
                    selectedScope = SearchScope.ALL
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                grouped.forEach { (scope, itemsInGroup) ->
                    item(key = "header-${scope.name}") {
                        Text(scope.label, color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    item(key = "group-${scope.name}") {
                        PastelCard(
                            color = AppColors.White,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsInGroup.forEachIndexed { index, item ->
                                SearchResultRow(
                                    item = item,
                                    highlightQuery = query.trim(),
                                    onClick = {
                                        if (query.isNotBlank()) onSaveRecentQuery(query.trim())
                                        onResultClick(item)
                                    }
                                )
                                if (index < itemsInGroup.lastIndex) {
                                    HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.45f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: GlobalSearchResult,
    highlightQuery: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(scopeColor(item.scope)),
            contentAlignment = Alignment.Center
        ) {
            Icon(scopeIcon(item.scope), contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = highlightedText(item.title, highlightQuery, AppColors.Yellow),
                color = AppColors.Ink,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = highlightedText(item.subtitle, highlightQuery, AppColors.Yellow),
                    color = AppColors.Muted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        item.badge?.let { badge ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Background)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(badge, color = AppColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SearchEmptyNotFound(onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AppColors.Yellow),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.size(16.dp))
        Text("Ничего не найдено", style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
        Spacer(Modifier.size(6.dp))
        Text(
            text = "Проверьте запрос или выберите фильтр «Всё»",
            color = AppColors.Muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(16.dp))
        Surface(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onReset),
            color = AppColors.White,
            shape = CircleShape
        ) {
            Text(
                text = "Очистить запрос",
                color = AppColors.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

private fun highlightedText(
    source: String,
    query: String,
    highlightColor: Color
) = buildAnnotatedString {
    if (query.isBlank()) {
        append(source)
        return@buildAnnotatedString
    }
    var index = 0
    while (index < source.length) {
        val matchIndex = source.indexOf(query, startIndex = index, ignoreCase = true)
        if (matchIndex < 0) {
            append(source.substring(index))
            break
        }
        if (matchIndex > index) {
            append(source.substring(index, matchIndex))
        }
        withStyle(SpanStyle(background = highlightColor)) {
            append(source.substring(matchIndex, matchIndex + query.length))
        }
        index = matchIndex + query.length
    }
}

@Composable
private fun scopeColor(scope: SearchScope): Color = when (scope) {
    SearchScope.TASKS -> AppColors.Blue
    SearchScope.PROJECTS -> AppColors.Lavender
    SearchScope.NOTES -> AppColors.Yellow
    SearchScope.GOALS -> AppColors.Pink
    SearchScope.HABITS -> AppColors.Mint
    SearchScope.ALL -> AppColors.Background
}

private fun scopeIcon(scope: SearchScope): ImageVector = when (scope) {
    SearchScope.TASKS -> Icons.Outlined.CheckCircleOutline
    SearchScope.PROJECTS -> Icons.Outlined.FolderOpen
    SearchScope.NOTES -> Icons.Outlined.Description
    SearchScope.GOALS -> Icons.Outlined.Flag
    SearchScope.HABITS -> Icons.Outlined.Repeat
    SearchScope.ALL -> Icons.Outlined.Search
}