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
    val targetValue: Double? = null,
    val targetKind: TargetKind? = null,
    val targetNumerator: Int = 1,
    val targetDenominator: Int = 1,
    val history: List<DayCell> = emptyList(),
    val isLoading: Boolean = true,
)
