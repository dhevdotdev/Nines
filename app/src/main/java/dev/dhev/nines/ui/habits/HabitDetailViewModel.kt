package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.todayFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModel(
    private val habitId: Long,
    private val repository: HabitRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    /** A date the user browsed to. Null means "follow today", including across midnight. */
    private val browsedDate = MutableStateFlow<LocalDate?>(null)

    // Rolling over at midnight restarts the snapshot pipeline for the new day. Score decay, six-plus
    // O(history-length) passes for streaks, gaps, weekday and momentum, and building up to ~270
    // DayCells run off the main thread, which is what removes the jank rather than a spinner.
    val uiState: StateFlow<HabitDetailUiState> = clock
        .todayFlow()
        .flatMapLatest { today ->
            repository
                .observeHabitSnapshot(
                    habitId = habitId,
                    selectedDate = browsedDate.map { it ?: today },
                    asOfDate = today,
                ).map { snapshot -> habitDetailUiState(habitId, snapshot, today, clock.zone) }
                .flowOn(computeDispatcher)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitDetailUiState(habitId = habitId, today = LocalDate.now(clock)),
        )

    fun onAction(action: HabitDetailAction) {
        when (action) {
            is HabitDetailAction.SelectDate -> browsedDate.value = action.date.takeIf { it != LocalDate.now(clock) }

            is HabitDetailAction.Toggle -> viewModelScope.launch {
                repository.toggleCompletion(habitId, action.date)
            }

            is HabitDetailAction.Increment -> viewModelScope.launch {
                repository.incrementNumericEntry(habitId, action.date, action.delta)
            }

            is HabitDetailAction.SetValue -> viewModelScope.launch {
                repository.setNumericEntry(habitId, action.date, action.value)
            }

            is HabitDetailAction.ClearNumericEntry -> viewModelScope.launch {
                repository.clearNumericEntry(habitId, action.date)
            }

            is HabitDetailAction.UndoClearNumericEntry -> viewModelScope.launch {
                repository.setNumericEntry(habitId, action.date, action.value)
            }

            is HabitDetailAction.Update -> viewModelScope.launch {
                repository.updateHabit(
                    habitId = habitId,
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
            }

            is HabitDetailAction.Delete -> viewModelScope.launch {
                repository.archiveHabit(habitId)
            }
        }
    }
}
