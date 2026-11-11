package dev.dhev.nines.ui.habits

import dev.dhev.nines.FakeEntryDao
import dev.dhev.nines.FakeHabitDao
import dev.dhev.nines.FakeReminderDao
import dev.dhev.nines.FakeScheduleDao
import dev.dhev.nines.TestClock
import dev.dhev.nines.data.DemoData
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.EntryDao
import dev.dhev.nines.data.local.HabitDao
import dev.dhev.nines.data.local.ScheduleVersionDao
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import dev.dhev.nines.reminders.HabitReminders
import dev.dhev.nines.reminders.RecordingAlarms
import dev.dhev.nines.reminders.RecordingNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelContractTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `homepage selected date changes and return to today use matching entry`() = runTest(mainDispatcherRule.dispatcher) {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitsViewModel(fixture.repository, fixture.reminders, computeDispatcher = mainDispatcherRule.dispatcher)

        assertTrue(
            viewModel.uiState
                .first { !it.isLoading }
                .habits
                .single()
                .completed,
        )

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
    fun `demo data is unavailable by default and the seed action is a no-op`() = runTest(mainDispatcherRule.dispatcher) {
        val fixture = Fixture(LocalDate.now())
        val viewModel = HabitsViewModel(fixture.repository, fixture.reminders, computeDispatcher = mainDispatcherRule.dispatcher)

        assertFalse(viewModel.uiState.first { !it.isLoading }.demoDataAvailable)
        viewModel.onAction(HabitsAction.SeedDemoData)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.first { !it.isLoading }.habits.size)
    }

    @Test
    fun `a bound demo data source is advertised and seeded on request`() = runTest(mainDispatcherRule.dispatcher) {
        val demoData = object : DemoData {
            override val isAvailable = true
            var seeded = 0

            override suspend fun seed() {
                seeded++
            }
        }
        val fixture = Fixture(LocalDate.now())
        val viewModel = HabitsViewModel(
            fixture.repository,
            fixture.reminders,
            demoData = demoData,
            computeDispatcher = mainDispatcherRule.dispatcher,
        )

        assertTrue(viewModel.uiState.first { !it.isLoading }.demoDataAvailable)
        viewModel.onAction(HabitsAction.SeedDemoData)
        advanceUntilIdle()

        assertEquals(1, demoData.seeded)
    }

    @Test
    fun `homepage toggle writes to the selected date`() = runTest(mainDispatcherRule.dispatcher) {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitsViewModel(fixture.repository, fixture.reminders, computeDispatcher = mainDispatcherRule.dispatcher)
        viewModel.uiState.first { !it.isLoading }

        val historicalDate = today.minusDays(1)
        viewModel.onAction(HabitsAction.SelectDate(historicalDate))
        advanceUntilIdle()
        viewModel.uiState.first { it.selectedDate == historicalDate }
        viewModel.onAction(HabitsAction.ToggleHabit(fixture.habit.id))
        advanceUntilIdle()

        val updated =
            viewModel.uiState.first {
                it.selectedDate == historicalDate && it.habits.single().completed
            }
        assertTrue(updated.habits.single().completed)
        assertTrue(fixture.entries.value.any { it.date == historicalDate && it.state == EntryState.COMPLETED })
    }

    @Test
    fun `homepage numeric logging writes value to the selected date`() = runTest(mainDispatcherRule.dispatcher) {
        val today = LocalDate.now()
        val fixture = Fixture(today, HabitType.NUMERICAL)
        val viewModel = HabitsViewModel(fixture.repository, fixture.reminders, computeDispatcher = mainDispatcherRule.dispatcher)
        val historicalDate = today.minusDays(1)

        viewModel.onAction(HabitsAction.SelectDate(historicalDate))
        advanceUntilIdle()
        viewModel.onAction(HabitsAction.SetNumericValue(fixture.habit.id, 3.5))
        advanceUntilIdle()

        val updated =
            viewModel.uiState.first {
                it.selectedDate == historicalDate && it.habits.single().currentValue == 3.5
            }
        assertEquals(3.5, updated.habits.single().currentValue ?: 0.0, 0.0)
        assertEquals(
            3.5,
            fixture.entries.value
                .first { it.date == historicalDate }
                .numericValue ?: 0.0,
            0.0,
        )
    }

    @Test
    fun `homepage clear and undo numeric entry use the action date`() = runTest(mainDispatcherRule.dispatcher) {
        val today = LocalDate.now()
        val fixture = Fixture(today, HabitType.NUMERICAL)
        val viewModel = HabitsViewModel(fixture.repository, fixture.reminders, computeDispatcher = mainDispatcherRule.dispatcher)
        val historicalDate = today.minusDays(1)

        viewModel.onAction(HabitsAction.SetNumericValue(fixture.habit.id, 3.5))
        advanceUntilIdle()
        viewModel.onAction(HabitsAction.SelectDate(historicalDate))
        advanceUntilIdle()
        viewModel.onAction(HabitsAction.SetNumericValue(fixture.habit.id, 2.0))
        advanceUntilIdle()

        viewModel.onAction(HabitsAction.ClearNumericEntry(fixture.habit.id, historicalDate, 2.0))
        advanceUntilIdle()
        assertFalse(fixture.entries.value.any { it.date == historicalDate })
        assertEquals(historicalDate, viewModel.uiState.first { it.selectedDate == historicalDate }.selectedDate)

        viewModel.onAction(HabitsAction.UndoClearNumericEntry(fixture.habit.id, historicalDate, 2.0))
        advanceUntilIdle()
        assertEquals(
            2.0,
            fixture.entries.value
                .first { it.date == historicalDate }
                .numericValue ?: 0.0,
            0.0,
        )
    }

    @Test
    fun `detail selected date changes and missing habit resolves without loading`() = runTest(mainDispatcherRule.dispatcher) {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitDetailViewModel(fixture.habit.id, fixture.repository, computeDispatcher = mainDispatcherRule.dispatcher)
        viewModel.uiState.first { !it.isLoading }

        val historicalDate = today.minusDays(1)
        viewModel.onAction(HabitDetailAction.SelectDate(historicalDate))
        advanceUntilIdle()
        assertEquals(historicalDate, viewModel.uiState.first { it.selectedDate == historicalDate }.selectedDate)
        viewModel.onAction(HabitDetailAction.SelectDate(today))
        advanceUntilIdle()
        assertEquals(today, viewModel.uiState.first { it.selectedDate == today }.selectedDate)

        val missing = HabitDetailViewModel(999L, fixture.repository, computeDispatcher = mainDispatcherRule.dispatcher)
        val missingState = missing.uiState.first { !it.isLoading }
        assertEquals(999L, missingState.habitId)
        assertEquals("", missingState.name)
    }

    @Test
    fun `homepage today and the selected date roll over at midnight`() = runTest(mainDispatcherRule.dispatcher) {
        val clock = TestClock(testScheduler, Instant.parse("2026-03-18T22:00:00Z"))
        val fixture = Fixture(LocalDate.of(2026, 3, 18), clock = clock)
        val viewModel = HabitsViewModel(
            fixture.repository,
            fixture.reminders,
            clock = clock,
            computeDispatcher = mainDispatcherRule.dispatcher,
        )
        val collector = launch { viewModel.uiState.collect {} }
        val before = viewModel.uiState.first { !it.isLoading }
        assertEquals(LocalDate.of(2026, 3, 18), before.today)
        assertEquals(LocalDate.of(2026, 3, 18), before.selectedDate)

        advanceTimeBy(3.hours)
        runCurrent()
        val after = viewModel.uiState.first { it.today == LocalDate.of(2026, 3, 19) }

        assertEquals("a user following today keeps following it", LocalDate.of(2026, 3, 19), after.selectedDate)
        collector.cancel()
        advanceUntilIdle()
    }

    @Test
    fun `homepage keeps a browsed past date across midnight`() = runTest(mainDispatcherRule.dispatcher) {
        val clock = TestClock(testScheduler, Instant.parse("2026-03-18T22:00:00Z"))
        val fixture = Fixture(LocalDate.of(2026, 3, 18), clock = clock)
        val viewModel = HabitsViewModel(
            fixture.repository,
            fixture.reminders,
            clock = clock,
            computeDispatcher = mainDispatcherRule.dispatcher,
        )
        val collector = launch { viewModel.uiState.collect {} }
        viewModel.uiState.first { !it.isLoading }
        viewModel.onAction(HabitsAction.SelectDate(LocalDate.of(2026, 3, 16)))
        runCurrent()
        viewModel.uiState.first { it.selectedDate == LocalDate.of(2026, 3, 16) }

        advanceTimeBy(3.hours)
        runCurrent()
        val after = viewModel.uiState.first { it.today == LocalDate.of(2026, 3, 19) }

        assertEquals(LocalDate.of(2026, 3, 16), after.selectedDate)
        collector.cancel()
        advanceUntilIdle()
    }

    @Test
    fun `detail today rolls over at midnight`() = runTest(mainDispatcherRule.dispatcher) {
        val clock = TestClock(testScheduler, Instant.parse("2026-03-18T22:00:00Z"))
        val fixture = Fixture(LocalDate.of(2026, 3, 18), clock = clock)
        val viewModel = HabitDetailViewModel(fixture.habit.id, fixture.repository, clock, mainDispatcherRule.dispatcher)
        val collector = launch { viewModel.uiState.collect {} }
        assertEquals(LocalDate.of(2026, 3, 18), viewModel.uiState.first { !it.isLoading }.today)

        advanceTimeBy(3.hours)
        runCurrent()

        assertEquals(LocalDate.of(2026, 3, 19), viewModel.uiState.first { it.today == LocalDate.of(2026, 3, 19) }.today)
        collector.cancel()
        advanceUntilIdle()
    }

    @Test
    fun `detail logging action uses selected date`() = runTest(mainDispatcherRule.dispatcher) {
        val today = LocalDate.now()
        val fixture = Fixture(today)
        val viewModel = HabitDetailViewModel(fixture.habit.id, fixture.repository, computeDispatcher = mainDispatcherRule.dispatcher)
        val historicalDate = today.minusDays(1)
        viewModel.onAction(HabitDetailAction.SelectDate(historicalDate))
        advanceUntilIdle()
        viewModel.uiState.first { it.selectedDate == historicalDate }
        viewModel.onAction(HabitDetailAction.Toggle(historicalDate))
        advanceUntilIdle()

        assertTrue(fixture.entries.value.any { it.date == historicalDate && it.state == EntryState.COMPLETED })
        assertTrue(viewModel.uiState.first { it.selectedDate == historicalDate && it.completed }.completed)
    }

    private class Fixture(
        today: LocalDate,
        type: HabitType = HabitType.BOOLEAN,
        clock: Clock = Clock.systemDefaultZone(),
    ) {
        val habit =
            HabitEntity(
                id = 1L,
                name = "Test habit",
                type = type,
                color = 0xFF000000.toInt(),
                iconKey = "check",
                position = 0,
                createdAt = today.minusDays(7).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
            )
        val entries =
            MutableStateFlow(
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
        private val schedules =
            MutableStateFlow(
                listOf(
                    ScheduleVersionEntity(
                        id = 1L,
                        habitId = habit.id,
                        effectiveFromDate = habit.createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        targetNumerator = 1,
                        targetDenominator = 1,
                        targetValue = if (type == HabitType.NUMERICAL) 5.0 else null,
                    ),
                ),
            )
        val repository = HabitRepository(FakeHabitDao(habits), FakeScheduleDao(schedules), FakeEntryDao(entries), clock)
        val reminders = HabitReminders(ReminderRepository(FakeReminderDao()), repository, RecordingAlarms(), RecordingNotifications())
    }

    class MainDispatcherRule : TestWatcher() {
        private val scheduler = TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)

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
