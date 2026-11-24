package androidx.datastore.preferences.core

import androidx.datastore.core.DataStore

open class Preferences {
    class Key<T>(val name: String)
    protected val values = mutableMapOf<String, Any>()
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: Key<T>): T? = values[key.name] as? T
}

class MutablePreferences : Preferences() {
    operator fun <T> set(key: Key<T>, value: T) { values[key.name] = value as Any }
}

fun stringPreferencesKey(name: String) = Preferences.Key<String>(name)
fun booleanPreferencesKey(name: String) = Preferences.Key<Boolean>(name)
fun emptyPreferences() = Preferences()

suspend fun DataStore<Preferences>.edit(transform: (MutablePreferences) -> Unit) {
    update { current ->
        val mutable = MutablePreferences()
        // Copy known test values through the public keys used by the app.
        mutable[ stringPreferencesKey("theme_mode") ] = current[stringPreferencesKey("theme_mode")] ?: "SYSTEM"
        mutable[ booleanPreferencesKey("notifications_enabled") ] = current[booleanPreferencesKey("notifications_enabled")] ?: true
        mutable[ booleanPreferencesKey("show_completed_tasks_on_home") ] = current[booleanPreferencesKey("show_completed_tasks_on_home")] ?: true
        transform(mutable)
        mutable
    }
}
