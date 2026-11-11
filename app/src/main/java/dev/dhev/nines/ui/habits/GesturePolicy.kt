package dev.dhev.nines.ui.habits

import java.time.LocalDate

enum class HabitSwipeDirection {
    START_TO_END,
    END_TO_START,
}

enum class HabitSwipeAction {
    EDIT,
    ARCHIVE,
    NONE,
}

data class HabitGesturePolicyInput(
    val selectedDate: LocalDate,
    val today: LocalDate,
    val reorderMode: Boolean,
    val swipeDirection: HabitSwipeDirection?,
)

fun canEnterReorderMode(
    selectedDate: LocalDate,
    today: LocalDate,
): Boolean = selectedDate == today

fun isReorderModeActive(
    reorderMode: Boolean,
    selectedDate: LocalDate,
    today: LocalDate,
): Boolean = reorderMode && canEnterReorderMode(selectedDate, today)

/** Keeps gesture meaning and mode exclusivity independent of pointer/Compose implementation. */
fun habitSwipeAction(input: HabitGesturePolicyInput): HabitSwipeAction = when {
    input.reorderMode || input.swipeDirection == null -> HabitSwipeAction.NONE
    input.swipeDirection == HabitSwipeDirection.START_TO_END -> HabitSwipeAction.EDIT
    else -> HabitSwipeAction.ARCHIVE
}
