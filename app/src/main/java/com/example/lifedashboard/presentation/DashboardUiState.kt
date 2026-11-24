package com.example.lifedashboard.presentation

import com.example.lifedashboard.data.model.DashboardActivityEvent
import com.example.lifedashboard.data.model.DashboardGoal
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.DashboardProject
import com.example.lifedashboard.data.model.DashboardTask
import com.example.lifedashboard.data.model.NoteAttachment
import com.example.lifedashboard.data.model.TaskSubtask
import com.example.lifedashboard.data.model.TaskTimerSession

/** Immutable state assembled from feature repository streams. */
data class DashboardUiState(
    val tasks: List<DashboardTask> = emptyList(),
    val projects: List<DashboardProject> = emptyList(),
    val notes: List<DashboardNote> = emptyList(),
    val habits: List<DashboardHabit> = emptyList(),
    val activityEvents: List<DashboardActivityEvent> = emptyList(),
    val goals: List<DashboardGoal> = emptyList(),
    val subtasks: List<TaskSubtask> = emptyList(),
    val timerSessions: List<TaskTimerSession> = emptyList(),
    val noteAttachments: List<NoteAttachment> = emptyList()
)