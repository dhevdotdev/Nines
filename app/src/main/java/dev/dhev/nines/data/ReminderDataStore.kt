package dev.dhev.nines.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.reminderPreferences by preferencesDataStore(name = "nines_reminders")

fun Context.reminderPreferencesStore(): ReminderPreferencesStore = ReminderPreferencesStore(reminderPreferences)
