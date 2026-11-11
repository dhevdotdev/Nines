package dev.dhev.nines.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchivedHabitsViewModel(
    private val repository: HabitRepository,
    private val reminders: ReminderRepository? = null,
    private val scheduler: ReminderScheduler? = null,
) : ViewModel() {
    val uiState: StateFlow<ArchivedHabitsUiState> =
        repository
            .observeArchivedHabits()
            .map { habits ->
                ArchivedHabitsUiState(
                    archivedHabits =
                    habits.map { habit ->
                        ArchivedHabitItem(
                            id = habit.id,
                            name = habit.name,
                            color = habit.color,
                            iconKey = habit.iconKey,
                        )
                    },
                    isLoading = false,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ArchivedHabitsUiState(),
            )

    fun onAction(action: ArchivedHabitsAction) {
        when (action) {
            is ArchivedHabitsAction.RestoreHabit -> {
                viewModelScope.launch {
                    repository.restoreHabit(action.habitId)
                    reminders?.get(action.habitId)?.let { scheduler?.schedule(action.habitId, it.localTime) }
                }
            }
        }
    }
}
