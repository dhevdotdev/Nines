package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import java.time.LocalDate

/** One day in a habit's at-a-glance history strip. [state] is null when no entry was logged. */
data class DayCell(
    val date: LocalDate,
    val state: EntryState?,
    val isToday: Boolean,
)

data class HabitListItem(
    val id: Long,
    val name: String,
    val color: Int,
    val iconKey: String,
    val type: HabitType,
    val completedToday: Boolean,
    val currentScore: Double,
    val currentStreak: Int = 0,
    val recentDays: List<DayCell> = emptyList(),
    val currentValue: Double? = null,
    val targetValue: Double? = null,
    val targetKind: TargetKind? = null,
    val unit: String? = null,
    val targetNumerator: Int = 1,
    val targetDenominator: Int = 1,
)

data class HabitsUiState(
    val habits: List<HabitListItem> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface HabitsAction {
    data class ToggleHabit(val habitId: Long) : HabitsAction
    data class IncrementHabit(val habitId: Long, val delta: Double) : HabitsAction
    data class SetNumericValue(val habitId: Long, val value: Double) : HabitsAction
    data class CreateHabit(
        val name: String,
        val color: Int,
        val iconKey: String,
        val type: HabitType,
        val targetNumerator: Int = 1,
        val targetDenominator: Int = 1,
        val targetValue: Double? = null,
        val targetKind: TargetKind? = null,
        val unit: String? = null,
    ) : HabitsAction
    data class UpdateHabit(
        val habitId: Long,
        val name: String,
        val color: Int,
        val iconKey: String,
        val targetNumerator: Int = 1,
        val targetDenominator: Int = 1,
        val targetValue: Double? = null,
        val targetKind: TargetKind? = null,
        val unit: String? = null,
    ) : HabitsAction
    data class DeleteHabit(val habitId: Long) : HabitsAction
}
