package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.FrequencyTargetKind
import dev.dhev.nines.scoring.HabitTimelineInput
import dev.dhev.nines.scoring.ScheduleTarget
import dev.dhev.nines.scoring.TimelineEntry
import dev.dhev.nines.scoring.TimelineEntryState
import dev.dhev.nines.scoring.TimelineHabitKind
import dev.dhev.nines.scoring.TimelineSchedule
import dev.dhev.nines.scoring.buildHabitTimeline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class HabitDetailAdapterTest {
    private val created = LocalDate.of(2026, 3, 1)
    private val today = LocalDate.of(2026, 3, 18)

    private fun habit(type: HabitType = HabitType.BOOLEAN) = HabitEntity(
        id = 3,
        name = "Stretch",
        type = type,
        color = 0xFF112233.toInt(),
        iconKey = "fitness",
        position = 0,
        createdAt = created.atStartOfDay(ZoneOffset.UTC).toInstant(),
    )

    private fun snapshot(
        type: HabitType = HabitType.BOOLEAN,
        target: ScheduleTarget = ScheduleTarget(1, 1),
        targetValue: Double? = null,
        targetKind: FrequencyTargetKind? = null,
        completedDaysAgo: List<Long> = emptyList(),
    ): HabitRepository.HabitTimelineSnapshot {
        val entries = completedDaysAgo.map {
            TimelineEntry(
                date = today.minusDays(it),
                state = TimelineEntryState.COMPLETED,
                numericValue = targetValue.takeIf { type == HabitType.NUMERICAL },
            )
        }
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = if (type == HabitType.NUMERICAL) TimelineHabitKind.NUMERICAL else TimelineHabitKind.BOOLEAN,
                createdDate = created,
                selectedDate = today,
                entries = entries,
                schedules = listOf(TimelineSchedule(created, target, targetValue, targetKind)),
            ),
        )
        return HabitRepository.HabitTimelineSnapshot(habit(type), timeline, entries)
    }

    private fun adapt(snapshot: HabitRepository.HabitTimelineSnapshot?) = habitDetailUiState(3, snapshot, today, ZoneOffset.UTC)

    @Test
    fun `a missing habit becomes a loaded empty state`() {
        val state = adapt(null)

        assertFalse(state.isLoading)
        assertEquals(3L, state.habitId)
        assertEquals(today, state.today)
    }

    @Test
    fun `a daily habit has no cycle progress`() {
        val state = adapt(snapshot(completedDaysAgo = listOf(0)))

        assertTrue(state.completed)
        assertNull(state.cycleCompletions)
        assertEquals(1, state.cycleTarget)
        assertEquals(1, state.cycleWindowDays)
    }

    @Test
    fun `a weekly boolean habit reports completions inside its window`() {
        val state = adapt(snapshot(target = ScheduleTarget(3, 7), completedDaysAgo = listOf(0, 2)))

        assertEquals(2, state.cycleCompletions)
        assertEquals(3, state.cycleTarget)
        assertEquals(7, state.cycleWindowDays)
    }

    @Test
    fun `a numeric habit carries its target and never cycle completions`() {
        val state = adapt(
            snapshot(
                type = HabitType.NUMERICAL,
                target = ScheduleTarget(1, 7),
                targetValue = 20.0,
                targetKind = FrequencyTargetKind.AT_MOST,
                completedDaysAgo = listOf(0),
            ),
        )

        assertEquals(20.0, state.targetValue!!, 0.0)
        assertEquals(TargetKind.AT_MOST, state.targetKind)
        assertNull(state.cycleCompletions)
    }

    @Test
    fun `being on the longest streak says so`() {
        val state = adapt(snapshot(completedDaysAgo = listOf(0, 1, 2)))

        assertEquals(3, state.currentStreak)
        assertEquals("You're on your longest streak yet", state.recordProgressLabel)
    }

    @Test
    fun `history runs from the habit's creation up to the selected date`() {
        val state = adapt(snapshot(completedDaysAgo = listOf(0)))

        assertEquals(created, state.history.first().date)
        assertEquals(today, state.history.last().date)
        assertTrue(state.history.last().isToday)
    }
}
