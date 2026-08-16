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
                currentStreak = 3,
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
                currentStreak = 0,
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
                currentStreak = 0,
            ),
        )
        val thirteenDay = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(12),
                entries = listOf(entry(1)),
                currentStreak = 0,
            ),
        )
        val gap = computeHabitAnalytics(
            HabitAnalyticsInput(
                kind = TimelineHabitKind.BOOLEAN,
                createdDate = created,
                asOfDate = created.plusDays(5),
                entries = listOf(entry(1), entry(2), entry(6)),
                currentStreak = 1,
            ),
        )

        assertEquals(7, fourteenDay.weekdayRates.size)
        assertTrue(thirteenDay.weekdayRates.isEmpty())
        assertEquals(3, gap.longestGap?.length)
        assertEquals(RecordProgress.CHASING_RECORD, gap.recordProgress)
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
