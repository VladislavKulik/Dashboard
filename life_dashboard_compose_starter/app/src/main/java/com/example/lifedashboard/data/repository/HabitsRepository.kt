package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardHabit
import kotlinx.coroutines.flow.Flow

/** Habit persistence API. Completion history is updated atomically by date key. */
interface HabitsRepository {
    fun observeHabits(): Flow<List<DashboardHabit>>

    suspend fun getHabit(habitId: String): DashboardHabit?

    suspend fun getEnabledHabits(): List<DashboardHabit>

    /** Creates a habit with empty completion history. */
    suspend fun createHabit(habit: DashboardHabit): Boolean

    /** Updates editable fields while preserving the stored completion history. */
    suspend fun updateHabit(habit: DashboardHabit): Boolean

    suspend fun deleteHabit(habitId: String): Boolean

    suspend fun toggleCompletion(habitId: String, dateKey: String): Boolean
}
