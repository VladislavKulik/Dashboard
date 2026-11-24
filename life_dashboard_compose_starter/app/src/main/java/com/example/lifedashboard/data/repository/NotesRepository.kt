package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardNote
import kotlinx.coroutines.flow.Flow

/** Notes persistence API. The UI observes the list and sends commands through the ViewModel. */
interface NotesRepository {
    fun observeNotes(): Flow<List<DashboardNote>>

    suspend fun saveNote(note: DashboardNote)

    suspend fun deleteNote(noteId: String)

    suspend fun togglePinned(noteId: String)
}
