package dev.dhev.nines.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitObservationTest {
    private val created = LocalDate.of(2026, 8, 1)
    private val selected = LocalDate.of(2026, 8, 5)
    private val asOf = LocalDate.of(2026, 8, 10)

    @Test
    fun `observation clips entries and calendar to creation and as-of dates`() {
        val observation = buildHabitObservation(
            HabitObservationInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                selectedDate = selected,
                asOfDate = asOf,
                historyStartDate = created.minusDays(3),
                entries = listOf(
                    entry(created.minusDays(1)),
                    entry(created),
                    entry(selected),
                    entry(asOf),
                    entry(asOf.plusDays(1)),
                ),
                schedules = listOf(
                    TimelineSchedule(created, ScheduleTarget.DAILY),
                ),
            ),
        )

        assertEquals(listOf(created, selected, asOf), observation.entriesThroughAsOfDate.map { it.date })
        assertEquals(created, observation.calendarDays.first().date)
        assertEquals(selected, observation.calendarDays.last().date)
        assertEquals(selected, observation.timeline.selectedDate)
        assertTrue(observation.timeline.selectedEntry != null)
    }

    @Test
    fun `future selected date is capped by explicit as-of date`() {
        val observation = buildHabitObservation(
            HabitObservationInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                selectedDate = asOf.plusDays(5),
                asOfDate = asOf,
                historyStartDate = created,
                entries = listOf(entry(asOf.plusDays(1))),
                schedules = listOf(TimelineSchedule(created, ScheduleTarget.DAILY)),
            ),
        )

        assertEquals(asOf, observation.calendarDays.last().date)
        assertFalse(observation.entriesThroughAsOfDate.any { it.date.isAfter(asOf) })
    }

    private fun entry(date: LocalDate) = TimelineEntry(date, TimelineEntryState.COMPLETED)
}
