package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.FrequencyTargetKind
import dev.dhev.nines.scoring.TimelineEntryState

/**
 * Adapts the shared date-bounded observation into the homepage row read model.
 *
 * Visibility and all date/scoring semantics belong to the observation's timeline;
 * this adapter only chooses the fields the homepage presents.
 */
fun habitListItemFromSnapshot(snapshot: HabitRepository.HabitObservationSnapshot): HabitListItem? {
    val habit = snapshot.habit
    val timeline = snapshot.observation.timeline
    if (!timeline.isVisible) return null

    val selectedSchedule = timeline.selectedSchedule
    val selectedEntry = timeline.selectedEntry
    return HabitListItem(
        id = habit.id,
        name = habit.name,
        color = habit.color,
        iconKey = habit.iconKey,
        backgroundStyleKey = habit.backgroundStyleKey,
        type = habit.type,
        completed = selectedEntry?.state == TimelineEntryState.COMPLETED,
        currentScore = timeline.score,
        currentStreak = timeline.currentStreak,
        currentValue = selectedEntry?.numericValue,
        targetValue = selectedSchedule?.targetValue,
        targetKind = selectedSchedule?.targetKind?.let {
            if (it == FrequencyTargetKind.AT_MOST) TargetKind.AT_MOST else TargetKind.AT_LEAST
        },
        unit = habit.unit,
        targetNumerator = selectedSchedule?.target?.numerator ?: 1,
        targetDenominator = selectedSchedule?.target?.denominator ?: 1,
        frequencyProgress = timeline.frequencyProgress,
    )
}
