package androidx.room
import android.content.Context
import androidx.room.migration.Migration
@Target(AnnotationTarget.CLASS) annotation class Entity(val tableName: String = "")
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY) annotation class PrimaryKey
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY) annotation class ColumnInfo(val defaultValue: String = "")
@Target(AnnotationTarget.CLASS) annotation class Dao
@Target(AnnotationTarget.FUNCTION) annotation class Query(val value: String)
@Target(AnnotationTarget.FUNCTION) annotation class Insert(val onConflict: Int = 0)
@Target(AnnotationTarget.FUNCTION) annotation class Update
@Target(AnnotationTarget.CLASS) annotation class Database(val entities: Array<kotlin.reflect.KClass<*>>, val version: Int, val exportSchema: Boolean = true)
object OnConflictStrategy { const val REPLACE = 1 }
abstract class RoomDatabase { open fun close() {} }
class RoomBuilder<T : RoomDatabase> { fun addMigrations(vararg migrations: Migration): RoomBuilder<T> = this; fun build(): T = error("Compile-only stub") }
object Room { fun <T : RoomDatabase> databaseBuilder(context: Context, klass: Class<T>, name: String): RoomBuilder<T> = RoomBuilder() }
suspend fun <T> RoomDatabase.withTransaction(block: suspend () -> T): T = block()
