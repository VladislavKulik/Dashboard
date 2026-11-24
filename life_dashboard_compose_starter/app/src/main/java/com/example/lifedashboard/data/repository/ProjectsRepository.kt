package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardProject
import kotlinx.coroutines.flow.Flow

/** Project persistence API. Project stages are stored with their parent project atomically. */
interface ProjectsRepository {
    fun observeProjects(): Flow<List<DashboardProject>>
    suspend fun saveProject(project: DashboardProject)
    suspend fun deleteProject(projectId: String): Boolean
    suspend fun toggleStage(projectId: String, stageId: String): Boolean
}
