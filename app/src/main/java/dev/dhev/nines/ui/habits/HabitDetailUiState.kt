package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind

data class HabitDetailUiState(
    val habitId: Long = -1,
    val name: String = "",
    val iconKey: String = "",
    val backgroundStyleKey: String? = null,
    val color: Int = 0,
    val type: HabitType = HabitType.BOOLEAN,
    val unit: String? = null,
    val currentScore: Double = 0.0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val completedToday: Boolean = false,
    val currentValue: Double? = null,
    val targetValue: Double? = null,
    val targetKind: TargetKind? = null,
    val targetNumerator: Int = 1,
    val targetDenominator: Int = 1,
    val history: List<DayCell> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface HabitDetailAction {
    data object Toggle : HabitDetailAction
    data class Increment(val delta: Double) : HabitDetailAction
    data class SetValue(val value: Double) : HabitDetailAction
    data class Update(
        val name: String,
        val color: Int,
        val iconKey: String,
        val backgroundStyleKey: String?,
        val targetNumerator: Int,
        val targetDenominator: Int,
        val targetValue: Double?,
        val targetKind: TargetKind?,
        val unit: String?,
    ) : HabitDetailAction
    data object Delete : HabitDetailAction
}
