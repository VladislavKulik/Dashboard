package com.example.lifedashboard.data.model

data class DashboardHabit(
    val id: String,
    val title: String,
    val completionDates: Set<String>
)
