package com.example.lifedashboard.feature.notes
import androidx.compose.runtime.Composable
import com.example.lifedashboard.data.model.DashboardNote
@Composable fun NotesScreen(notes: List<DashboardNote>, onBack: () -> Unit, onOpenNote: (String) -> Unit, onCreateNote: () -> Unit, onTogglePinned: (String) -> Unit, operationError: String? = null) {}
