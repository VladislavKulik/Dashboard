package com.example.lifedashboard.data.local

class TaskEntity(val id:String,val title:String,val trailingLabel:String?,val dueLabel:String,val projectId:String?,val dayCategory:String,val priority:String,val isCompleted:Boolean,val showOnHome:Boolean,val sortOrder:Int)
class ProjectEntity(val id:String,val name:String,val description:String,val deadlineLabel:String,val stageLabel:String,val progress:Float,val completedTasks:Int,val totalTasks:Int,val colorArgb:Int,val isCompleted:Boolean)
class ProjectStageEntity(val id:String,val projectId:String,val title:String,val isCompleted:Boolean,val isCurrent:Boolean,val sortOrder:Int)
class NoteEntity(val id:String,val title:String,val body:String,val category:String,val color:String,val updatedLabel:String,val isPinned:Boolean,val attachmentCount:Int)
class HabitEntity(val id:String,val title:String,val completionDates:String,val color:String,val reminderLabel:String,val reminderEnabled:Boolean,val reminderTime:String,val reminderDays:String)
class GoalEntity(val id:String,val title:String,val description:String,val dueDate:String?,val currentValue:Int,val targetValue:Int,val unit:String,val projectId:String?,val color:String,val sortOrder:Int)
class ActivityEventEntity(val id:String,val type:String,val itemId:String,val occurredAtEpochMillis:Long,val completed:Boolean)
class AppMetadataEntity(val key:String,val value:String)

class DashboardDao {
 suspend fun getTasks():List<TaskEntity> = emptyList()
 suspend fun getProjects():List<ProjectEntity> = emptyList()
 suspend fun getStages():List<ProjectStageEntity> = emptyList()
 suspend fun getNotes():List<NoteEntity> = emptyList()
 suspend fun getHabits():List<HabitEntity> = emptyList()
 suspend fun getGoals():List<GoalEntity> = emptyList()
 suspend fun getActivityEvents():List<ActivityEventEntity> = emptyList()
 suspend fun getAllMetadata():List<AppMetadataEntity> = emptyList()
 suspend fun clearTasks() {}
 suspend fun clearStages() {}
 suspend fun clearProjects() {}
 suspend fun clearNotes() {}
 suspend fun clearHabits() {}
 suspend fun clearGoals() {}
 suspend fun clearActivityEvents() {}
 suspend fun clearMetadata() {}
 suspend fun insertTasks(items:List<TaskEntity>) {}
 suspend fun insertProjects(items:List<ProjectEntity>) {}
 suspend fun insertStages(items:List<ProjectStageEntity>) {}
 suspend fun insertNotes(items:List<NoteEntity>) {}
 suspend fun insertHabits(items:List<HabitEntity>) {}
 suspend fun insertGoals(items:List<GoalEntity>) {}
 suspend fun insertEvents(items:List<ActivityEventEntity>) {}
 suspend fun insertMetadataItems(items:List<AppMetadataEntity>) {}
 suspend fun getMetadata(key:String):AppMetadataEntity? = null
 suspend fun insertMetadata(item:AppMetadataEntity) {}
}
class LifeDashboardDatabase: androidx.room.RoomDatabase() { fun dashboardDao() = DashboardDao() }
