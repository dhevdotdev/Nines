package dev.dhev.nines.scoring

import java.time.LocalDate

/** The complete derived Streak/Gap view for one bounded Habit date range. */
data class HabitRunAnalysis(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val completedDates: Set<LocalDate>,
    val streakRuns: List<StreakRun>,
    val gapRuns: List<StreakRun>,
    val currentStreak: Int,
    val currentGap: Int,
)

/**
 * Derives every Streak and Gap projection for one bounded date range.
 *
 * This is the authoritative scoring seam for run semantics. Timeline and detail analytics may
 * present different slices of the result, but they no longer own separate derivations.
 */
fun analyzeHabitRuns(
    completedDates: Set<LocalDate>,
    startDate: LocalDate,
    endDate: LocalDate,
): HabitRunAnalysis {
    val streakRuns = computeStreakRuns(completedDates, startDate, endDate)
    val gapRuns = computeGapRuns(completedDates, startDate, endDate)
    return HabitRunAnalysis(
        startDate = startDate,
        endDate = endDate,
        completedDates = completedDates,
        streakRuns = streakRuns,
        gapRuns = gapRuns,
        currentStreak = currentStreakLength(streakRuns, endDate),
        currentGap = currentGapLength(gapRuns, endDate),
    )
}

fun emptyHabitRunAnalysis(
    startDate: LocalDate,
    endDate: LocalDate,
): HabitRunAnalysis = HabitRunAnalysis(
    startDate = startDate,
    endDate = endDate,
    completedDates = emptySet(),
    streakRuns = emptyList(),
    gapRuns = emptyList(),
    currentStreak = 0,
    currentGap = 0,
)

private fun currentGapLength(
    runs: List<StreakRun>,
    endDate: LocalDate,
): Int {
    val last = runs.lastOrNull() ?: return 0
    return if (!last.end.isBefore(endDate.minusDays(1))) last.length else 0
}
