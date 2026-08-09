package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = repository.observeActiveHabits()
        .flatMapLatest { habits ->
            if (habits.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(habits.map(::habitItemFlow)) { items -> items.toList() }
            }
        }
        .map { items -> HabitsUiState(habits = items, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitsUiState(),
        )

    private fun habitItemFlow(habit: HabitEntity): Flow<HabitListItem> =
        repository.observeEntries(habit.id).map { entries ->
            val today = LocalDate.now()
            val todayEntry = entries.firstOrNull { it.date == today }
            val completedToday = todayEntry?.state == EntryState.COMPLETED
            val startDate = habit.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
            val score = repository.scoreSeries(habit.id, startDate, today).lastOrNull()?.score ?: 0.0
            val schedule = repository.scheduleEffectiveOn(habit.id, today)
            HabitListItem(
                id = habit.id,
                name = habit.name,
                color = habit.color,
                iconKey = habit.iconKey,
                type = habit.type,
                completedToday = completedToday,
                currentScore = score,
                currentValue = todayEntry?.numericValue,
                targetValue = schedule?.targetValue,
                unit = habit.unit,
                targetNumerator = schedule?.targetNumerator ?: 1,
                targetDenominator = schedule?.targetDenominator ?: 1,
            )
        }

    fun onAction(action: HabitsAction) {
        when (action) {
            is HabitsAction.ToggleHabit -> viewModelScope.launch {
                repository.toggleCompletion(action.habitId, LocalDate.now())
            }
            is HabitsAction.IncrementHabit -> viewModelScope.launch {
                repository.incrementNumericEntry(action.habitId, LocalDate.now(), action.delta)
            }
            is HabitsAction.CreateHabit -> viewModelScope.launch {
                repository.createHabit(
                    name = action.name,
                    type = action.type,
                    color = action.color,
                    iconKey = action.iconKey,
                    targetNumerator = action.targetNumerator,
                    targetDenominator = action.targetDenominator,
                    unit = action.unit,
                    targetValue = action.targetValue,
                    targetKind = action.targetKind,
                )
            }
        }
    }
}
