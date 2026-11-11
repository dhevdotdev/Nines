package dev.dhev.nines.scoring

import java.time.LocalDate

/**
 * Whether a Habit still needs a reminder for [date]: true when the Entries already satisfy the
 * Schedule in force on that date, or the day was skipped.
 *
 * [entries] may be any superset of the Schedule window ending on [date]; this function picks out
 * the window itself. A numeric at-most target counts as resolved once anything is logged today,
 * because the value can only be judged against the limit after it exists.
 */
fun isReminderResolvedOn(
    kind: TimelineHabitKind,
    date: LocalDate,
    schedules: List<TimelineSchedule>,
    entries: List<TimelineEntry>,
): Boolean {
    val schedule = scheduleEffectiveOn(schedules, date) ?: return false
    val today = entries.firstOrNull { it.date == date }
    return when {
        today?.state == TimelineEntryState.SKIPPED -> true

        kind == TimelineHabitKind.NUMERICAL && schedule.targetKind == FrequencyTargetKind.AT_MOST ->
            today?.numericValue != null

        else -> frequencyProgressFor(kind, date, schedule, entries)?.isMet ?: false
    }
}

private fun frequencyProgressFor(
    kind: TimelineHabitKind,
    date: LocalDate,
    schedule: TimelineSchedule,
    entries: List<TimelineEntry>,
): FrequencyProgress? {
    val input = when (kind) {
        TimelineHabitKind.NUMERICAL -> FrequencyProgressInput.NumericEntries(
            valuesByDate = entries.mapNotNull { entry -> entry.numericValue?.let { entry.date to it } }.toMap(),
            targetValue = schedule.targetValue ?: return null,
            targetKind = schedule.targetKind ?: FrequencyTargetKind.AT_LEAST,
        )

        TimelineHabitKind.BOOLEAN -> FrequencyProgressInput.BooleanEntries(
            completedDates = entries.filter { it.state == TimelineEntryState.COMPLETED }.map { it.date }.toSet(),
        )
    }
    return computeFrequencyProgress(date, schedule.target, input)
}
