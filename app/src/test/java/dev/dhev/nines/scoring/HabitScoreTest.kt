package dev.dhev.nines.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

private const val TOLERANCE = 0.002

class HabitScoreTest {

    @Test
    fun `daily habit half-life lands at day 13`() {
        val start = LocalDate.of(2026, 1, 1)
        val allDates = generateSequence(start) { it.plusDays(1) }.take(90).toSet()

        val series = computeScoreSeries(
            target = ScheduleTarget.DAILY,
            completedDates = allDates,
            skippedDates = emptySet(),
            startDate = start,
            endDate = start.plusDays(89),
        )

        fun scoreOnDay(day: Int) = series[day - 1].score

        assertEquals(0.052, scoreOnDay(1), TOLERANCE)
        assertEquals(0.312, scoreOnDay(7), TOLERANCE)
        assertEquals(0.500, scoreOnDay(13), TOLERANCE)
        assertEquals(0.798, scoreOnDay(30), TOLERANCE)
        assertEquals(0.992, scoreOnDay(90), TOLERANCE)
    }

    @Test
    fun `skipped day freezes the score instead of decaying it`() {
        val start = LocalDate.of(2026, 1, 1)
        val completed = generateSequence(start) { it.plusDays(1) }.take(20).toSet()
        val skipDay = start.plusDays(20)

        val withSkip = computeScoreSeries(
            target = ScheduleTarget.DAILY,
            completedDates = completed,
            skippedDates = setOf(skipDay),
            startDate = start,
            endDate = skipDay,
        )

        val scoreBeforeSkip = withSkip[withSkip.size - 2].score
        val scoreOnSkipDay = withSkip.last().score

        assertEquals(scoreBeforeSkip, scoreOnSkipDay, 1e-9)
    }

    @Test
    fun `a long streak survives two weeks of misses without collapsing to zero`() {
        val start = LocalDate.of(2026, 1, 1)
        val completed = generateSequence(start) { it.plusDays(1) }.take(200).toSet()
        val endDate = start.plusDays(213) // 200 completed days + 14 missed days

        val series = computeScoreSeries(
            target = ScheduleTarget.DAILY,
            completedDates = completed,
            skippedDates = emptySet(),
            startDate = start,
            endDate = endDate,
        )

        val scoreBeforeMisses = series[199].score
        val scoreAfter14Misses = series.last().score

        assertTrue("score before misses should be near-perfect", scoreBeforeMisses > 0.99)
        assertTrue(
            "score after 14 misses should have dropped but not collapsed to near zero",
            scoreAfter14Misses in 0.3..0.6,
        )
    }

    @Test
    fun `three times per week perfect execution ramps slower than daily`() {
        val start = LocalDate.of(2026, 1, 5) // a Monday
        val target = ScheduleTarget(numerator = 3, denominator = 7)

        val completed = generateSequence(start) { it.plusDays(1) }
            .take(91)
            .filter { it.dayOfWeek in setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY) }
            .toSet()

        val series = computeScoreSeries(
            target = target,
            completedDates = completed,
            skippedDates = emptySet(),
            startDate = start,
            endDate = start.plusDays(90),
        )

        fun scoreAfterWeeks(weeks: Int) = series[weeks * 7 - 1].score

        assertEquals(0.540, scoreAfterWeeks(4), 0.03)
        assertEquals(0.827, scoreAfterWeeks(8), 0.03)
        assertEquals(0.949, scoreAfterWeeks(13), 0.03)
    }

    @Test
    fun `decay multiplier is looser for sparser schedules`() {
        val daily = decayMultiplier(ScheduleTarget.DAILY.frequency)
        val weekly = decayMultiplier(ScheduleTarget(1, 7).frequency)
        val monthly = decayMultiplier(ScheduleTarget(1, 30).frequency)

        assertTrue("sparser schedules should decay more slowly (m closer to 1)", weekly > daily)
        assertTrue(monthly > weekly)
    }

    @Test
    fun `streak as of a past date ignores completions that happened after it`() {
        val start = LocalDate.of(2026, 1, 1)
        // Completed every day through day 20, but we only ask about the streak as of day 10 -
        // days 11-20 must not count, exactly as if day 10 were the last known date.
        val completed = generateSequence(start) { it.plusDays(1) }.take(20).toSet()
        val asOfDate = start.plusDays(9) // day 10

        val runs = computeStreakRuns(completed, start, asOfDate)
        val streak = currentStreakLength(runs, asOfDate)

        assertEquals(10, streak)
    }

    @Test
    fun `schedule target rejects an impossible numerator`() {
        try {
            ScheduleTarget(numerator = 8, denominator = 7)
            assertTrue("expected an exception for numerator > denominator", false)
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }
}
