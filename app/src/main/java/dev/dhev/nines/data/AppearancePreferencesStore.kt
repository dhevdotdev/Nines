package dev.dhev.nines.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.dhev.nines.appearance.AppColorSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val appColorSourceKey = stringPreferencesKey("app_color_source")

class AppearancePreferencesStore(
    private val preferences: DataStore<Preferences>,
) {
    val colorSource: Flow<AppColorSource> = preferences.data.map {
        AppColorSource.fromStorage(it[appColorSourceKey])
    }

    suspend fun select(source: AppColorSource) {
        preferences.edit { it[appColorSourceKey] = source.storageValue }
    }
}
