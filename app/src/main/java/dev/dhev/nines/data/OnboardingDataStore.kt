package dev.dhev.nines.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.onboardingPreferences by preferencesDataStore(name = "nines_onboarding")

fun Context.onboardingCompletionStore(): OnboardingCompletionStore =
    OnboardingCompletionStore(onboardingPreferences)
