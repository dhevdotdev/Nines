package dev.dhev.nines.scoring

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HabitRunAnalysisTest {
    private val start = LocalDate.of(2026, 8, 1)
    private val end = LocalDate.of(2026, 8, 7)

    @Test
    fun `analysis returns streaks gaps and current projections for one bounded range`() {
        val analysis =
            analyzeHabitRuns(
                completedDates = setOf(start, start.plusDays(1), start.plusDays(3), start.plusDays(4)),
                startDate = start,
                endDate = end,
            )

        assertEquals(listOf(2, 2), analysis.streakRuns.map { it.length })
        assertEquals(listOf(1, 2), analysis.gapRuns.map { it.length })
        assertEquals(0, analysis.currentStreak)
        assertEquals(2, analysis.currentGap)
    }

    @Test
    fun `analysis current streak includes a run ending yesterday`() {
        val analysis =
            analyzeHabitRuns(
                completedDates = setOf(end.minusDays(2), end.minusDays(1)),
                startDate = start,
                endDate = end,
            )

        assertEquals(2, analysis.currentStreak)
        assertEquals(1, analysis.currentGap)
    }

    @Test
    fun `analysis current streak includes a completion on the end date`() {
        val analysis =
            analyzeHabitRuns(
                completedDates = setOf(end),
                startDate = start,
                endDate = end,
            )

        assertEquals(1, analysis.currentStreak)
        assertEquals(6, analysis.currentGap)
    }

    @Test
    fun `analysis ignores completed dates outside its bounded range`() {
        val analysis =
            analyzeHabitRuns(
                completedDates = setOf(start.minusDays(1), start.plusDays(1), end.plusDays(1)),
                startDate = start,
                endDate = end,
            )

        assertEquals(listOf(1), analysis.streakRuns.map { it.length })
        assertEquals(start.plusDays(1), analysis.streakRuns.single().start)
        assertEquals(start.plusDays(1), analysis.streakRuns.single().end)
    }

    @Test
    fun `analysis handles a one-day empty range`() {
        val analysis = analyzeHabitRuns(emptySet(), start, start)

        assertEquals(emptyList<StreakRun>(), analysis.streakRuns)
        assertEquals(listOf(1), analysis.gapRuns.map { it.length })
        assertEquals(0, analysis.currentStreak)
        assertEquals(1, analysis.currentGap)
    }
}
