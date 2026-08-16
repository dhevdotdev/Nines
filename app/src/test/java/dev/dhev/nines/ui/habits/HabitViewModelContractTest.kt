package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.EntryDao
import dev.dhev.nines.data.local.HabitDao
import dev.dhev.nines.data.local.ScheduleVersionDao
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelContractTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `homepage selected date changes and return to today use matching entry`() = runTest {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitsViewModel(fixture.repository)

        assertTrue(viewModel.uiState.first { !it.isLoading }.habits.single().completed)

        viewModel.onAction(HabitsAction.SelectDate(today.minusDays(1)))
        advanceUntilIdle()
        val historical = viewModel.uiState.first { it.selectedDate == today.minusDays(1) }
        assertFalse(historical.habits.single().completed)

        viewModel.onAction(HabitsAction.SelectDate(today))
        advanceUntilIdle()
        val current = viewModel.uiState.first { it.selectedDate == today && it.habits.single().completed }
        assertEquals(today, current.selectedDate)
    }

    @Test
    fun `homepage toggle writes to the selected date`() = runTest {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitsViewModel(fixture.repository)
        viewModel.uiState.first { !it.isLoading }

        val historicalDate = today.minusDays(1)
        viewModel.onAction(HabitsAction.SelectDate(historicalDate))
        advanceUntilIdle()
        viewModel.uiState.first { it.selectedDate == historicalDate }
        viewModel.onAction(HabitsAction.ToggleHabit(fixture.habit.id))
        advanceUntilIdle()

        val updated = viewModel.uiState.first {
            it.selectedDate == historicalDate && it.habits.single().completed
        }
        assertTrue(updated.habits.single().completed)
        assertTrue(fixture.entries.value.any { it.date == historicalDate && it.state == EntryState.COMPLETED })
    }

    @Test
    fun `detail selected date changes and missing habit resolves without loading`() = runTest {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitDetailViewModel(fixture.habit.id, fixture.repository)
        viewModel.uiState.first { !it.isLoading }

        val historicalDate = today.minusDays(1)
        viewModel.onAction(HabitDetailAction.SelectDate(historicalDate))
        advanceUntilIdle()
        assertEquals(historicalDate, viewModel.uiState.first { it.selectedDate == historicalDate }.selectedDate)
        viewModel.onAction(HabitDetailAction.SelectDate(today))
        advanceUntilIdle()
        assertEquals(today, viewModel.uiState.first { it.selectedDate == today }.selectedDate)

        val missing = HabitDetailViewModel(999L, fixture.repository)
        val missingState = missing.uiState.first { !it.isLoading }
        assertEquals(999L, missingState.habitId)
        assertEquals("", missingState.name)
    }

    @Test
    fun `detail logging action uses selected date`() = runTest {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitDetailViewModel(fixture.habit.id, fixture.repository)
        val historicalDate = today.minusDays(1)
        viewModel.onAction(HabitDetailAction.SelectDate(historicalDate))
        advanceUntilIdle()
        viewModel.uiState.first { it.selectedDate == historicalDate }
        viewModel.onAction(HabitDetailAction.Toggle(historicalDate))
        advanceUntilIdle()

        assertTrue(fixture.entries.value.any { it.date == historicalDate && it.state == EntryState.COMPLETED })
        assertTrue(viewModel.uiState.first { it.selectedDate == historicalDate && it.completed }.completed)
    }

    private class Fixture(today: LocalDate) {
        val habit = HabitEntity(
            id = 1L,
            name = "Test habit",
            type = HabitType.BOOLEAN,
            color = 0xFF000000.toInt(),
            iconKey = "check",
            position = 0,
            createdAt = today.minusDays(7).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
        )
        val entries = MutableStateFlow(
            listOf(
                EntryEntity(
                    habitId = habit.id,
                    date = today,
                    state = EntryState.COMPLETED,
                    tzOffsetMinutes = 0,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            ),
        )
        private val habits = MutableStateFlow<HabitEntity?>(habit)
        private val schedules = MutableStateFlow(
            listOf(
                ScheduleVersionEntity(
                    id = 1L,
                    habitId = habit.id,
                    effectiveFromDate = habit.createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    targetNumerator = 1,
                    targetDenominator = 1,
                ),
            ),
        )
        val repository = HabitRepository(FakeHabitDao(habits), FakeScheduleDao(schedules), FakeEntryDao(entries))
    }

    private class FakeHabitDao(private val habit: MutableStateFlow<HabitEntity?>) : HabitDao {
        override fun observeActiveHabits(): Flow<List<HabitEntity>> = habit.value?.let { MutableStateFlow(listOf(it)) } ?: MutableStateFlow(emptyList())
        override fun observeArchivedHabits(): Flow<List<HabitEntity>> = MutableStateFlow(emptyList())
        override suspend fun getById(habitId: Long): HabitEntity? = habit.value?.takeIf { it.id == habitId }
        override fun observeById(habitId: Long): Flow<HabitEntity?> = if (habit.value?.id == habitId) habit else flowOf(null)
        override suspend fun insert(habit: HabitEntity): Long = habit.id
        override suspend fun update(habit: HabitEntity) { this.habit.value = habit }
        override suspend fun nextPosition(): Int = 0
        override suspend fun updatePosition(habitId: Long, position: Int) = Unit
    }

    private class FakeScheduleDao(private val schedules: MutableStateFlow<List<ScheduleVersionEntity>>) : ScheduleVersionDao {
        override suspend fun insert(scheduleVersion: ScheduleVersionEntity): Long {
            schedules.value = schedules.value + scheduleVersion
            return scheduleVersion.id
        }
        override fun observeVersions(habitId: Long): Flow<List<ScheduleVersionEntity>> =
            schedules.map { values -> values.filter { it.habitId == habitId } }
        override suspend fun getVersions(habitId: Long): List<ScheduleVersionEntity> = schedules.value.filter { it.habitId == habitId }
        override suspend fun versionEffectiveOn(habitId: Long, date: LocalDate): ScheduleVersionEntity? =
            schedules.value.filter { it.habitId == habitId && !it.effectiveFromDate.isAfter(date) }
                .maxWithOrNull(compareBy<ScheduleVersionEntity> { it.effectiveFromDate }.thenBy { it.id })
    }

    private class FakeEntryDao(val entries: MutableStateFlow<List<EntryEntity>>) : EntryDao {
        override suspend fun upsert(entry: EntryEntity) {
            entries.value = entries.value.filterNot { it.habitId == entry.habitId && it.date == entry.date } + entry
        }
        override suspend fun delete(entry: EntryEntity) { entries.value = entries.value - entry }
        override fun observeEntries(habitId: Long): Flow<List<EntryEntity>> =
            entries.map { values -> values.filter { it.habitId == habitId } }
        override suspend fun entriesBetween(habitId: Long, from: LocalDate, to: LocalDate): List<EntryEntity> =
            entries.value.filter { it.habitId == habitId && !it.date.isBefore(from) && !it.date.isAfter(to) }
        override suspend fun entryOn(habitId: Long, date: LocalDate): EntryEntity? = entries.value.firstOrNull { it.habitId == habitId && it.date == date }
    }

    class MainDispatcherRule : TestWatcher() {
        private val scheduler = TestCoroutineScheduler()
        private val dispatcher = StandardTestDispatcher(scheduler)

        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            scheduler.advanceTimeBy(5_000)
            scheduler.runCurrent()
            Dispatchers.resetMain()
        }
    }
}
