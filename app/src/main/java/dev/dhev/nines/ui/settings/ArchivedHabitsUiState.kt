package dev.dhev.nines.ui.settings

data class ArchivedHabitItem(
    val id: Long,
    val name: String,
    val color: Int,
    val iconKey: String,
)

data class ArchivedHabitsUiState(
    val archivedHabits: List<ArchivedHabitItem> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface ArchivedHabitsAction {
    data class RestoreHabit(
        val habitId: Long,
    ) : ArchivedHabitsAction
}
