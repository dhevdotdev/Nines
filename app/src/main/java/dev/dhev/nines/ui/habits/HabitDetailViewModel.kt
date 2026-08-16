package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.computeGapRuns
import dev.dhev.nines.scoring.computeStreakRuns
import dev.dhev.nines.scoring.FrequencyTargetKind
import dev.dhev.nines.scoring.HabitTimelineInput
import dev.dhev.nines.scoring.TimelineEntryState
import dev.dhev.nines.scoring.TimelineHabitKind
import dev.dhev.nines.scoring.buildHabitTimeline
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
import java.time.ZoneOffset

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
                    val completedDates = entries.filter { it.state == TimelineEntryState.COMPLETED }.map { it.date }.toSet()

                    // Full runs (not just the longest length) so the detail screen can list past
                    // streaks ranked by length, not just report a single number - current/best
                    // are both just views onto the same underlying runs.
                    val streakRuns = computeStreakRuns(completedDates, createdDate, today)
                    val currentStreak = timeline.currentStreak
                    val bestStreak = streakRuns.maxOfOrNull { it.length } ?: 0
                    val topStreaks = streakRuns.sortedByDescending { it.length }.take(5)

                    val totalCompletions = entries.count { it.state == TimelineEntryState.COMPLETED }
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

                    // Which weekdays this habit actually lands on - a habit can have a strong
                    // score overall while consistently sliding on one or two particular days,
                    // and nothing else on this screen would ever surface that pattern.
                    val weekdayRates = if (!createdDate.isAfter(today.minusDays(13))) {
                        val totals = IntArray(7)
                        val completions = IntArray(7)
                        var wd = createdDate
                        while (!wd.isAfter(today)) {
                            val idx = wd.dayOfWeek.value - 1
                            totals[idx]++
                            if (entriesByDate[wd]?.state == TimelineEntryState.COMPLETED) completions[idx]++
                            wd = wd.plusDays(1)
                        }
                        (0 until 7).map { i -> if (totals[i] > 0) completions[i].toDouble() / totals[i] else 0.0 }
                    } else {
                        emptyList()
                    }

                    // Comparing two full, equal-length trailing windows (not calendar months,
                    // which vary in length and would bias the comparison) rather than eyeballing
                    // the score - a direct "are things trending up or down lately" readout.
                    val momentumDeltaPoints = if (!createdDate.isAfter(today.minusDays(59))) {
                        fun completionRate(windowEnd: LocalDate): Double {
                            val windowStart = windowEnd.minusDays(29)
                            var count = 0
                            var wd = windowStart
                            while (!wd.isAfter(windowEnd)) {
                                if (entriesByDate[wd]?.state == TimelineEntryState.COMPLETED) count++
                                wd = wd.plusDays(1)
                            }
                            return count / 30.0
                        }
                        (completionRate(today) - completionRate(today.minusDays(30))) * 100
                    } else {
                        null
                    }

                    // Uses timestamps already captured on every entry (tzOffsetMinutes +
                    // createdAt) but never surfaced anywhere - a "when do I actually do this"
                    // pattern that only shows up once you have several logged times to compare.
                    val completedEntries = entries.filter { it.state == TimelineEntryState.COMPLETED }
                    val typicalTimeOfDay = if (completedEntries.size >= 5) {
                        completedEntries
                            .map {
                                val hour = it.createdAt
                                    ?.atOffset(ZoneOffset.ofTotalSeconds(it.tzOffsetMinutes * 60))
                                    ?.hour
                                    ?: return@map "night"
                                when (hour) {
                                    in 5..11 -> "morning"
                                    in 12..16 -> "afternoon"
                                    in 17..20 -> "evening"
                                    else -> "night"
                                }
                            }
                            .groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }
                            ?.key
                    } else {
                        null
                    }

                    // The inverse of the streak runs above - best streaks alone only tell half
                    // the story; this is the other, harder half, using the same
                    // completed/not-completed rule so the two lists never disagree about what
                    // counts as a "day". A single missed day isn't a "gap" worth naming - only
                    // surface one long enough to represent a real lapse, not routine noise.
                    val longestGap = computeGapRuns(completedDates, createdDate, today)
                        .filter { it.length >= 3 }
                        .maxByOrNull { it.length }

                    // Never summed/averaged anywhere else - the running totals only ever showed
                    // a day-count, never what was actually logged on those days.
                    val loggedValues = entries.mapNotNull { it.numericValue }.filter { it > 0 }
                    val numericTotal = if (habit.type == HabitType.NUMERICAL && loggedValues.isNotEmpty()) {
                        loggedValues.sum()
                    } else {
                        null
                    }
                    val numericAverage = if (habit.type == HabitType.NUMERICAL && loggedValues.isNotEmpty()) {
                        loggedValues.sum() / loggedValues.size
                    } else {
                        null
                    }

                    // Forward-looking rather than retrospective like everything else on this
                    // screen - "how close am I" instead of only "how have I done".
                    val recordProgressLabel = when {
                        bestStreak == 0 -> null
                        currentStreak == 0 -> null
                        currentStreak >= bestStreak -> "You're on your longest streak yet"
                        else -> "${bestStreak - currentStreak} days to beat your record of $bestStreak"
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
                        currentStreak = currentStreak,
                        bestStreak = bestStreak,
                        totalCompletions = totalCompletions,
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
                        topStreaks = topStreaks,
                        weekdayRates = weekdayRates,
                        momentumDeltaPoints = momentumDeltaPoints,
                        typicalTimeOfDay = typicalTimeOfDay,
                        longestGap = longestGap,
                        numericTotal = numericTotal,
                        numericAverage = numericAverage,
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
