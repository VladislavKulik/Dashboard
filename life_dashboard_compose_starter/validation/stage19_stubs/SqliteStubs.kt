package androidx.sqlite.db
import java.io.Closeable
interface Cursor : Closeable { fun moveToNext(): Boolean; fun getString(index: Int): String }
interface SupportSQLiteDatabase {
    fun execSQL(sql: String)
    fun execSQL(sql: String, bindArgs: Array<out Any?>)
    fun query(sql: String): Cursor
}
