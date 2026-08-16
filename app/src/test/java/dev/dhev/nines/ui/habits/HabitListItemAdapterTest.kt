package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.scoring.TimelineEntry
import dev.dhev.nines.scoring.TimelineHabitKind
import dev.dhev.nines.scoring.TimelineSchedule
import dev.dhev.nines.scoring.HabitTimeline
import dev.dhev.nines.scoring.HabitTimelineInput
import dev.dhev.nines.scoring.buildHabitTimeline
import dev.dhev.nines.scoring.ScheduleTarget
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitListItemAdapterTest {
    private val createdDate = LocalDate.of(2026, 8, 1)
    private val selectedDate = LocalDate.of(2026, 8, 8)
    private val habit = HabitEntity(
        id = 7,
        name = "Read",
        type = HabitType.NUMERICAL,
        unit = "pages",
        color = 0xFF00AA88.toInt(),
        iconKey = "book",
        backgroundStyleKey = "waves",
        position = 0,
        createdAt = createdDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
    )

    @Test
    fun `invisible habit is omitted from homepage`() {
        val observation = buildObservation(selectedDate = createdDate.minusDays(1))

        assertNull(habitListItemFromSnapshot(snapshot(observation)))
    }

    @Test
    fun `adapter preserves selected entry and numeric target`() {
        val observation = buildObservation(
            entries = listOf(
                TimelineEntry(
                    date = selectedDate,
                    state = dev.dhev.nines.scoring.TimelineEntryState.COMPLETED,
                    numericValue = 12.0,
                ),
            ),
        )

        val item = habitListItemFromSnapshot(snapshot(observation))

        requireNotNull(item)
        assertEquals(7L, item.id)
        assertEquals(true, item.completed)
        assertEquals(12.0, item.currentValue!!, 0.0)
        assertEquals(20.0, item.targetValue!!, 0.0)
        assertEquals("pages", item.unit)
        assertEquals(1, item.targetNumerator)
        assertEquals(1, item.targetDenominator)
    }

    private fun snapshot(timeline: HabitTimeline) = HabitRepository.HabitTimelineSnapshot(
        habit = habit,
        timeline = timeline,
        entriesThroughAsOfDate = emptyList(),
    )

    private fun buildObservation(
        selectedDate: LocalDate = this.selectedDate,
        entries: List<TimelineEntry> = emptyList(),
    ): HabitTimeline = buildHabitTimeline(
        HabitTimelineInput(
            kind = TimelineHabitKind.NUMERICAL,
            createdDate = createdDate,
            selectedDate = selectedDate,
            entries = entries,
            schedules = listOf(
                TimelineSchedule(
                    effectiveFromDate = createdDate,
                    target = ScheduleTarget(1, 1),
                    targetValue = 20.0,
                ),
            ),
        ),
    )
}
