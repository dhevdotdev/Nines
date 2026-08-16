package dev.dhev.nines.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dhev.nines.data.local.NinesDatabase
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import dev.dhev.nines.data.local.entity.TargetKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HabitRepositoryRoomTest {
    private lateinit var database: NinesDatabase
    private lateinit var repository: HabitRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NinesDatabase::class.java).build()
        repository = HabitRepository(database.habitDao(), database.scheduleVersionDao(), database.entryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createHabitPersistsPositionAndInitialSchedule() = runBlocking {
        val createdAt = LocalDate.of(2026, 8, 1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()

        val habitId = repository.createHabit(
            name = "Read",
            type = HabitType.BOOLEAN,
            color = 0xFF000000.toInt(),
            iconKey = "book",
            createdAt = createdAt,
            scheduleEffectiveFrom = createdAt.atZone(java.time.ZoneOffset.UTC).toLocalDate(),
            targetNumerator = 2,
            targetDenominator = 7,
        )

        val habit = database.habitDao().getById(habitId)
        val schedule = database.scheduleVersionDao().getVersions(habitId)

        assertNotNull(habit)
        assertEquals(0, habit?.position)
        assertEquals("Read", habit?.name)
        assertEquals(listOf(2), schedule.map { it.targetNumerator })
        assertEquals(listOf(7), schedule.map { it.targetDenominator })
    }

    @Test
    fun archiveAndRestoreChangeActiveOrderingWithoutDeletingHistory() = runBlocking {
        val firstId = createHabit("First")
        val secondId = createHabit("Second")
        val date = LocalDate.of(2026, 8, 10)
        database.entryDao().upsert(completedEntry(firstId, date))

        repository.archiveHabit(firstId)
        assertEquals(listOf(secondId), database.habitDao().observeActiveHabits().first().map { it.id })
        assertTrue(database.habitDao().observeArchivedHabits().first().any { it.id == firstId })

        repository.restoreHabit(firstId)
        assertEquals(listOf(secondId, firstId), database.habitDao().observeActiveHabits().first().map { it.id })
        assertEquals(EntryState.COMPLETED, database.entryDao().entryOn(firstId, date)?.state)
    }

    @Test
    fun numericLoggingUsesScheduleAndTimelineObservesUpdatedEntry() = runBlocking {
        val habitId = repository.createHabit(
            name = "Water",
            type = HabitType.NUMERICAL,
            color = 0xFF000000.toInt(),
            iconKey = "water",
            targetValue = 2.0,
            targetKind = TargetKind.AT_LEAST,
        )
        val date = LocalDate.of(2026, 8, 16)

        repository.setNumericEntry(habitId, date, 2.5)

        val entry = database.entryDao().entryOn(habitId, date)
        val timeline = repository.observeHabitTimelineData(habitId).first()

        assertEquals(2.5, entry?.numericValue ?: 0.0, 0.0)
        assertEquals(EntryState.COMPLETED, entry?.state)
        assertEquals(2.5, timeline.entries.single().numericValue ?: 0.0, 0.0)
    }

    @Test
    fun scheduleEffectiveOnUsesLatestVersionOnSameDate() = runBlocking {
        val habitId = createHabit("Schedule")
        val effectiveDate = LocalDate.of(2026, 8, 16)
        database.scheduleVersionDao().insert(
            ScheduleVersionEntity(habitId = habitId, effectiveFromDate = effectiveDate, targetNumerator = 2, targetDenominator = 7),
        )
        database.scheduleVersionDao().insert(
            ScheduleVersionEntity(habitId = habitId, effectiveFromDate = effectiveDate, targetNumerator = 3, targetDenominator = 7),
        )

        val selected = database.scheduleVersionDao().versionEffectiveOn(habitId, effectiveDate)

        assertEquals(3, selected?.targetNumerator)
        assertEquals(7, selected?.targetDenominator)
    }

    @Test
    fun scoreSeriesAppliesHistoricalScheduleBoundary() = runBlocking {
        val habitId = createHabit("Score")
        val boundary = LocalDate.of(2026, 8, 12)
        database.scheduleVersionDao().insert(
            ScheduleVersionEntity(habitId = habitId, effectiveFromDate = boundary, targetNumerator = 2, targetDenominator = 3),
        )
        database.entryDao().upsert(completedEntry(habitId, boundary))
        database.entryDao().upsert(completedEntry(habitId, boundary.plusDays(1)))

        val points = repository.scoreSeries(habitId, boundary.minusDays(1), boundary.plusDays(1))

        assertEquals(3, points.size)
        assertEquals(boundary.minusDays(1), points[0].date)
        assertEquals(0.0, points[0].score, 0.0)
        assertTrue(points[1].score > 0.0)
        assertTrue(points[2].score > 0.0)
    }

    private suspend fun createHabit(name: String): Long = repository.createHabit(
        name = name,
        type = HabitType.BOOLEAN,
        color = 0xFF000000.toInt(),
        iconKey = "check",
        createdAt = Instant.parse("2026-08-01T00:00:00Z"),
        scheduleEffectiveFrom = LocalDate.of(2026, 8, 1),
    )

    private fun completedEntry(habitId: Long, date: LocalDate) = EntryEntity(
        habitId = habitId,
        date = date,
        state = EntryState.COMPLETED,
        createdAt = Instant.parse("2026-08-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-08-01T00:00:00Z"),
        tzOffsetMinutes = 0,
    )
}
