package com.example.lifedashboard.data.model

import com.example.lifedashboard.R

/** Persisted, app-wide preferences. Defaults preserve the current light-dashboard experience. */
enum class DashboardThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class HomeBlockType(val titleResId: Int, val descResId: Int) {
    STATS(R.string.settings_stats_title, R.string.settings_stats_desc),
    QUICK_ACCESS(R.string.settings_quick_access_title, R.string.settings_quick_access_desc),
    TODAY_TASKS(R.string.settings_today_tasks_title, R.string.settings_today_tasks_desc),
    ACTIVITY(R.string.settings_activity_title, R.string.settings_activity_desc)
}

data class HomeBlockConfig(
    val type: HomeBlockType,
    val visible: Boolean = true
) {
    companion object {
        val DEFAULT_ORDER: List<HomeBlockConfig> =
            HomeBlockType.values().map { HomeBlockConfig(type = it, visible = true) }

        fun encode(blocks: List<HomeBlockConfig>): String =
            normalize(blocks).joinToString(",") { "${it.type.name}:${if (it.visible) 1 else 0}" }

        fun decode(raw: String?): List<HomeBlockConfig> {
            if (raw.isNullOrBlank()) return DEFAULT_ORDER
            val parsed = raw.split(',')
                .mapNotNull { entry ->
                    val parts = entry.split(':')
                    if (parts.size != 2) return@mapNotNull null
                    val type = runCatching { HomeBlockType.valueOf(parts[0].trim()) }.getOrNull()
                        ?: return@mapNotNull null
                    val visible = parts[1].trim() != "0"
                    HomeBlockConfig(type = type, visible = visible)
                }
            return normalize(parsed)
        }

        fun normalize(blocks: List<HomeBlockConfig>): List<HomeBlockConfig> {
            val unique = blocks.distinctBy { it.type }
            val present = unique.map { it.type }.toSet()
            val missing = HomeBlockType.values()
                .filter { it !in present }
                .map { HomeBlockConfig(type = it, visible = true) }
            return unique + missing
        }
    }
}

data class DashboardSettings(
    val themeMode: DashboardThemeMode = DashboardThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val showCompletedTasksOnHome: Boolean = true,
    val homeBlocks: List<HomeBlockConfig> = HomeBlockConfig.DEFAULT_ORDER,
    val profileName: String = "",
    val profileEmail: String = "",
    val dailyTaskGoal: Int = 5
)