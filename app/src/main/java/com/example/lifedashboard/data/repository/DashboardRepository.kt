package com.example.lifedashboard.data.repository

/** Application-level initialization for the local database. */
interface DashboardRepository {
    /** Seeds the starter dataset once, without overwriting an existing user database. */
    suspend fun initializeIfNeeded()
}
