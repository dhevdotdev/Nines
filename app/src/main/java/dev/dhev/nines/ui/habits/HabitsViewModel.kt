package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.DemoData
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.reminders.ReminderNotificationCoordinator
import dev.dhev.nines.reminders.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(
    private val repository: HabitRepository,
    private val reminderRepository: ReminderRepository? = null,
    private val reminderScheduler: ReminderScheduler? = null,
    private val reminderNotifications: ReminderNotificationCoordinator? = null,
    private val demoData: DemoData = DemoData.None,
) : ViewModel() {

    private val today = LocalDate.now()
    private val selectedDate = MutableStateFlow(today)

    val uiState: StateFlow<HabitsUiState> = combine(
        repository.observeActiveHabits(),
        selectedDate,
    ) { habits, date -> habits to date }
        .flatMapLatest { (habits, date) ->
            if (habits.isEmpty()) {
                flowOf(emptyList<HabitListItem>() to date)
            } else {
                combine(habits.map { habitItemFlow(it, date) }) { items ->
                    items.filterNotNull() to date
                }
            }
        }
        .map { (items, date) ->
            HabitsUiState(
                habits = items,
                isLoading = false,
                today = today,
                selectedDate = date,
                demoDataAvailable = demoData.isAvailable,
            )
        }
        // Every habit's score/streak recompute combines here, all on whatever dispatcher
        // collects this flow - without this, that's viewModelScope's Main.immediate, i.e. the
        // main thread, for N habits at once on every entries change.
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitsUiState(),
        )

    /** [date] is the selected date being browsed/edited - not necessarily today. */
    private fun habitItemFlow(habit: HabitEntity, date: LocalDate): Flow<HabitListItem?> = combine(
        repository.observeHabitSnapshot(
            habitId = habit.id,
            selectedDate = flowOf(date),
            asOfDate = today,
        ),
        reminderRepository?.observe(habit.id) ?: flowOf(null),
    ) { snapshot, reminder ->
        snapshot?.let(::habitListItemFromSnapshot)?.copy(reminderTime = reminder?.localTime)
    }

    fun onAction(action: HabitsAction) {
        when (action) {
            is HabitsAction.SelectDate -> selectedDate.value = action.date

            is HabitsAction.ToggleHabit -> viewModelScope.launch {
                repository.toggleCompletion(action.habitId, selectedDate.value)
            }

            is HabitsAction.IncrementHabit -> viewModelScope.launch {
                repository.incrementNumericEntry(action.habitId, selectedDate.value, action.delta)
            }

            is HabitsAction.SetNumericValue -> viewModelScope.launch {
                repository.setNumericEntry(action.habitId, selectedDate.value, action.value)
            }

            is HabitsAction.ClearNumericEntry -> viewModelScope.launch {
                repository.clearNumericEntry(action.habitId, action.date)
            }

            is HabitsAction.UndoClearNumericEntry -> viewModelScope.launch {
                repository.setNumericEntry(action.habitId, action.date, action.value)
            }

            is HabitsAction.CreateHabit -> viewModelScope.launch {
                val habitId = repository.createHabit(
                    name = action.name,
                    type = action.type,
                    color = action.color,
                    iconKey = action.iconKey,
                    backgroundStyleKey = action.backgroundStyleKey,
                    targetNumerator = action.targetNumerator,
                    targetDenominator = action.targetDenominator,
                    unit = action.unit,
                    targetValue = action.targetValue,
                    targetKind = action.targetKind,
                )
                action.reminderTime?.let {
                    reminderRepository?.configure(habitId, it)
                    reminderScheduler?.schedule(habitId, it)
                }
            }

            is HabitsAction.UpdateHabit -> viewModelScope.launch {
                repository.updateHabit(
                    habitId = action.habitId,
                    name = action.name,
                    color = action.color,
                    iconKey = action.iconKey,
                    backgroundStyleKey = action.backgroundStyleKey,
                    unit = action.unit,
                    targetNumerator = action.targetNumerator,
                    targetDenominator = action.targetDenominator,
                    targetValue = action.targetValue,
                    targetKind = action.targetKind,
                )
                action.reminderTime?.let {
                    reminderRepository?.configure(action.habitId, it)
                    reminderScheduler?.schedule(action.habitId, it)
                } ?: run {
                    reminderRepository?.remove(action.habitId)
                    reminderScheduler?.cancel(action.habitId)
                }
            }

            is HabitsAction.DeleteHabit -> viewModelScope.launch {
                repository.archiveHabit(action.habitId)
                reminderScheduler?.cancel(action.habitId)
                reminderNotifications?.cancel(action.habitId)
            }

            is HabitsAction.ReorderHabits -> viewModelScope.launch {
                repository.reorderHabits(action.orderedIds)
            }

            is HabitsAction.SeedDemoData -> viewModelScope.launch { demoData.seed() }
        }
    }
}
