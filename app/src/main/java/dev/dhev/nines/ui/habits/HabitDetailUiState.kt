package dev.dhev.nines.ui.habits

import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import java.time.LocalDate

/** A single run of consecutive completed days, used to list past streaks ranked by length. */
data class StreakRun(val start: LocalDate, val end: LocalDate, val length: Int)

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
    val cycleCompletions: Int? = null,
    val cycleTarget: Int? = null,
    val cycleWindowDays: Int? = null,
    val topStreaks: List<StreakRun> = emptyList(),
    val weekdayRates: List<Double> = emptyList(),
    val momentumDeltaPoints: Double? = null,
    val typicalTimeOfDay: String? = null,
    val history: List<DayCell> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface HabitDetailAction {
    // Carry an explicit date rather than always assuming "today" — these now double as the
    // retroactive edit actions fired from the expanded month view, not just the mark-complete
    // button for the current day.
    data class Toggle(val date: LocalDate) : HabitDetailAction
    data class Increment(val date: LocalDate, val delta: Double) : HabitDetailAction
    data class SetValue(val date: LocalDate, val value: Double) : HabitDetailAction
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
