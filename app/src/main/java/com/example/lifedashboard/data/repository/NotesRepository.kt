package com.example.lifedashboard.data.repository

import com.example.lifedashboard.data.model.DashboardNote
import com.example.lifedashboard.data.model.NoteAttachment
import kotlinx.coroutines.flow.Flow

/** Notes persistence API. The UI observes the list and attachments and sends commands through the ViewModel. */
interface NotesRepository {
    fun observeNotes(): Flow<List<DashboardNote>>
    fun observeAttachments(): Flow<List<NoteAttachment>>

    suspend fun saveNote(note: DashboardNote)
    suspend fun deleteNote(noteId: String)
    suspend fun togglePinned(noteId: String)

    suspend fun addAttachment(attachment: NoteAttachment): Boolean
    suspend fun deleteAttachment(attachmentId: String): Boolean
}