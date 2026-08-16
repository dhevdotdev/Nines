package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.HabitAnalyticsInput
import dev.dhev.nines.scoring.FrequencyTargetKind
import dev.dhev.nines.scoring.HabitTimelineInput
import dev.dhev.nines.scoring.TimelineEntryState
import dev.dhev.nines.scoring.TimelineHabitKind
import dev.dhev.nines.scoring.buildHabitTimeline
import dev.dhev.nines.scoring.RecordProgress
import dev.dhev.nines.scoring.computeHabitAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
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

    private val selectedDate = kotlinx.coroutines.flow.MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HabitDetailUiState> = repository.observeHabit(habitId)
        .flatMapLatest { habit ->
            if (habit == null) {
                flowOf(HabitDetailUiState(habitId = habitId, isLoading = false))
            } else {
                val today = LocalDate.now()
                val createdDate = habit.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
                combine(
                    repository.observeHabitTimelineData(habitId),
                    selectedDate,
                ) { timelineData, selectedDate ->
                    val entries = timelineData.entries
                    val entriesByDate = entries.associateBy { it.date }
                    val timeline = buildHabitTimeline(
                        HabitTimelineInput(
                            kind = if (habit.type == HabitType.NUMERICAL) TimelineHabitKind.NUMERICAL else TimelineHabitKind.BOOLEAN,
                            createdDate = createdDate,
                            selectedDate = selectedDate,
                            entries = timelineData.entries,
                            schedules = timelineData.schedules,
                        ),
                    )
                    val schedule = timeline.selectedSchedule
                    val analytics = computeHabitAnalytics(
                        HabitAnalyticsInput(
                            kind = if (habit.type == HabitType.NUMERICAL) TimelineHabitKind.NUMERICAL else TimelineHabitKind.BOOLEAN,
                            createdDate = createdDate,
                            asOfDate = selectedDate,
                            entries = entries,
                            currentStreak = timeline.currentStreak,
                        ),
                    )
                    val selectedEntry = timeline.selectedEntry

                    // Daily habits (1/1) don't need this - the streak already says everything
                    // there is to say. It only adds information for a habit with real slack
                    // ("3x per week"): how much of that slack is left in the window closing today.
                    val cycleTarget = schedule?.target?.numerator
                    val cycleWindowDays = schedule?.target?.denominator
                    val cycleCompletions = if (habit.type == HabitType.BOOLEAN && cycleWindowDays != null && cycleWindowDays > 1) {
                        timeline.frequencyProgress?.achieved?.toInt()
                    } else {
                        null
                    }

                    // Forward-looking rather than retrospective like everything else on this
                    // screen - "how close am I" instead of only "how have I done".
                    val recordProgressLabel = when {
                        analytics.recordProgress == RecordProgress.NONE -> null
                        analytics.recordProgress == RecordProgress.CURRENT_RECORD -> "You're on your longest streak yet"
                        else -> "${analytics.bestStreak - analytics.currentStreak} days to beat your record of ${analytics.bestStreak}"
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
                                state = entry?.state?.let {
                                    when (it) {
                                        TimelineEntryState.COMPLETED -> EntryState.COMPLETED
                                        TimelineEntryState.SKIPPED -> EntryState.SKIPPED
                                        TimelineEntryState.IN_PROGRESS -> EntryState.IN_PROGRESS
                                    }
                                },
                                isToday = date == today,
                                numericValue = entry?.numericValue,
                            )
                        }
                        .toList()

                    HabitDetailUiState(
                        habitId = habitId,
                        selectedDate = selectedDate,
                        name = habit.name,
                        iconKey = habit.iconKey,
                        backgroundStyleKey = habit.backgroundStyleKey,
                        color = habit.color,
                        type = habit.type,
                        unit = habit.unit,
                        currentScore = timeline.score,
                        currentStreak = analytics.currentStreak,
                        bestStreak = analytics.bestStreak,
                        totalCompletions = analytics.totalCompletions,
                        completed = selectedEntry?.state == TimelineEntryState.COMPLETED,
                        currentValue = selectedEntry?.numericValue,
                        targetValue = schedule?.targetValue,
                        targetKind = schedule?.targetKind?.let {
                            if (it == FrequencyTargetKind.AT_MOST) TargetKind.AT_MOST else TargetKind.AT_LEAST
                        },
                        targetNumerator = schedule?.target?.numerator ?: 1,
                        targetDenominator = schedule?.target?.denominator ?: 1,
                        cycleCompletions = cycleCompletions,
                        cycleTarget = cycleTarget,
                        cycleWindowDays = cycleWindowDays,
                        topStreaks = analytics.topStreaks,
                        weekdayRates = analytics.weekdayRates,
                        momentumDeltaPoints = analytics.momentumDeltaPoints,
                        typicalTimeOfDay = analytics.typicalTimeOfDay,
                        longestGap = analytics.longestGap,
                        numericTotal = analytics.numericTotal,
                        numericAverage = analytics.numericAverage,
                        recordProgressLabel = recordProgressLabel,
                        history = history,
                        isLoading = false,
                    )
                }
            }
        }
        // Score decay, six-plus O(history-length) passes for streaks/gaps/weekday/momentum, and
        // building up to ~270 DayCells all ran on this flow's collection context with no
        // dispatcher specified, which defaults to viewModelScope's Main.immediate - i.e. the main
        // thread, right as the detail screen is trying to animate in. Moving it off-thread is
        // what actually fixes the perceived jank, not a loading spinner papering over it.
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitDetailUiState(habitId = habitId),
        )

    fun onAction(action: HabitDetailAction) {
        when (action) {
            is HabitDetailAction.SelectDate -> selectedDate.value = action.date
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
