package dev.dhev.nines.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.appearance.AppColorSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AppearancePreferencesStoreTest {
    @Test
    fun selectedColorSourcePersists() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("appearance-test-${UUID.randomUUID()}")
        }
        val store = AppearancePreferencesStore(dataStore)

        assertEquals(AppColorSource.WALLPAPER, store.colorSource.first())
        store.select(AppColorSource.FOREST)
        assertEquals(AppColorSource.FOREST, store.colorSource.first())
    }
}
