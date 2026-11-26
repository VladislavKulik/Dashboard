package com.example.dashboard

import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.HomeBlockConfig
import com.example.lifedashboard.data.model.HomeBlockType
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.ProjectStage
import com.example.lifedashboard.feature.search.GlobalSearchMatcher
import com.example.lifedashboard.feature.search.SearchScope
import com.example.lifedashboard.presentation.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalSearchAndLayoutTest {

    private val sampleState = DashboardUiState(
        tasks = listOf(
            DashboardTask("t1", "Релиз приложения", dueLabel = "Сегодня", projectId = "p1"),
            DashboardTask("t2", "Купить кофе", dueLabel = "Завтра", isCompleted = true)
        ),
        projects = listOf(
            DashboardProject(
                id = "p1",
                name = "Мобильный релиз",
                description = "Подготовка сборки",
                deadlineLabel = "29 ноя",
                stageLabel = "Этап 1",
                progress = 0.5f,
                completedTasks = 1,
                totalTasks = 2,
                colorArgb = 0xFFDCCBFF.toInt(),
                stages = listOf(ProjectStage("s1", "Финальный тест"))
            )
        ),
        notes = listOf(
            DashboardNote("n1", "Чеклист", "Проверить релиз и заметки", NoteCategory.APP)
        ),
        goals = listOf(
            DashboardGoal("g1", "Выпустить релиз 1.0", targetValue = 10, currentValue = 4, projectId = "p1")
        ),
        habits = listOf(
            DashboardHabit("h1", "Чтение перед сном")
        )
    )

    @Test
    fun globalSearchFindsAcrossAllEntitiesAndRanksExactPrefixHigher() {
        val results = GlobalSearchMatcher.search("релиз", sampleState, SearchScope.ALL, null) { "" }
        assertEquals(4, results.size)
        assertEquals(SearchScope.TASKS, results.first().scope)
        assertTrue(results.any { it.scope == SearchScope.PROJECTS })
        assertTrue(results.any { it.scope == SearchScope.NOTES })
        assertTrue(results.any { it.scope == SearchScope.GOALS })
    }

    @Test
    fun globalSearchMatchesProjectStagesAndLinkedProjectName() {
        val stageMatch = GlobalSearchMatcher.search("финальный", sampleState, SearchScope.PROJECTS, null) { "" }
        assertEquals(1, stageMatch.size)
        assertEquals("p1", stageMatch.first().entityId)

        val linkedTaskMatch = GlobalSearchMatcher.search("Мобильный релиз", sampleState, SearchScope.TASKS, null) { "" }
        assertEquals(1, linkedTaskMatch.size)
        assertEquals("t1", linkedTaskMatch.first().entityId)
    }

    @Test
    fun homeBlockCodecRestoresOrderVisibilityAndMissingTypes() {
        val encoded = HomeBlockConfig.encode(
            listOf(
                HomeBlockConfig(HomeBlockType.TODAY_TASKS, visible = true),
                HomeBlockConfig(HomeBlockType.STATS, visible = false)
            )
        )
        val decoded = HomeBlockConfig.decode(encoded)
        assertEquals(HomeBlockType.values().size, decoded.size)
        assertEquals(HomeBlockType.TODAY_TASKS, decoded[0].type)
        assertTrue(decoded[0].visible)
        assertEquals(HomeBlockType.STATS, decoded[1].type)
        assertFalse(decoded[1].visible)
        assertTrue(decoded[2].visible)
    }
}