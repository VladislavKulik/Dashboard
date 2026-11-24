package com.example.lifedashboard.data.backup
import com.example.lifedashboard.data.local.LifeDashboardDatabase
import com.example.lifedashboard.data.repository.BackupRepository
class RoomDashboardBackupRepository(database: LifeDashboardDatabase): BackupRepository {
 override suspend fun exportJson(includeSettings:Boolean, settings:com.example.lifedashboard.data.model.DashboardSettings?):String = ""
 override suspend fun restoreJson(json:String):com.example.lifedashboard.data.repository.BackupRestoreResult = error("stub")
}
