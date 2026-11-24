package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardSettings

/** Result summary returned after a validated, transactional data restore. */
data class BackupRestoreResult(
    val taskCount: Int,
    val projectCount: Int,
    val noteCount: Int,
    val habitCount: Int,
    val eventCount: Int,
    val settings: DashboardSettings?,
    val settingsRestored: Boolean? = null,
    val goalCount: Int = 0
)

/** Versioned JSON backup boundary. Restores replace feature data, never merge it silently. */
interface BackupRepository {
    suspend fun exportJson(includeSettings: Boolean, settings: DashboardSettings?): String
    suspend fun restoreJson(json: String): BackupRestoreResult
}
