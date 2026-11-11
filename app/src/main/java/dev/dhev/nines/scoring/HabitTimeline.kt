package dev.dhev.nines.scoring

import java.time.Instant
import java.time.LocalDate

/** The two kinds of Habit entries understood by the shared timeline projection. */
enum class TimelineHabitKind {
    BOOLEAN,
    NUMERICAL,
}

enum class TimelineEntryState {
    COMPLETED,
    SKIPPED,
    IN_PROGRESS,
}

data class TimelineEntry(
    val date: LocalDate,
    val state: TimelineEntryState,
    val numericValue: Double? = null,
    val createdAt: Instant? = null,
    val tzOffsetMinutes: Int = 0,
)

/** A Schedule version expressed without persistence or Compose types. */
data class TimelineSchedule(
    val effectiveFromDate: LocalDate,
    val target: ScheduleTarget,
    val targetValue: Double? = null,
    val targetKind: FrequencyTargetKind? = null,
    /** Persistence version order breaks ties when multiple versions take effect on one date. */
    val versionOrder: Long = 0L,
)

data class HabitTimelineData(
    val entries: List<TimelineEntry>,
    val schedules: List<TimelineSchedule>,
)

data class HabitTimelineInput(
    val kind: TimelineHabitKind,
    val createdDate: LocalDate,
    val selectedDate: LocalDate,
    val entries: List<TimelineEntry>,
    val schedules: List<TimelineSchedule>,
)

/** The shared date-bounded result consumed by homepage and detail adapters. */
data class HabitTimeline(
    val isVisible: Boolean,
    val selectedDate: LocalDate,
    val selectedEntry: TimelineEntry?,
    val selectedSchedule: TimelineSchedule?,
    val score: Double,
    val runAnalysis: HabitRunAnalysis,
    val frequencyProgress: FrequencyProgress?,
) {
    val currentStreak: Int get() = runAnalysis.currentStreak
    val currentGap: Int get() = runAnalysis.currentGap
}

/**
 * Projects a Habit's Entries and versioned Schedules through one selected-date contract.
 *
 * Entries after the selected date are deliberately discarded before any derived result is
 * calculated. Score uses the Schedule effective on each scored date, not one Schedule copied
 * from the end of the range.
 */
fun buildHabitTimeline(input: HabitTimelineInput): HabitTimeline {
    val visible = isHabitVisibleOn(input.createdDate, input.selectedDate)
    if (!visible) {
        return HabitTimeline(
            isVisible = false,
            selectedDate = input.selectedDate,
            selectedEntry = null,
            selectedSchedule = null,
            score = 0.0,
            runAnalysis = emptyHabitRunAnalysis(input.createdDate, input.selectedDate),
            frequencyProgress = null,
        )
    }

    val entries =
        input.entries
            .filter { !it.date.isAfter(input.selectedDate) && !it.date.isBefore(input.createdDate) }
    val entriesByDate = entries.associateBy { it.date }
    val completedDates =
        entries
            .filter { it.state == TimelineEntryState.COMPLETED }
            .map { it.date }
            .toSet()
    val skippedDates =
        entries
            .filter { it.state == TimelineEntryState.SKIPPED }
            .map { it.date }
            .toSet()
    val selectedSchedule = scheduleEffectiveOn(input.schedules, input.selectedDate)
    val score =
        computeScheduleAwareScoreSeries(
            scheduleForDate = { date -> scheduleEffectiveOn(input.schedules, date)?.target },
            completedDates = completedDates,
            skippedDates = skippedDates,
            startDate = input.createdDate,
            endDate = input.selectedDate,
        ).lastOrNull()?.score ?: 0.0
    val runAnalysis = analyzeHabitRuns(completedDates, input.createdDate, input.selectedDate)
    val frequencyProgress =
        selectedSchedule?.let { schedule ->
            when (input.kind) {
                TimelineHabitKind.BOOLEAN -> {
                    computeFrequencyProgress(
                        selectedDate = input.selectedDate,
                        target = schedule.target,
                        input = FrequencyProgressInput.BooleanEntries(completedDates),
                    )
                }

                TimelineHabitKind.NUMERICAL -> {
                    schedule.targetValue?.let { targetValue ->
                        computeFrequencyProgress(
                            selectedDate = input.selectedDate,
                            target = schedule.target,
                            input =
                            FrequencyProgressInput.NumericEntries(
                                valuesByDate =
                                entries
                                    .filter { it.state != TimelineEntryState.SKIPPED }
                                    .mapNotNull { entry -> entry.numericValue?.let { entry.date to it } }
                                    .toMap(),
                                targetValue = targetValue,
                                targetKind = schedule.targetKind ?: FrequencyTargetKind.AT_LEAST,
                            ),
                        )
                    }
                }
            }
        }

    return HabitTimeline(
        isVisible = true,
        selectedDate = input.selectedDate,
        selectedEntry = entriesByDate[input.selectedDate],
        selectedSchedule = selectedSchedule,
        score = score,
        runAnalysis = runAnalysis,
        frequencyProgress = frequencyProgress,
    )
}

fun scheduleEffectiveOn(
    schedules: List<TimelineSchedule>,
    date: LocalDate,
): TimelineSchedule? = schedules
    .filter { !it.effectiveFromDate.isAfter(date) }
    .maxWithOrNull(compareBy<TimelineSchedule> { it.effectiveFromDate }.thenBy { it.versionOrder })
