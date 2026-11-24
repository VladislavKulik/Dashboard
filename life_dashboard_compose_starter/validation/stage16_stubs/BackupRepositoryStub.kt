package com.example.lifedashboard.data.repository
import com.example.lifedashboard.data.model.DashboardSettings
data class BackupRestoreResult(val taskCount:Int,val projectCount:Int,val noteCount:Int,val habitCount:Int,val eventCount:Int,val settings:DashboardSettings?,val settingsRestored:Boolean?=null,val goalCount:Int=0)
interface BackupRepository { suspend fun exportJson(includeSettings:Boolean, settings:DashboardSettings?):String; suspend fun restoreJson(json:String):BackupRestoreResult }
