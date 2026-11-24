package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardActivityEvent
import kotlinx.coroutines.flow.Flow

/** Read access to persisted activity history. Events are written atomically by feature repositories. */
interface ActivityRepository {
    fun observeEvents(): Flow<List<DashboardActivityEvent>>
}
