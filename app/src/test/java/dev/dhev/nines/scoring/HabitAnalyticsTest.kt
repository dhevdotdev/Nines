package dev.dhev.nines.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class HabitAnalyticsTest {
    private val created = LocalDate.of(2026, 1, 1)
    private val asOf = LocalDate.of(2026, 1, 10)

    @Test
    fun `analytics is date bounded and ranks streaks`() {
        val result = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = asOf,
                entries = listOf(
                    entry(0),
                    entry(1), entry(2), entry(3),
                    entry(8), entry(9), entry(10),
                    entry(11),
                ),
            ),
        )

        assertEquals(6, result.totalCompletions)
        assertEquals(3, result.bestStreak)
        assertEquals(listOf(3, 3), result.topStreaks.map { it.length })
        assertEquals(RecordProgress.CURRENT_RECORD, result.recordProgress)
        assertTrue(result.weekdayRates.isEmpty())
    }

    @Test
    fun `numeric aggregates and typical time use completed values only`() {
        val result = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.NUMERICAL,
                createdDate = created,
                asOfDate = asOf,
                entries = (1..5).map { day ->
                    entry(day, value = day.toDouble(), createdAt = Instant.parse("2026-01-0${day}T08:00:00Z"))
                } + entry(0, value = 200.0, createdAt = Instant.parse("2025-12-31T08:00:00Z"))
                    + entry(6, state = TimelineEntryState.IN_PROGRESS, value = 100.0),
            ),
        )

        assertEquals(15.0, result.numericTotal ?: 0.0, 0.0)
        assertEquals(3.0, result.numericAverage ?: 0.0, 0.0)
        assertEquals("morning", result.typicalTimeOfDay)
        assertEquals(RecordProgress.NONE, result.recordProgress)
        assertNull(result.momentumDeltaPoints)
    }

    @Test
    fun `thresholds expose weekday rates at fourteen days and gaps at three days`() {
        val fourteenDay = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(13),
                entries = listOf(entry(1)),
            ),
        )
        val thirteenDay = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(12),
                entries = listOf(entry(1)),
            ),
        )
        val gap = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(5),
                entries = listOf(entry(1), entry(2), entry(6)),
            ),
        )
        val twoDayGap = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(3),
                entries = listOf(entry(1), entry(4)),
            ),
        )
        val fourTimedEntries = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(3),
                entries = (1..4).map { day -> entry(day, createdAt = Instant.parse("2026-01-0${day}T08:00:00Z")) },
            ),
        )
        val fiveTimedEntries = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(4),
                entries = (1..5).map { day -> entry(day, createdAt = Instant.parse("2026-01-0${day}T08:00:00Z")) },
            ),
        )
        val momentum59Days = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(58),
                entries = emptyList(),
            ),
        )
        val momentum60Days = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(59),
                entries = emptyList(),
            ),
        )

        assertEquals(7, fourteenDay.weekdayRates.size)
        assertTrue(thirteenDay.weekdayRates.isEmpty())
        assertEquals(3, gap.longestGap?.length)
        assertEquals(RecordProgress.CHASING_RECORD, gap.recordProgress)
        assertNull(twoDayGap.longestGap)
        assertNull(fourTimedEntries.typicalTimeOfDay)
        assertEquals("morning", fiveTimedEntries.typicalTimeOfDay)
        assertNull(momentum59Days.momentumDeltaPoints)
        assertEquals(0.0, momentum60Days.momentumDeltaPoints ?: -1.0, 0.0)
    }

    @Test
    fun `analytics honors upstream run analysis from the timeline`() {
        val entries = listOf(entry(1), entry(2), entry(5))
        val timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                selectedDate = asOf,
                entries = entries,
                schedules = listOf(TimelineSchedule(created, ScheduleTarget.DAILY)),
            ),
        )

        val result = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = asOf,
                entries = entries,
                runAnalysis = timeline.runAnalysis,
            ),
        )

        assertEquals(timeline.currentStreak, result.currentStreak)
        assertEquals(timeline.runAnalysis.streakRuns.maxOf { it.length }, result.bestStreak)
        assertEquals(
            timeline.runAnalysis.gapRuns.filter { it.length >= 3 }.maxBy { it.length },
            result.longestGap,
        )
    }

    private fun entry(
        day: Int,
        state: TimelineEntryState = TimelineEntryState.COMPLETED,
        value: Double? = null,
        createdAt: Instant? = null,
    ) = TimelineEntry(
        date = created.plusDays((day - 1).toLong()),
        state = state,
        numericValue = value,
        createdAt = createdAt,
    )
}
