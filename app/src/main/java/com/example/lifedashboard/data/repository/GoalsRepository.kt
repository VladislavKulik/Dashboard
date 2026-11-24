package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardGoal
import kotlinx.coroutines.flow.Flow

/** Persistence API for measurable personal goals stored locally in Room. */
interface GoalsRepository {
    fun observeGoals(): Flow<List<DashboardGoal>>
    suspend fun saveGoal(goal: DashboardGoal): Boolean
    suspend fun deleteGoal(goalId: String): Boolean
    suspend fun changeProgress(goalId: String, delta: Int): Boolean
}
