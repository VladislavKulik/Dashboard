package com.example.lifedashboard.feature.search

import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.presentation.DashboardUiState
import java.time.LocalDate

enum class SearchScope(val label: String) {
    ALL("Всё"),
    TASKS("Задачи"),
    PROJECTS("Проекты"),
    NOTES("Заметки"),
    GOALS("Цели"),
    HABITS("Привычки")
}

enum class SearchQuickFilter(val label: String) {
    OVERDUE("Просроченные"),
    PINNED("Закреплённые"),
    NO_PROJECT("Без проекта"),
    HABIT_STREAKS("Серии привычек")
}

data class GlobalSearchResult(
    val key: String,
    val scope: SearchScope,
    val entityId: String,
    val title: String,
    val subtitle: String,
    val badge: String?,
    val projectId: String? = null,
    val isCompleted: Boolean = false,
    internal val relevance: Int = 0
)

object GlobalSearchMatcher {
    fun search(
        query: String,
        state: DashboardUiState,
        scope: SearchScope = SearchScope.ALL,
        quickFilter: SearchQuickFilter? = null
    ): List<GlobalSearchResult> {
        if (quickFilter != null && query.isBlank()) {
            return quickFilterResults(quickFilter, state, scope)
        }
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return emptyList()

        val projectNames = state.projects.associate { it.id to it.name }
        val results = mutableListOf<GlobalSearchResult>()

        if (scope == SearchScope.ALL || scope == SearchScope.PROJECTS) {
            state.projects.forEach { project ->
                matchProject(project, normalizedQuery)?.let(results::add)
            }
        }
        if (scope == SearchScope.ALL || scope == SearchScope.TASKS) {
            state.tasks.forEach { task ->
                matchTask(task, projectNames[task.projectId], normalizedQuery)?.let(results::add)
            }
        }
        if (scope == SearchScope.ALL || scope == SearchScope.NOTES) {
            state.notes.forEach { note ->
                matchNote(note, normalizedQuery)?.let(results::add)
            }
        }
        if (scope == SearchScope.ALL || scope == SearchScope.GOALS) {
            state.goals.forEach { goal ->
                matchGoal(goal, projectNames[goal.projectId], normalizedQuery)?.let(results::add)
            }
        }
        if (scope == SearchScope.ALL || scope == SearchScope.HABITS) {
            state.habits.forEach { habit ->
                matchHabit(habit, normalizedQuery)?.let(results::add)
            }
        }

        return results.sortedWith(
            compareBy<GlobalSearchResult> { it.relevance }
                .thenBy { it.isCompleted }
                .thenBy { it.title.lowercase() }
        )
    }

    fun countByScope(
        query: String,
        state: DashboardUiState,
        quickFilter: SearchQuickFilter? = null
    ): Map<SearchScope, Int> {
        val all = search(query, state, SearchScope.ALL, quickFilter)
        return SearchScope.values().associateWith { scope ->
            if (scope == SearchScope.ALL) all.size else all.count { it.scope == scope }
        }
    }

    private fun quickFilterResults(
        filter: SearchQuickFilter,
        state: DashboardUiState,
        scope: SearchScope
    ): List<GlobalSearchResult> {
        val projectNames = state.projects.associate { it.id to it.name }
        val today = LocalDate.now()
        val raw = when (filter) {
            SearchQuickFilter.OVERDUE -> state.tasks
                .filter { !it.isCompleted && it.dueDate?.isBefore(today) == true }
                .map { task ->
                    GlobalSearchResult(
                        key = "task:${task.id}",
                        scope = SearchScope.TASKS,
                        entityId = task.id,
                        title = task.title,
                        subtitle = listOfNotNull(task.dueLabel, projectNames[task.projectId]).joinToString(" · "),
                        badge = task.priority.label,
                        projectId = task.projectId
                    )
                }
            SearchQuickFilter.PINNED -> state.notes
                .filter { it.isPinned }
                .map { note ->
                    GlobalSearchResult(
                        key = "note:${note.id}",
                        scope = SearchScope.NOTES,
                        entityId = note.id,
                        title = note.title,
                        subtitle = note.body.replace('\n', ' ').trim().ifBlank { note.category.label },
                        badge = note.category.label
                    )
                }
            SearchQuickFilter.NO_PROJECT -> state.tasks
                .filter { it.projectId == null && !it.isCompleted }
                .map { task ->
                    GlobalSearchResult(
                        key = "task:${task.id}",
                        scope = SearchScope.TASKS,
                        entityId = task.id,
                        title = task.title,
                        subtitle = task.dueLabel,
                        badge = task.priority.label
                    )
                }
            SearchQuickFilter.HABIT_STREAKS -> state.habits
                .sortedByDescending { HabitMetrics.currentStreak(it.completionDates) }
                .map { habit ->
                    val streak = HabitMetrics.currentStreak(habit.completionDates)
                    GlobalSearchResult(
                        key = "habit:${habit.id}",
                        scope = SearchScope.HABITS,
                        entityId = habit.id,
                        title = habit.title,
                        subtitle = "Серия $streak дн.",
                        badge = if (streak > 0) "$streak дн." else null
                    )
                }
        }
        return if (scope == SearchScope.ALL) raw else raw.filter { it.scope == scope }
    }

    private fun matchTask(
        task: DashboardTask,
        projectName: String?,
        query: String
    ): GlobalSearchResult? {
        val relevance = score(query, task.title, listOfNotNull(projectName, task.dueLabel, task.trailingLabel))
            ?: return null
        val subtitle = listOfNotNull(
            task.dueLabel.takeIf { it.isNotBlank() },
            projectName,
            task.priority.label
        ).joinToString(" · ")
        return GlobalSearchResult(
            key = "task:${task.id}",
            scope = SearchScope.TASKS,
            entityId = task.id,
            title = task.title,
            subtitle = subtitle,
            badge = if (task.isCompleted) "Готово" else null,
            projectId = task.projectId,
            isCompleted = task.isCompleted,
            relevance = relevance
        )
    }

    private fun matchProject(project: DashboardProject, query: String): GlobalSearchResult? {
        val stageTitles = project.stages.map { it.title }
        val matchedStage = project.stages.firstOrNull { it.title.contains(query, ignoreCase = true) }
        val relevance = score(query, project.name, listOf(project.description, project.deadlineLabel) + stageTitles)
            ?: return null
        val subtitle = if (matchedStage != null && !project.name.contains(query, ignoreCase = true)) {
            "Этап: ${matchedStage.title}"
        } else {
            "${project.stageLabel} · ${project.completedTasks}/${project.totalTasks} задач"
        }
        return GlobalSearchResult(
            key = "project:${project.id}",
            scope = SearchScope.PROJECTS,
            entityId = project.id,
            title = project.name,
            subtitle = subtitle,
            badge = if (project.isCompleted) "Завершён" else "${(project.progress * 100).toInt()}%",
            isCompleted = project.isCompleted,
            relevance = relevance
        )
    }

    private fun matchNote(note: DashboardNote, query: String): GlobalSearchResult? {
        val relevance = score(query, note.title, listOf(note.body, note.category.label))
            ?: return null
        val snippet = note.body.replace('\n', ' ').trim().ifBlank { note.category.label }
        return GlobalSearchResult(
            key = "note:${note.id}",
            scope = SearchScope.NOTES,
            entityId = note.id,
            title = note.title,
            subtitle = snippet,
            badge = note.category.label,
            relevance = relevance
        )
    }

    private fun matchGoal(
        goal: DashboardGoal,
        projectName: String?,
        query: String
    ): GlobalSearchResult? {
        val relevance = score(query, goal.title, listOfNotNull(goal.description, goal.unit, projectName))
            ?: return null
        val subtitle = listOfNotNull(
            "${goal.currentValue} / ${goal.targetValue} ${goal.unit}",
            projectName
        ).joinToString(" · ")
        return GlobalSearchResult(
            key = "goal:${goal.id}",
            scope = SearchScope.GOALS,
            entityId = goal.id,
            title = goal.title,
            subtitle = subtitle,
            badge = "${(goal.progress * 100).toInt()}%",
            projectId = goal.projectId,
            isCompleted = goal.isCompleted,
            relevance = relevance
        )
    }

    private fun matchHabit(habit: DashboardHabit, query: String): GlobalSearchResult? {
        val relevance = score(query, habit.title, listOf(habit.reminderLabel))
            ?: return null
        val streak = HabitMetrics.currentStreak(habit.completionDates)
        return GlobalSearchResult(
            key = "habit:${habit.id}",
            scope = SearchScope.HABITS,
            entityId = habit.id,
            title = habit.title,
            subtitle = "Серия $streak дн.",
            badge = if (streak > 0) "$streak дн." else null,
            relevance = relevance
        )
    }

    private fun score(query: String, primary: String, secondary: List<String>): Int? = when {
        primary.equals(query, ignoreCase = true) -> 0
        primary.startsWith(query, ignoreCase = true) -> 1
        primary.contains(query, ignoreCase = true) -> 2
        secondary.any { it.contains(query, ignoreCase = true) } -> 3
        else -> null
    }
}