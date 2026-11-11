package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.DemoData
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.todayFlow
import dev.dhev.nines.reminders.HabitReminders
import kotlinx.coroutines.CoroutineDispatcher
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
import java.time.Clock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(
    private val repository: HabitRepository,
    private val reminders: HabitReminders,
    private val demoData: DemoData = DemoData.None,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    /** A date the user browsed to. Null means "follow today", including across midnight. */
    private val browsedDate = MutableStateFlow<LocalDate?>(null)

    private fun selectedDate(): LocalDate = browsedDate.value ?: LocalDate.now(clock)

    // Rolling over at midnight restarts the per-habit pipeline for the new day. The heavy score and
    // streak recompute for every habit runs off the main thread: without flowOn it would run on
    // viewModelScope's Main.immediate for N habits at once on every entries change.
    val uiState: StateFlow<HabitsUiState> = clock
        .todayFlow()
        .flatMapLatest { todayDate -> habitsUiState(todayDate).flowOn(computeDispatcher) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitsUiState(today = LocalDate.now(clock)),
        )

    private fun habitsUiState(todayDate: LocalDate): Flow<HabitsUiState> = combine(
        repository.observeActiveHabits(),
        browsedDate,
    ) { habits, browsed -> habits to (browsed ?: todayDate) }
        .flatMapLatest { (habits, date) ->
            if (habits.isEmpty()) {
                flowOf(emptyList<HabitListItem>() to date)
            } else {
                combine(habits.map { habitItemFlow(it, todayDate, date) }) { items ->
                    items.filterNotNull() to date
                }
            }
        }.map { (items, date) ->
            HabitsUiState(
                habits = items,
                isLoading = false,
                today = todayDate,
                selectedDate = date,
                demoDataAvailable = demoData.isAvailable,
            )
        }

    /** [date] is the selected date being browsed/edited - not necessarily today. */
    private fun habitItemFlow(habit: HabitEntity, todayDate: LocalDate, date: LocalDate): Flow<HabitListItem?> = combine(
        repository.observeHabitSnapshot(
            habitId = habit.id,
            selectedDate = flowOf(date),
            asOfDate = todayDate,
        ),
        reminders.observeTime(habit.id),
    ) { snapshot, reminderTime ->
        snapshot?.let(::habitListItemFromSnapshot)?.copy(reminderTime = reminderTime)
    }

    fun onAction(action: HabitsAction) {
        when (action) {
            is HabitsAction.SelectDate -> browsedDate.value = action.date.takeIf { it != LocalDate.now(clock) }

            is HabitsAction.ToggleHabit -> viewModelScope.launch {
                repository.toggleCompletion(action.habitId, selectedDate())
            }

            is HabitsAction.IncrementHabit -> viewModelScope.launch {
                repository.incrementNumericEntry(action.habitId, selectedDate(), action.delta)
            }

            is HabitsAction.SetNumericValue -> viewModelScope.launch {
                repository.setNumericEntry(action.habitId, selectedDate(), action.value)
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
                action.reminderTime?.let { reminders.configure(habitId, it) }
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
                reminders.configure(action.habitId, action.reminderTime)
            }

            is HabitsAction.DeleteHabit -> viewModelScope.launch {
                repository.archiveHabit(action.habitId)
                reminders.onArchived(action.habitId)
            }

            is HabitsAction.ReorderHabits -> viewModelScope.launch {
                repository.reorderHabits(action.orderedIds)
            }

            is HabitsAction.SeedDemoData -> viewModelScope.launch { demoData.seed() }
        }
    }
}
