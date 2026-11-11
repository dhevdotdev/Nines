package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.toTargetKind
import dev.dhev.nines.scoring.HabitAnalytics
import dev.dhev.nines.scoring.HabitAnalyticsInput
import dev.dhev.nines.scoring.HabitTimeline
import dev.dhev.nines.scoring.RecordProgress
import dev.dhev.nines.scoring.TimelineEntry
import dev.dhev.nines.scoring.TimelineEntryState
import dev.dhev.nines.scoring.TimelineHabitKind
import dev.dhev.nines.scoring.computeHabitAnalytics
import java.time.LocalDate
import java.time.ZoneId

private const val HISTORY_MONTHS_BACK = 9L

/**
 * Adapts a [HabitRepository.HabitTimelineSnapshot] into the detail screen's [HabitDetailUiState].
 * A null snapshot means the habit is gone, which the screen shows as loaded-but-empty.
 */
internal fun habitDetailUiState(
    habitId: Long,
    snapshot: HabitRepository.HabitTimelineSnapshot?,
    today: LocalDate,
    zone: ZoneId,
): HabitDetailUiState {
    if (snapshot == null) return HabitDetailUiState(habitId = habitId, today = today, isLoading = false)
    val habit = snapshot.habit
    val timeline = snapshot.timeline
    val schedule = timeline.selectedSchedule
    val createdDate = habit.createdAt.atZone(zone).toLocalDate()
    val analytics = computeHabitAnalytics(
        HabitAnalyticsInput(
            kind = if (habit.type == HabitType.NUMERICAL) TimelineHabitKind.NUMERICAL else TimelineHabitKind.BOOLEAN,
            createdDate = createdDate,
            asOfDate = timeline.selectedDate,
            entries = snapshot.entriesThroughAsOfDate,
            runAnalysis = timeline.runAnalysis,
        ),
    )
    val selectedEntry = timeline.selectedEntry
    val cycle = cycleProgress(habit.type, timeline)

    return HabitDetailUiState(
        habitId = habitId,
        today = today,
        selectedDate = timeline.selectedDate,
        name = habit.name,
        iconKey = habit.iconKey,
        backgroundStyleKey = habit.backgroundStyleKey,
        color = habit.color,
        type = habit.type,
        unit = habit.unit,
        currentScore = timeline.score,
        currentStreak = analytics.currentStreak,
        bestStreak = analytics.bestStreak,
        totalCompletions = analytics.totalCompletions,
        completed = selectedEntry?.state == TimelineEntryState.COMPLETED,
        currentValue = selectedEntry?.numericValue,
        targetValue = schedule?.targetValue,
        targetKind = schedule?.targetKind?.toTargetKind(),
        targetNumerator = schedule?.target?.numerator ?: 1,
        targetDenominator = schedule?.target?.denominator ?: 1,
        cycleCompletions = cycle.completions,
        cycleTarget = cycle.target,
        cycleWindowDays = cycle.windowDays,
        topStreaks = analytics.topStreaks,
        weekdayRates = analytics.weekdayRates,
        momentumDeltaPoints = analytics.momentumDeltaPoints,
        typicalTimeOfDay = analytics.typicalTimeOfDay,
        longestGap = analytics.longestGap,
        numericTotal = analytics.numericTotal,
        numericAverage = analytics.numericAverage,
        recordProgressLabel = recordProgressLabel(analytics),
        history = buildDetailHistory(
            entries = snapshot.entriesThroughAsOfDate,
            createdDate = createdDate,
            historyStartDate = today.minusMonths(HISTORY_MONTHS_BACK).withDayOfMonth(1),
            selectedDate = timeline.selectedDate,
            asOfDate = today,
            today = today,
        ),
        isLoading = false,
    )
}

private data class CycleProgress(
    val completions: Int?,
    val target: Int?,
    val windowDays: Int?,
)

/**
 * Daily habits (1/1) don't need this: the streak already says everything there is to say. It only
 * adds information for a boolean habit with real slack ("3x per week").
 */
private fun cycleProgress(
    type: HabitType,
    timeline: HabitTimeline,
): CycleProgress {
    val target = timeline.selectedSchedule?.target
    val windowDays = target?.denominator
    val completions = if (type == HabitType.BOOLEAN && windowDays != null && windowDays > 1) {
        timeline.frequencyProgress?.achieved?.toInt()
    } else {
        null
    }
    return CycleProgress(completions, target?.numerator, windowDays)
}

/** Forward-looking rather than retrospective: "how close am I" instead of only "how have I done". */
private fun recordProgressLabel(analytics: HabitAnalytics): String? = when (analytics.recordProgress) {
    RecordProgress.NONE -> null
    RecordProgress.CURRENT_RECORD -> "You're on your longest streak yet"
    else -> "${analytics.bestStreak - analytics.currentStreak} days to beat your record of ${analytics.bestStreak}"
}

/** Builds the calendar data used only by the detail history presentation. */
internal fun buildDetailHistory(
    entries: List<TimelineEntry>,
    createdDate: LocalDate,
    historyStartDate: LocalDate,
    selectedDate: LocalDate,
    asOfDate: LocalDate,
    today: LocalDate,
): List<DayCell> {
    val boundedEntries = entries
        .filter { !it.date.isBefore(createdDate) && !it.date.isAfter(asOfDate) }
        .associateBy { it.date }
    val start = maxOf(createdDate, historyStartDate)
    val end = minOf(selectedDate, asOfDate)
    if (end.isBefore(start)) return emptyList()
    return generateSequence(start) { it.plusDays(1) }
        .takeWhile { !it.isAfter(end) }
        .map { date ->
            val entry = boundedEntries[date]
            DayCell(
                date = date,
                state = entry?.state?.let {
                    when (it) {
                        TimelineEntryState.COMPLETED -> EntryState.COMPLETED
                        TimelineEntryState.SKIPPED -> EntryState.SKIPPED
                        TimelineEntryState.IN_PROGRESS -> EntryState.IN_PROGRESS
                    }
                },
                isToday = date == today,
                numericValue = entry?.numericValue,
            )
        }
        .toList()
}
