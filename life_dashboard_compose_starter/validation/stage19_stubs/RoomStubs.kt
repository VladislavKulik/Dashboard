package androidx.room
import kotlin.reflect.KClass
@Target(AnnotationTarget.CLASS) annotation class Entity(val tableName: String = "")
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY) annotation class PrimaryKey
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY) annotation class ColumnInfo(val defaultValue: String = "")
@Target(AnnotationTarget.CLASS) annotation class Dao
@Target(AnnotationTarget.FUNCTION) annotation class Query(val value: String)
@Target(AnnotationTarget.FUNCTION) annotation class Insert(val onConflict: Int = 0)
@Target(AnnotationTarget.FUNCTION) annotation class Update
@Target(AnnotationTarget.CLASS) annotation class Database(val entities: Array<KClass<*>>, val version: Int, val exportSchema: Boolean = true)
@Target(AnnotationTarget.CLASS) annotation class TypeConverters(vararg val value: KClass<*>)
@Target(AnnotationTarget.FUNCTION) annotation class TypeConverter
object OnConflictStrategy { const val REPLACE = 1 }
abstract class RoomDatabase { open fun close() {} }
class RoomBuilder<T : RoomDatabase> {
    fun addMigrations(vararg migrations: androidx.room.migration.Migration): RoomBuilder<T> = this
    fun build(): T = error("compile-only stub")
}
object Room {
    fun <T : RoomDatabase> databaseBuilder(context: android.content.Context, klass: Class<T>, name: String): RoomBuilder<T> = RoomBuilder()
}
suspend fun <T> RoomDatabase.withTransaction(block: suspend () -> T): T = block()
