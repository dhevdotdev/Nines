package dev.dhev.nines.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val combineSimultaneousKey = booleanPreferencesKey("combine_simultaneous_reminders")

data class ReminderPreferences(
    val combineSimultaneous: Boolean = false,
)

class ReminderPreferencesStore(
    private val preferences: DataStore<Preferences>,
) {
    val preferencesFlow: Flow<ReminderPreferences> =
        preferences.data.map {
            ReminderPreferences(it[combineSimultaneousKey] ?: false)
        }

    suspend fun setCombineSimultaneous(enabled: Boolean) {
        preferences.edit { it[combineSimultaneousKey] = enabled }
    }
}
