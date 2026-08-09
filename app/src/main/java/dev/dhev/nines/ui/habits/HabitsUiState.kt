package dev.dhev.nines.ui.habits

data class HabitListItem(
    val id: Long,
    val name: String,
    val color: Int,
    val completedToday: Boolean,
    val currentScore: Double,
)

data class HabitsUiState(
    val habits: List<HabitListItem> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface HabitsAction {
    data class ToggleHabit(val habitId: Long) : HabitsAction
    data class CreateHabit(val name: String) : HabitsAction
}
