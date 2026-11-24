package androidx.room
abstract class RoomDatabase
suspend fun <T> RoomDatabase.withTransaction(block: suspend () -> T): T = block()
