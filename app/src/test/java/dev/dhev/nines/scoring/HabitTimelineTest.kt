package dev.dhev.nines.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.pow

class HabitTimelineTest {

    private val selectedDate = LocalDate.of(2026, 8, 16)

    @Test
    fun `timeline projects selected date without leaking future entries`() {
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = selectedDate.minusDays(15),
                selectedDate = selectedDate,
                entries = listOf(
                    TimelineEntry(selectedDate, TimelineEntryState.COMPLETED),
                    TimelineEntry(selectedDate.minusDays(2), TimelineEntryState.COMPLETED),
                    TimelineEntry(selectedDate.minusDays(1), TimelineEntryState.SKIPPED),
                    TimelineEntry(selectedDate.plusDays(1), TimelineEntryState.COMPLETED),
                ),
                schedules = listOf(
                    TimelineSchedule(
                        effectiveFromDate = selectedDate.minusDays(15),
                        target = ScheduleTarget(numerator = 3, denominator = 7),
                    ),
                ),
            ),
        )

        assertTrue(timeline.isVisible)
        assertEquals(TimelineEntryState.COMPLETED, timeline.selectedEntry?.state)
        assertEquals(2.0, timeline.frequencyProgress?.achieved ?: -1.0, 0.0)
        assertEquals(1, timeline.currentStreak)
        assertEquals(1, timeline.currentGap)
        assertNotNull(timeline.selectedSchedule)
        assertTrue(timeline.score > 0.0)
    }

    @Test
    fun `timeline selects the schedule effective on the selected date`() {
        val olderDate = selectedDate.minusDays(3)
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = selectedDate.minusDays(10),
                selectedDate = olderDate,
                entries = emptyList(),
                schedules = listOf(
                    TimelineSchedule(
                        effectiveFromDate = selectedDate.minusDays(10),
                        target = ScheduleTarget.DAILY,
                    ),
                    TimelineSchedule(
                        effectiveFromDate = selectedDate.minusDays(1),
                        target = ScheduleTarget(numerator = 3, denominator = 7),
                    ),
                ),
            ),
        )

        assertEquals(ScheduleTarget.DAILY, timeline.selectedSchedule?.target)

        val laterTimeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = selectedDate.minusDays(10),
                selectedDate = selectedDate,
                entries = emptyList(),
                schedules = listOf(
                    TimelineSchedule(selectedDate.minusDays(10), ScheduleTarget.DAILY),
                    TimelineSchedule(selectedDate.minusDays(1), ScheduleTarget(numerator = 3, denominator = 7)),
                ),
            ),
        )
        assertEquals(ScheduleTarget(numerator = 3, denominator = 7), laterTimeline.selectedSchedule?.target)
    }

    @Test
    fun `timeline selects the newest version when schedules share an effective date`() {
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = selectedDate,
                selectedDate = selectedDate,
                entries = emptyList(),
                schedules = listOf(
                    TimelineSchedule(selectedDate, ScheduleTarget.DAILY, versionOrder = 10),
                    TimelineSchedule(selectedDate, ScheduleTarget(numerator = 3, denominator = 7), versionOrder = 11),
                ),
            ),
        )

        assertEquals(ScheduleTarget(numerator = 3, denominator = 7), timeline.selectedSchedule?.target)
    }

    @Test
    fun `empty timeline is visible but has no derived completion state`() {
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = selectedDate,
                selectedDate = selectedDate,
                entries = emptyList(),
                schedules = listOf(
                    TimelineSchedule(selectedDate, ScheduleTarget.DAILY),
                ),
            ),
        )

        assertTrue(timeline.isVisible)
        assertEquals(0.0, timeline.frequencyProgress?.achieved ?: -1.0, 0.0)
        assertEquals(0, timeline.currentStreak)
        assertEquals(1, timeline.currentGap)
        assertEquals(0.0, timeline.score, 0.0)
    }

    @Test
    fun `timeline hides a Habit before its creation date`() {
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = selectedDate,
                selectedDate = selectedDate.minusDays(1),
                entries = emptyList(),
                schedules = emptyList(),
            ),
        )

        assertEquals(false, timeline.isVisible)
        assertEquals(null, timeline.frequencyProgress)
    }

    @Test
    fun `numeric timeline preserves at least and at most target semantics`() {
        val atLeast = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.NUMERICAL,
                createdDate = selectedDate,
                selectedDate = selectedDate,
                entries = listOf(TimelineEntry(selectedDate, TimelineEntryState.IN_PROGRESS, 8.0)),
                schedules = listOf(
                    TimelineSchedule(
                        effectiveFromDate = selectedDate,
                        target = ScheduleTarget.DAILY,
                        targetValue = 5.0,
                        targetKind = FrequencyTargetKind.AT_LEAST,
                    ),
                ),
            ),
        )
        val atMost = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.NUMERICAL,
                createdDate = selectedDate,
                selectedDate = selectedDate,
                entries = listOf(TimelineEntry(selectedDate, TimelineEntryState.IN_PROGRESS, 4.0)),
                schedules = listOf(
                    TimelineSchedule(
                        effectiveFromDate = selectedDate,
                        target = ScheduleTarget.DAILY,
                        targetValue = 5.0,
                        targetKind = FrequencyTargetKind.AT_MOST,
                    ),
                ),
            ),
        )

        assertTrue(atLeast.frequencyProgress!!.isMet)
        assertEquals(FrequencyTargetKind.AT_LEAST, atLeast.frequencyProgress.targetKind)
        assertTrue(atMost.frequencyProgress!!.isMet)
        assertEquals(FrequencyTargetKind.AT_MOST, atMost.frequencyProgress.targetKind)
    }

    @Test
    fun `numeric timeline treats an empty window as unmet at least and met at most`() {
        val atLeast = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.NUMERICAL,
                createdDate = selectedDate,
                selectedDate = selectedDate,
                entries = emptyList(),
                schedules = listOf(
                    TimelineSchedule(
                        selectedDate,
                        ScheduleTarget.DAILY,
                        targetValue = 5.0,
                        targetKind = FrequencyTargetKind.AT_LEAST,
                    ),
                ),
            ),
        )
        val atMost = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.NUMERICAL,
                createdDate = selectedDate,
                selectedDate = selectedDate,
                entries = emptyList(),
                schedules = listOf(
                    TimelineSchedule(
                        selectedDate,
                        ScheduleTarget.DAILY,
                        targetValue = 5.0,
                        targetKind = FrequencyTargetKind.AT_MOST,
                    ),
                ),
            ),
        )

        assertEquals(false, atLeast.frequencyProgress?.isMet)
        assertEquals(true, atMost.frequencyProgress?.isMet)
    }

    @Test
    fun `numeric timeline reports over target values as unmet at most`() {
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.NUMERICAL,
                createdDate = selectedDate,
                selectedDate = selectedDate,
                entries = listOf(TimelineEntry(selectedDate, TimelineEntryState.IN_PROGRESS, 8.0)),
                schedules = listOf(
                    TimelineSchedule(
                        selectedDate,
                        ScheduleTarget.DAILY,
                        targetValue = 5.0,
                        targetKind = FrequencyTargetKind.AT_MOST,
                    ),
                ),
            ),
        )

        assertEquals(8.0, timeline.frequencyProgress?.achieved ?: -1.0, 0.0)
        assertEquals(false, timeline.frequencyProgress?.isMet)
    }

    @Test
    fun `incomplete and skipped entries do not inflate completion progress`() {
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = selectedDate.minusDays(2),
                selectedDate = selectedDate,
                entries = listOf(
                    TimelineEntry(selectedDate, TimelineEntryState.IN_PROGRESS),
                    TimelineEntry(selectedDate.minusDays(1), TimelineEntryState.SKIPPED),
                ),
                schedules = listOf(
                    TimelineSchedule(selectedDate.minusDays(2), ScheduleTarget(numerator = 1, denominator = 2)),
                ),
            ),
        )

        assertEquals(0.0, timeline.frequencyProgress?.achieved ?: -1.0, 0.0)
        assertEquals(0, timeline.currentStreak)
    }

    @Test
    fun `score changes when a new schedule takes effect inside the range`() {
        val startDate = selectedDate.minusDays(4)
        val scheduleChangeDate = selectedDate.minusDays(1)
        val completedDates = setOf(startDate, startDate.plusDays(1), selectedDate)
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = startDate,
                selectedDate = selectedDate,
                entries = completedDates.map { TimelineEntry(it, TimelineEntryState.COMPLETED) },
                schedules = listOf(
                    TimelineSchedule(startDate, ScheduleTarget.DAILY),
                    TimelineSchedule(scheduleChangeDate, ScheduleTarget(numerator = 3, denominator = 7)),
                ),
            ),
        )
        val scheduleAwareSeries = computeScheduleAwareScoreSeries(
            scheduleForDate = { date ->
                if (date.isBefore(scheduleChangeDate)) ScheduleTarget.DAILY
                else ScheduleTarget(numerator = 3, denominator = 7)
            },
            completedDates = completedDates,
            skippedDates = emptySet(),
            startDate = startDate,
            endDate = selectedDate,
        )
        val oldScheduleSeries = computeScoreSeries(
            target = ScheduleTarget.DAILY,
            completedDates = completedDates,
            skippedDates = emptySet(),
            startDate = startDate,
            endDate = scheduleChangeDate.minusDays(1),
        )
        assertEquals(
            oldScheduleSeries.last().score,
            scheduleAwareSeries[oldScheduleSeries.lastIndex].score,
            0.000001,
        )
        val oldMultiplier = decayMultiplier(ScheduleTarget.DAILY.frequency)
        assertEquals(
            "old multiplier=$oldMultiplier actual=${scheduleAwareSeries[oldScheduleSeries.lastIndex].score}",
            oldMultiplier - oldMultiplier.pow(3),
            scheduleAwareSeries[oldScheduleSeries.lastIndex].score,
            0.000001,
        )
        val endDateOnlyScore = computeScoreSeries(
            target = ScheduleTarget(numerator = 3, denominator = 7),
            completedDates = completedDates,
            skippedDates = emptySet(),
            startDate = startDate,
            endDate = selectedDate,
        ).last().score

        assertTrue(abs(timeline.score - endDateOnlyScore) > 0.0001)
        assertEquals(timeline.score, scheduleAwareSeries.last().score, 0.000001)
        val newMultiplier = decayMultiplier((ScheduleTarget(numerator = 3, denominator = 7)).frequency)
        val oldScore = oldMultiplier - oldMultiplier.pow(3)
        val scoreAtChange = oldScore * newMultiplier + (1.0 / 3.0) * (1.0 - newMultiplier)
        val expectedAfterChange = scoreAtChange * newMultiplier + 0.5 * (1.0 - newMultiplier)
        assertEquals(expectedAfterChange, timeline.score, 0.000001)
    }
}
