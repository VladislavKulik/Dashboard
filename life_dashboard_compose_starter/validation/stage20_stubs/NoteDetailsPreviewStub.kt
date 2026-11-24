package com.example.lifedashboard.feature.notes
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.DashboardNote
@Composable fun NoteDetailsScreen(note: DashboardNote?, onBack: () -> Unit, onSave: (DashboardNote) -> Unit, onDelete: (String) -> Unit, operationError: String? = null) {}
