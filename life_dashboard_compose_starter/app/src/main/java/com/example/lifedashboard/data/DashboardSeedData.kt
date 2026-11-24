package com.example.lifedashboard.data

import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.HabitColor
import com.example.lifedashboard.data.model.NoteCategory
import com.example.lifedashboard.data.model.NoteColor
import com.example.lifedashboard.data.model.ProjectStage
import com.example.lifedashboard.data.model.TaskDayCategory
import com.example.lifedashboard.data.model.TaskPriority

/** Starter content used only when a database is first created. */
object DashboardSeedData {
    fun tasks() = listOf(
        DashboardTask("p1-design-system", "Дизайн-система", "Mobile App", "Готово", "mobile-app", isCompleted = true),
        DashboardTask("p1-transitions", "Анимации переходов", "Mobile App", "Сегодня · 16:00", "mobile-app", priority = TaskPriority.HIGH),
        DashboardTask("home-analytics", "Макет экрана аналитики", "App", "Сегодня", "mobile-app", isCompleted = true, showOnHome = true, priority = TaskPriority.MEDIUM),
        DashboardTask("home-meeting", "Созвон по релизу", "14:00", "Сегодня · 14:00", showOnHome = true, priority = TaskPriority.HIGH),
        DashboardTask("home-api-note", "Заметка по API", "Идеи", "Сегодня", showOnHome = true, priority = TaskPriority.LOW),
        DashboardTask("p1-task-screen", "Экран задач", dueLabel = "Завтра", projectId = "mobile-app", dayCategory = TaskDayCategory.UPCOMING, priority = TaskPriority.HIGH),
        DashboardTask("p1-room", "Подключить Room", dueLabel = "27 ноя", projectId = "mobile-app", dayCategory = TaskDayCategory.UPCOMING),
        DashboardTask("p2-outline", "Подготовить структуру статьи", dueLabel = "26 ноя", projectId = "productivity-blog", dayCategory = TaskDayCategory.UPCOMING, priority = TaskPriority.MEDIUM),
        DashboardTask("p3-coroutines", "Повторить Flow и Coroutines", dueLabel = "28 ноя", projectId = "kotlin-study", dayCategory = TaskDayCategory.UPCOMING, priority = TaskPriority.LOW)
    )

    fun projects() = listOf(
        DashboardProject(
            id = "mobile-app", name = "Мобильное приложение",
            description = "Персональное пространство для задач, проектов, заметок и личной активности.",
            deadlineLabel = "29 ноя", stageLabel = "Этап 2 из 4", progress = 0.62f,
            completedTasks = 12, totalTasks = 19, colorArgb = 0xFFDCCBFF.toInt(),
            stages = listOf(
                ProjectStage("p1-stage-1", "Планирование", isCompleted = true),
                ProjectStage("p1-stage-2", "Дизайн интерфейса", isCurrent = true),
                ProjectStage("p1-stage-3", "Разработка"),
                ProjectStage("p1-stage-4", "Тестирование и релиз")
            )
        ),
        DashboardProject(
            id = "productivity-blog", name = "Блог о продуктивности",
            description = "Подготовка материалов и запуск небольшого блога о продуктивности.",
            deadlineLabel = "15 дек", stageLabel = "Этап 1 из 3", progress = 0.28f,
            completedTasks = 4, totalTasks = 14, colorArgb = 0xFFBFEBDD.toInt(),
            stages = listOf(
                ProjectStage("p2-stage-1", "Идея и структура", isCurrent = true),
                ProjectStage("p2-stage-2", "Подготовка материалов"),
                ProjectStage("p2-stage-3", "Публикация")
            )
        ),
        DashboardProject(
            id = "kotlin-study", name = "Изучение Kotlin",
            description = "Практика Kotlin, Coroutines и современных Android-инструментов.",
            deadlineLabel = "Без срока", stageLabel = "Этап 3 из 5", progress = 0.45f,
            completedTasks = 9, totalTasks = 20, colorArgb = 0xFFFFC8DC.toInt(),
            stages = listOf(
                ProjectStage("p3-stage-1", "Основы языка", isCompleted = true),
                ProjectStage("p3-stage-2", "ООП и коллекции", isCompleted = true),
                ProjectStage("p3-stage-3", "Coroutines и Flow", isCurrent = true),
                ProjectStage("p3-stage-4", "Jetpack Compose"),
                ProjectStage("p3-stage-5", "Практический проект")
            )
        ),
        DashboardProject(
            id = "personal-site", name = "Личный сайт",
            description = "Завершённый небольшой проект по обновлению личного сайта.",
            deadlineLabel = "Готово", stageLabel = "Все этапы завершены", progress = 1f,
            completedTasks = 8, totalTasks = 8, colorArgb = 0xFFBFE2FF.toInt(), isCompleted = true,
            stages = listOf(
                ProjectStage("p4-stage-1", "Структура", isCompleted = true),
                ProjectStage("p4-stage-2", "Дизайн", isCompleted = true),
                ProjectStage("p4-stage-3", "Публикация", isCompleted = true)
            )
        )
    )

    fun notes() = listOf(
        DashboardNote("note-weekly-review", "Недельный обзор", "Каждое воскресенье ставить три приоритета на следующую неделю и подводить итоги предыдущей.", NoteCategory.APP, NoteColor.YELLOW, "Сегодня"),
        DashboardNote("note-meeting", "Встреча", "Сократить MVP до трёх экранов. Отдельно обсудить навигацию и первый релиз.", NoteCategory.APP, NoteColor.PINK, "Вчера"),
        DashboardNote("note-books", "Список книг", "Подборка книг, которые хочу прочитать. Добавить заметки после каждой прочитанной книги.", NoteCategory.PERSONAL, NoteColor.MINT, "18 ноя", attachmentCount = 3),
        DashboardNote("note-references", "Референсы", "Визуальные идеи и материалы для блога. Собрать примеры карточек, типографики и композиции.", NoteCategory.BLOG, NoteColor.LAVENDER, "16 ноя", attachmentCount = 1)
    )

    fun habits() = listOf(
        DashboardHabit("habit-morning-energy", "Утренняя зарядка", habitDates(1..14, 16..20), HabitColor.PINK),
        DashboardHabit("habit-reading", "Чтение 20 минут", habitDates(1..5), HabitColor.BLUE)
    )

    private fun habitDates(vararg ranges: IntRange): Set<String> =
        ranges.flatMap { range -> range.map(DateKeys::keyForDaysAgo) }.toSet()
}
