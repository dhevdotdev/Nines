package dev.dhev.nines.scoring

import java.time.LocalDate
import java.time.ZoneOffset

data class HabitAnalyticsInput(
    val kind: TimelineHabitKind,
    val createdDate: LocalDate,
    val asOfDate: LocalDate,
    val entries: List<TimelineEntry>,
    val runAnalysis: HabitRunAnalysis =
        analyzeHabitRuns(
            completedDates =
            entries
                .filter {
                    it.state == TimelineEntryState.COMPLETED &&
                        !it.date.isBefore(createdDate) &&
                        !it.date.isAfter(asOfDate)
                }.map { it.date }
                .toSet(),
            startDate = createdDate,
            endDate = asOfDate,
        ),
)

enum class RecordProgress {
    NONE,
    CURRENT_RECORD,
    CHASING_RECORD,
}

data class HabitAnalytics(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val topStreaks: List<StreakRun>,
    val weekdayRates: List<Double>,
    val momentumDeltaPoints: Double?,
    val typicalTimeOfDay: String?,
    val longestGap: StreakRun?,
    val numericTotal: Double?,
    val numericAverage: Double?,
    val recordProgress: RecordProgress,
)

/** Computes detail-only statistics without depending on Compose, Room, or ViewModel state. */
fun computeHabitAnalytics(input: HabitAnalyticsInput): HabitAnalytics {
    val entries =
        input.entries.filter {
            !it.date.isBefore(input.createdDate) && !it.date.isAfter(input.asOfDate)
        }
    val completedEntries = entries.filter { it.state == TimelineEntryState.COMPLETED }
    val completedDates = completedEntries.map { it.date }.toSet()
    require(
        input.runAnalysis.startDate == input.createdDate &&
            input.runAnalysis.endDate == input.asOfDate &&
            input.runAnalysis.completedDates == completedDates,
    ) {
        "runAnalysis must cover the same completed dates and date range as analytics"
    }
    val runAnalysis = input.runAnalysis
    val streakRuns = runAnalysis.streakRuns
    val bestStreak = streakRuns.maxOfOrNull { it.length } ?: 0
    val numeric = numericSummary(input.kind, completedEntries)

    return HabitAnalytics(
        currentStreak = runAnalysis.currentStreak,
        bestStreak = bestStreak,
        totalCompletions = completedDates.size,
        topStreaks = streakRuns.sortedByDescending { it.length }.take(5),
        weekdayRates = weekdayRates(input.createdDate, input.asOfDate, completedDates),
        momentumDeltaPoints = momentumDeltaPoints(input.createdDate, input.asOfDate, completedDates),
        typicalTimeOfDay = typicalTimeOfDay(completedEntries),
        longestGap = runAnalysis.gapRuns.filter { it.length >= 3 }.maxByOrNull { it.length },
        numericTotal = numeric?.first,
        numericAverage = numeric?.second,
        recordProgress = recordProgress(bestStreak, runAnalysis.currentStreak),
    )
}

/** Completion rate per weekday (Monday first), or empty until the habit has two weeks of history. */
private fun weekdayRates(
    createdDate: LocalDate,
    asOfDate: LocalDate,
    completedDates: Set<LocalDate>,
): List<Double> {
    if (createdDate.isAfter(asOfDate.minusDays(13))) return emptyList()
    val totals = IntArray(7)
    val completions = IntArray(7)
    var date = createdDate
    while (!date.isAfter(asOfDate)) {
        val index = date.dayOfWeek.value - 1
        totals[index]++
        if (date in completedDates) completions[index]++
        date = date.plusDays(1)
    }
    return (0 until 7).map { index ->
        if (totals[index] > 0) completions[index].toDouble() / totals[index] else 0.0
    }
}

/** The last 30 days' completion rate minus the 30 before it, in points, once there are 60 days of history. */
private fun momentumDeltaPoints(
    createdDate: LocalDate,
    asOfDate: LocalDate,
    completedDates: Set<LocalDate>,
): Double? {
    if (createdDate.isAfter(asOfDate.minusDays(59))) return null

    fun completionRate(windowEnd: LocalDate): Double = (0L until 30L).count { windowEnd.minusDays(it) in completedDates } / 30.0
    return (completionRate(asOfDate) - completionRate(asOfDate.minusDays(30))) * 100
}

/** The part of the day most completions happen in, once there are at least five to judge from. */
private fun typicalTimeOfDay(completedEntries: List<TimelineEntry>): String? {
    if (completedEntries.size < 5) return null
    return completedEntries
        .map { timeOfDay(it) }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
}

private fun timeOfDay(entry: TimelineEntry): String {
    val hour = entry.createdAt?.atOffset(ZoneOffset.ofTotalSeconds(entry.tzOffsetMinutes * 60))?.hour ?: return "night"
    return when (hour) {
        in 5..11 -> "morning"
        in 12..16 -> "afternoon"
        in 17..20 -> "evening"
        else -> "night"
    }
}

/** Total and average of the positive logged values of a numeric habit, or null with nothing logged. */
private fun numericSummary(
    kind: TimelineHabitKind,
    completedEntries: List<TimelineEntry>,
): Pair<Double, Double>? {
    if (kind != TimelineHabitKind.NUMERICAL) return null
    val values = completedEntries.mapNotNull { it.numericValue }.filter { it > 0 }
    if (values.isEmpty()) return null
    val total = values.sum()
    return total to total / values.size
}

private fun recordProgress(
    bestStreak: Int,
    currentStreak: Int,
): RecordProgress = when {
    bestStreak == 0 || currentStreak == 0 -> RecordProgress.NONE
    currentStreak >= bestStreak -> RecordProgress.CURRENT_RECORD
    else -> RecordProgress.CHASING_RECORD
}
