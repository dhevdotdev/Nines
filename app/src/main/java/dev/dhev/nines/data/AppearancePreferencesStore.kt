package dev.dhev.nines.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import dev.dhev.nines.appearance.AppColorSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val appColorSourceKey = stringPreferencesKey("app_color_source")
private val customSeedArgbKey = longPreferencesKey("custom_seed_argb")

data class AppearancePreferences(val colorSource: AppColorSource = AppColorSource.WALLPAPER, val customSeedArgb: Long = 0xFFCBA6F7L)

class AppearancePreferencesStore(
    private val preferences: DataStore<Preferences>,
) {
    val appearance: Flow<AppearancePreferences> = preferences.data.map { AppearancePreferences(AppColorSource.fromStorage(it[appColorSourceKey]), it[customSeedArgbKey] ?: 0xFFCBA6F7L) }
    val colorSource: Flow<AppColorSource> = appearance.map { it.colorSource }

    suspend fun select(source: AppColorSource) {
        preferences.edit { it[appColorSourceKey] = source.storageValue }
    }

    suspend fun selectCustom(seedArgb: Long) { preferences.edit { it[customSeedArgbKey] = seedArgb; it[appColorSourceKey] = AppColorSource.CUSTOM.storageValue } }
}
