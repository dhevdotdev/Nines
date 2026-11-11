package dev.dhev.nines.data

import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.FrequencyTargetKind
import dev.dhev.nines.scoring.ScheduleTarget
import dev.dhev.nines.scoring.TimelineEntry
import dev.dhev.nines.scoring.TimelineEntryState
import dev.dhev.nines.scoring.TimelineHabitKind
import dev.dhev.nines.scoring.TimelineSchedule

/** The one place persistence entities become the persistence-free types the scoring package works on. */
internal fun HabitType.toTimelineKind(): TimelineHabitKind = when (this) {
    HabitType.NUMERICAL -> TimelineHabitKind.NUMERICAL
    HabitType.BOOLEAN -> TimelineHabitKind.BOOLEAN
}

internal fun EntryEntity.toTimelineEntry() = TimelineEntry(
    date = date,
    state = when (state) {
        EntryState.COMPLETED -> TimelineEntryState.COMPLETED
        EntryState.SKIPPED -> TimelineEntryState.SKIPPED
        EntryState.IN_PROGRESS -> TimelineEntryState.IN_PROGRESS
    },
    numericValue = numericValue,
    createdAt = createdAt,
    tzOffsetMinutes = tzOffsetMinutes,
)

/** A boolean habit has no target kind; the timeline treats that as at-least. */
internal fun TargetKind?.toFrequencyTargetKind(): FrequencyTargetKind = when (this) {
    TargetKind.AT_MOST -> FrequencyTargetKind.AT_MOST
    TargetKind.AT_LEAST, null -> FrequencyTargetKind.AT_LEAST
}

internal fun ScheduleVersionEntity.toTimelineSchedule() = TimelineSchedule(
    effectiveFromDate = effectiveFromDate,
    target = ScheduleTarget(targetNumerator, targetDenominator),
    targetValue = targetValue,
    targetKind = targetKind.toFrequencyTargetKind(),
    versionOrder = id,
)
