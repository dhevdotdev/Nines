package dev.dhev.nines.scoring

import java.time.LocalDate
import java.time.ZoneOffset

data class HabitAnalyticsInput(
    val kind: TimelineHabitKind,
    val createdDate: LocalDate,
    val asOfDate: LocalDate,
    val entries: List<TimelineEntry>,
    val currentStreak: Int,
    val runAnalysis: HabitRunAnalysis? = null,
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
    val entries = input.entries.filter {
        !it.date.isBefore(input.createdDate) && !it.date.isAfter(input.asOfDate)
    }
    val entriesByDate = entries.associateBy { it.date }
    val completedDates = entries
        .filter { it.state == TimelineEntryState.COMPLETED }
        .map { it.date }
        .toSet()
    val runAnalysis = input.runAnalysis ?: analyzeHabitRuns(completedDates, input.createdDate, input.asOfDate)
    val streakRuns = runAnalysis.streakRuns
    val bestStreak = streakRuns.maxOfOrNull { it.length } ?: 0
    val totalCompletions = completedDates.size

    val weekdayRates = if (!input.createdDate.isAfter(input.asOfDate.minusDays(13))) {
        val totals = IntArray(7)
        val completions = IntArray(7)
        var date = input.createdDate
        while (!date.isAfter(input.asOfDate)) {
            val index = date.dayOfWeek.value - 1
            totals[index]++
            if (entriesByDate[date]?.state == TimelineEntryState.COMPLETED) completions[index]++
            date = date.plusDays(1)
        }
        (0 until 7).map { index ->
            if (totals[index] > 0) completions[index].toDouble() / totals[index] else 0.0
        }
    } else {
        emptyList()
    }

    val momentumDeltaPoints = if (!input.createdDate.isAfter(input.asOfDate.minusDays(59))) {
        fun completionRate(windowEnd: LocalDate): Double {
            val windowStart = windowEnd.minusDays(29)
            var count = 0
            var date = windowStart
            while (!date.isAfter(windowEnd)) {
                if (entriesByDate[date]?.state == TimelineEntryState.COMPLETED) count++
                date = date.plusDays(1)
            }
            return count / 30.0
        }
        (completionRate(input.asOfDate) - completionRate(input.asOfDate.minusDays(30))) * 100
    } else {
        null
    }

    val completedEntries = entries.filter { it.state == TimelineEntryState.COMPLETED }
    val typicalTimeOfDay = if (completedEntries.size >= 5) {
        completedEntries
            .map {
                val hour = it.createdAt
                    ?.atOffset(ZoneOffset.ofTotalSeconds(it.tzOffsetMinutes * 60))
                    ?.hour
                    ?: return@map "night"
                when (hour) {
                    in 5..11 -> "morning"
                    in 12..16 -> "afternoon"
                    in 17..20 -> "evening"
                    else -> "night"
                }
            }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    } else {
        null
    }

    val longestGap = runAnalysis.gapRuns
        .filter { it.length >= 3 }
        .maxByOrNull { it.length }
    val loggedValues = entries
        .filter { it.state == TimelineEntryState.COMPLETED }
        .mapNotNull { it.numericValue }
        .filter { it > 0 }
    val numericTotal = if (input.kind == TimelineHabitKind.NUMERICAL && loggedValues.isNotEmpty()) {
        loggedValues.sum()
    } else {
        null
    }
    val numericAverage = numericTotal?.let { it / loggedValues.size }
    val recordProgress = when {
        bestStreak == 0 || runAnalysis.currentStreak == 0 -> RecordProgress.NONE
        runAnalysis.currentStreak >= bestStreak -> RecordProgress.CURRENT_RECORD
        else -> RecordProgress.CHASING_RECORD
    }

    return HabitAnalytics(
        currentStreak = runAnalysis.currentStreak,
        bestStreak = bestStreak,
        totalCompletions = totalCompletions,
        topStreaks = streakRuns.sortedByDescending { it.length }.take(5),
        weekdayRates = weekdayRates,
        momentumDeltaPoints = momentumDeltaPoints,
        typicalTimeOfDay = typicalTimeOfDay,
        longestGap = longestGap,
        numericTotal = numericTotal,
        numericAverage = numericAverage,
        recordProgress = recordProgress,
    )
}
