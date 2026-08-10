package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.EntryState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private const val HISTORY_MONTHS_BACK = 9L

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModel(
    private val habitId: Long,
    private val repository: HabitRepository,
) : ViewModel() {

    val uiState: StateFlow<HabitDetailUiState> = repository.observeHabit(habitId)
        .flatMapLatest { habit ->
            if (habit == null) {
                flowOf(HabitDetailUiState(habitId = habitId, isLoading = false))
            } else {
                val today = LocalDate.now()
                val createdDate = habit.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
                combine(
                    repository.observeEntries(habitId),
                    flowOf(repository.scheduleEffectiveOn(habitId, today)),
                ) { entries, schedule ->
                    val entriesByDate = entries.associateBy { it.date }
                    val score = repository.scoreSeries(habitId, createdDate, today).lastOrNull()?.score ?: 0.0

                    var currentStreak = 0
                    var cursor = if (entriesByDate[today]?.state == EntryState.COMPLETED) today else today.minusDays(1)
                    while (entriesByDate[cursor]?.state == EntryState.COMPLETED) {
                        currentStreak++
                        cursor = cursor.minusDays(1)
                    }

                    var bestStreak = 0
                    var running = 0
                    var day = createdDate
                    while (!day.isAfter(today)) {
                        if (entriesByDate[day]?.state == EntryState.COMPLETED) {
                            running++
                            if (running > bestStreak) bestStreak = running
                        } else {
                            running = 0
                        }
                        day = day.plusDays(1)
                    }

                    val totalCompletions = entries.count { it.state == EntryState.COMPLETED }
                    val todayEntry = entriesByDate[today]

                    // Daily habits (1/1) don't need this - the streak already says everything
                    // there is to say. It only adds information for a habit with real slack
                    // ("3x per week"): how much of that slack is left in the window closing today.
                    val cycleTarget = schedule?.targetNumerator
                    val cycleWindowDays = schedule?.targetDenominator
                    val cycleCompletions = if (cycleWindowDays != null && cycleWindowDays > 1) {
                        val windowStart = maxOf(createdDate, today.minusDays(cycleWindowDays - 1L))
                        generateSequence(windowStart) { it.plusDays(1) }
                            .takeWhile { !it.isAfter(today) }
                            .count { entriesByDate[it]?.state == EntryState.COMPLETED }
                    } else {
                        null
                    }

                    val historyStart = maxOf(
                        createdDate,
                        today.minusMonths(HISTORY_MONTHS_BACK).withDayOfMonth(1),
                    )
                    val history = generateSequence(historyStart) { it.plusDays(1) }
                        .takeWhile { !it.isAfter(today) }
                        .map { date ->
                            val entry = entriesByDate[date]
                            DayCell(
                                date = date,
                                state = entry?.state,
                                isToday = date == today,
                                numericValue = entry?.numericValue,
                            )
                        }
                        .toList()

                    HabitDetailUiState(
                        habitId = habitId,
                        name = habit.name,
                        iconKey = habit.iconKey,
                        backgroundStyleKey = habit.backgroundStyleKey,
                        color = habit.color,
                        type = habit.type,
                        unit = habit.unit,
                        currentScore = score,
                        currentStreak = currentStreak,
                        bestStreak = bestStreak,
                        totalCompletions = totalCompletions,
                        completedToday = todayEntry?.state == EntryState.COMPLETED,
                        currentValue = todayEntry?.numericValue,
                        targetValue = schedule?.targetValue,
                        targetKind = schedule?.targetKind,
                        targetNumerator = schedule?.targetNumerator ?: 1,
                        targetDenominator = schedule?.targetDenominator ?: 1,
                        cycleCompletions = cycleCompletions,
                        cycleTarget = cycleTarget,
                        cycleWindowDays = cycleWindowDays,
                        history = history,
                        isLoading = false,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitDetailUiState(habitId = habitId),
        )

    fun onAction(action: HabitDetailAction) {
        when (action) {
            is HabitDetailAction.Toggle -> viewModelScope.launch {
                repository.toggleCompletion(habitId, action.date)
            }
            is HabitDetailAction.Increment -> viewModelScope.launch {
                repository.incrementNumericEntry(habitId, action.date, action.delta)
            }
            is HabitDetailAction.SetValue -> viewModelScope.launch {
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
