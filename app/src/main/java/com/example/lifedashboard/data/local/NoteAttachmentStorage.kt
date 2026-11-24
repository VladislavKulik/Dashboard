package com.example.lifedashboard.data.local

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.lifedashboard.data.model.NoteAttachment
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Stores note attachments in app-private storage so access never depends on temporary SAF URI grants. */
object NoteAttachmentStorage {
    private const val MAX_ATTACHMENT_BYTES = 25L * 1024L * 1024L

    suspend fun importFromUri(context: Context, noteId: String, uri: Uri): NoteAttachment =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val attachmentId = UUID.randomUUID().toString()
            val rawName = queryDisplayName(context, uri)
                ?.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                ?.take(80)
                ?.takeIf { it.isNotBlank() }
                ?: "file_${System.currentTimeMillis()}"
            val mimeType = resolver.getType(uri) ?: "application/octet-stream"

            val dir = File(File(context.filesDir, "note_attachments"), noteId)
            if (!dir.exists()) dir.mkdirs()
            val target = File(dir, "${attachmentId}_$rawName")

            val input = resolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось прочитать выбранный файл.")
            var totalBytes = 0L
            try {
                input.use { stream ->
                    target.outputStream().use { out ->
                        val buffer = ByteArray(8 * 1024)
                        while (true) {
                            val read = stream.read(buffer)
                            if (read < 0) break
                            totalBytes += read
                            require(totalBytes <= MAX_ATTACHMENT_BYTES) {
                                "Файл слишком большой. Максимум — 25 МБ."
                            }
                            out.write(buffer, 0, read)
                        }
                    }
                }
            } catch (e: Exception) {
                target.delete()
                throw e
            }

            NoteAttachment(
                id = attachmentId,
                noteId = noteId,
                displayName = rawName,
                mimeType = mimeType,
                localPath = target.absolutePath,
                sizeBytes = totalBytes,
                createdAtEpochMillis = System.currentTimeMillis()
            )
        }

    fun deleteFile(localPath: String) {
        if (localPath.isBlank()) return
        runCatching {
            val file = File(localPath)
            if (file.exists()) file.delete()
        }
    }

    fun deleteNoteDirectory(context: Context, noteId: String) {
        if (noteId.isBlank()) return
        runCatching {
            val dir = File(File(context.filesDir, "note_attachments"), noteId)
            if (dir.exists()) dir.deleteRecursively()
        }
    }

    suspend fun loadThumbnail(localPath: String, maxEdgePx: Int = 420): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(localPath)
                if (!file.exists()) return@runCatching null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(localPath, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                var sampleSize = 1
                while (bounds.outWidth / sampleSize > maxEdgePx || bounds.outHeight / sampleSize > maxEdgePx) {
                    sampleSize *= 2
                }
                val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                BitmapFactory.decodeFile(localPath, opts)
            }.getOrNull()
        }

    fun openAttachment(context: Context, attachment: NoteAttachment): Boolean {
        val file = File(attachment.localPath)
        if (!file.exists()) return false
        return runCatching {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, attachment.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, attachment.displayName))
            true
        }.getOrDefault(false)
    }

    fun formatSize(bytes: Long): String = when {
        bytes < 1024L -> "$bytes Б"
        bytes < 1024L * 1024L -> "${(bytes / 1024L).coerceAtLeast(1)} КБ"
        else -> String.format("%.1f МБ", bytes / (1024.0 * 1024.0))
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
            } else null
        }
    }.getOrNull()
}