package dev.dhev.nines.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class OnboardingCompletionStoreTest {
    @Test
    fun completionIsFalseInitiallyAndPersistsAfterMarkingComplete() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fileName = "onboarding-test-${UUID.randomUUID()}"
        val dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(fileName)
        }
        val store = OnboardingCompletionStore(dataStore)

        assertFalse(store.completed.first())
        store.markCompleted()
        assertTrue(store.completed.first())
    }
}
