package dev.dhev.nines.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.appearancePreferences by preferencesDataStore(name = "nines_appearance")

fun Context.appearancePreferencesStore(): AppearancePreferencesStore = AppearancePreferencesStore(appearancePreferences)
