package dev.dhev.nines.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

class OnboardingCompletionStore(
    private val preferences: DataStore<Preferences>,
) {
    val completed: Flow<Boolean> = preferences.data.map { it[onboardingCompletedKey] ?: false }

    suspend fun markCompleted() {
        preferences.edit { it[onboardingCompletedKey] = true }
    }
}
