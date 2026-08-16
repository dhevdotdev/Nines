package dev.dhev.nines.scoring

import java.time.LocalDate
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A schedule target expressed as an exact rational (n times per d days), never a decimal.
 * This keeps rolling-window lengths exact integers instead of accumulating rounding error.
 */
data class ScheduleTarget(val numerator: Int, val denominator: Int) {
    init {
        require(numerator > 0) { "numerator must be positive" }
        require(denominator > 0) { "denominator must be positive" }
        require(numerator <= denominator) { "numerator cannot exceed denominator" }
    }

    val frequency: Double get() = numerator.toDouble() / denominator

    companion object {
        val DAILY = ScheduleTarget(1, 1)
    }
}

enum class DayOutcome { COMPLETED, SKIPPED, MISSED }

data class ScorePoint(val date: LocalDate, val score: Double)

/**
 * The exponential decay multiplier for a habit's target frequency.
 * Half-life = 13 / sqrt(frequency) days: daily habits (f=1) have a 13-day half-life,
 * sparser habits get proportionally longer memory, sub-linearly.
 */
fun decayMultiplier(frequency: Double): Double = 0.5.pow(sqrt(frequency) / 13.0)

/**
 * Computes the exponentially-weighted score series for a habit between [startDate] and
 * [endDate] inclusive. [completedDates] and [skippedDates] should cover at least the window
 * needed to evaluate compliance on [startDate] (i.e. include history before it).
 *
 * Non-daily targets have their numerator and window doubled before computing compliance —
 * "3 times per week" becomes "6 successes needed within a trailing 14-day window" — which
 * tolerates irregular day-of-week placement better than a literal 7-day window would.
 *
 * A skipped day freezes the score rather than decaying it: no compliance ratio is computed
 * and the recurrence is not applied for that day.
 */
fun computeScoreSeries(
    target: ScheduleTarget,
    completedDates: Set<LocalDate>,
    skippedDates: Set<LocalDate>,
    startDate: LocalDate,
    endDate: LocalDate,
): List<ScorePoint> = computeScoreSeriesForDates(
    scheduleForDate = { target },
    completedDates = completedDates,
    skippedDates = skippedDates,
    startDate = startDate,
    endDate = endDate,
)

/** Computes a Score series when the Schedule may change inside the date range. */
fun computeScheduleAwareScoreSeries(
    scheduleForDate: (LocalDate) -> ScheduleTarget?,
    completedDates: Set<LocalDate>,
    skippedDates: Set<LocalDate>,
    startDate: LocalDate,
    endDate: LocalDate,
): List<ScorePoint> = computeScoreSeriesForDates(
    scheduleForDate = scheduleForDate,
    completedDates = completedDates,
    skippedDates = skippedDates,
    startDate = startDate,
    endDate = endDate,
)

private fun computeScoreSeriesForDates(
    scheduleForDate: (LocalDate) -> ScheduleTarget?,
    completedDates: Set<LocalDate>,
    skippedDates: Set<LocalDate>,
    startDate: LocalDate,
    endDate: LocalDate,
): List<ScorePoint> {
    require(!endDate.isBefore(startDate)) { "endDate must not be before startDate" }

    val results = ArrayList<ScorePoint>()
    var score = 0.0
    var date = startDate
    while (!date.isAfter(endDate)) {
        val target = scheduleForDate(date) ?: ScheduleTarget.DAILY
        val isDaily = target.denominator == 1
        val effectiveNumerator = if (isDaily) target.numerator else target.numerator * 2
        val effectiveWindow = if (isDaily) target.denominator else target.denominator * 2
        val m = decayMultiplier(target.frequency)
        if (date !in skippedDates) {
            var completions = 0
            var windowDate = date
            repeat(effectiveWindow) {
                if (windowDate in completedDates) completions++
                windowDate = windowDate.minusDays(1)
            }
            val p = min(1.0, completions.toDouble() / effectiveNumerator)
            score = score * m + p * (1.0 - m)
        }
        results.add(ScorePoint(date, score))
        date = date.plusDays(1)
    }
    return results
}

/** A single run of consecutive days matching some day-level condition (completed, or not). */
data class StreakRun(val start: LocalDate, val end: LocalDate, val length: Int)

/**
 * Runs of consecutive completed days between [startDate] and [endDate] inclusive, in
 * chronological order. The last entry is the "current" streak exactly when its [StreakRun.end]
 * is [endDate] or the day before it — a run that stopped earlier than that has already broken.
 */
fun computeStreakRuns(completedDates: Set<LocalDate>, startDate: LocalDate, endDate: LocalDate): List<StreakRun> =
    computeRuns(startDate, endDate) { it in completedDates }

/** The inverse of [computeStreakRuns]: runs of consecutive NOT-completed days, i.e. gaps. */
fun computeGapRuns(completedDates: Set<LocalDate>, startDate: LocalDate, endDate: LocalDate): List<StreakRun> =
    computeRuns(startDate, endDate) { it !in completedDates }

private fun computeRuns(startDate: LocalDate, endDate: LocalDate, matches: (LocalDate) -> Boolean): List<StreakRun> {
    require(!endDate.isBefore(startDate)) { "endDate must not be before startDate" }
    val runs = mutableListOf<StreakRun>()
    var runStart: LocalDate? = null
    var length = 0
    var date = startDate
    while (!date.isAfter(endDate)) {
        if (matches(date)) {
            if (length == 0) runStart = date
            length++
        } else if (length > 0) {
            runs.add(StreakRun(runStart!!, date.minusDays(1), length))
            length = 0
        }
        date = date.plusDays(1)
    }
    if (length > 0) runs.add(StreakRun(runStart!!, endDate, length))
    return runs
}

/** The streak still running as of [endDate] — the last run's length if it reaches through
 * [endDate] or the day before it, zero if the most recent run already ended earlier than that. */
fun currentStreakLength(streakRuns: List<StreakRun>, endDate: LocalDate): Int {
    val last = streakRuns.lastOrNull() ?: return 0
    return if (!last.end.isBefore(endDate.minusDays(1))) last.length else 0
}
