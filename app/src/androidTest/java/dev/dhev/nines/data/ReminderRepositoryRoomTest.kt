package dev.dhev.nines.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.data.local.NinesDatabase
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class ReminderRepositoryRoomTest {
    private lateinit var database: NinesDatabase
    private lateinit var repository: ReminderRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NinesDatabase::class.java).build()
        repository = ReminderRepository(database.reminderDao())
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun reminderConfigurationPersistsIndependentlyOfEntries() = runBlocking {
        val habitId =
            database.habitDao().insert(
                HabitEntity(
                    name = "Meditate",
                    type = HabitType.BOOLEAN,
                    color = 0,
                    iconKey = "meditate",
                    position = 0,
                    createdAt = Instant.parse("2026-08-19T00:00:00Z"),
                ),
            )

        repository.configure(habitId, LocalTime.of(19, 0))

        assertEquals(LocalTime.of(19, 0), repository.observe(habitId).first()?.localTime)
        repository.remove(habitId)
        assertEquals(null, repository.observe(habitId).first())
    }
}
