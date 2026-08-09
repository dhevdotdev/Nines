package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind

data class HabitListItem(
    val id: Long,
    val name: String,
    val color: Int,
    val iconKey: String,
    val type: HabitType,
    val completedToday: Boolean,
    val currentScore: Double,
    val currentValue: Double? = null,
    val targetValue: Double? = null,
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
}
