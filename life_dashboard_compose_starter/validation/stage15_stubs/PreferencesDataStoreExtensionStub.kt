package androidx.datastore.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.flow.MutableStateFlow

private class TestPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(Preferences())
    override val data = state
    override suspend fun update(transform: (Preferences) -> Preferences) { state.value = transform(state.value) }
}

fun preferencesDataStore(name: String): ReadOnlyProperty<Context, DataStore<Preferences>> =
    object : ReadOnlyProperty<Context, DataStore<Preferences>> {
        private val store = TestPreferencesDataStore()
        override fun getValue(thisRef: Context, property: KProperty<*>): DataStore<Preferences> = store
    }
