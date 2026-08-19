package dev.dhev.nines.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderPreferencesStoreTest {
    @Test
    fun combinePreferencePersistsAcrossStoreReads() = runBlocking {
        val store = ApplicationProvider.getApplicationContext<Context>().reminderPreferencesStore()
        store.setCombineSimultaneous(true)
        assertTrue(store.preferencesFlow.first().combineSimultaneous)
        store.setCombineSimultaneous(false)
        assertFalse(store.preferencesFlow.first().combineSimultaneous)
    }
}
