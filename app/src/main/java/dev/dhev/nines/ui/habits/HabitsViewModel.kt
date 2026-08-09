package dev.dhev.nines.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.TargetKind
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

private const val RECENT_DAYS_WINDOW = 5

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
            val entriesByDate = entries.associateBy { it.date }
            val todayEntry = entriesByDate[today]
            val completedToday = todayEntry?.state == EntryState.COMPLETED
            val startDate = habit.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
            val score = repository.scoreSeries(habit.id, startDate, today).lastOrNull()?.score ?: 0.0
            val schedule = repository.scheduleEffectiveOn(habit.id, today)
            val recentDays = (RECENT_DAYS_WINDOW - 1 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                DayCell(date = date, state = entriesByDate[date]?.state, isToday = date == today)
            }
            var streak = 0
            var cursor = if (completedToday) today else today.minusDays(1)
            while (entriesByDate[cursor]?.state == EntryState.COMPLETED) {
                streak++
                cursor = cursor.minusDays(1)
            }
            HabitListItem(
                id = habit.id,
                name = habit.name,
                color = habit.color,
                iconKey = habit.iconKey,
                backgroundStyleKey = habit.backgroundStyleKey,
                type = habit.type,
                completedToday = completedToday,
                currentScore = score,
                currentStreak = streak,
                recentDays = recentDays,
                currentValue = todayEntry?.numericValue,
                targetValue = schedule?.targetValue,
                targetKind = schedule?.targetKind,
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
            is HabitsAction.SetNumericValue -> viewModelScope.launch {
                repository.setNumericEntry(action.habitId, LocalDate.now(), action.value)
            }
            is HabitsAction.CreateHabit -> viewModelScope.launch {
                repository.createHabit(
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
            }
            is HabitsAction.DeleteHabit -> viewModelScope.launch {
                repository.archiveHabit(action.habitId)
            }
            is HabitsAction.SeedDemoData -> viewModelScope.launch { seedDemoData() }
        }
    }

    /** Kotlin port of the scratchpad seed.py used earlier this session for adb-based reseeding —
     * same eight habits and roughly the same history shape, but driven through the repository
     * directly so it works regardless of adb/storage permissions on any given device. */
    private suspend fun seedDemoData() {
        val today = LocalDate.now()
        val random = Random(42)
        fun daysAgo(n: Long) = today.minusDays(n)
        fun createdAt(daysAgoCreated: Long): Instant =
            daysAgo(daysAgoCreated).atStartOfDay(ZoneId.systemDefault()).toInstant()

        suspend fun addHabit(
            name: String,
            type: HabitType,
            unit: String?,
            color: Int,
            iconKey: String,
            backgroundStyleKey: String?,
            createdDaysAgo: Long,
            targetNumerator: Int,
            targetDenominator: Int,
            targetValue: Double?,
            targetKind: TargetKind?,
        ): Long = repository.createHabit(
            name = name,
            type = type,
            unit = unit,
            color = color,
            iconKey = iconKey,
            backgroundStyleKey = backgroundStyleKey,
            targetNumerator = targetNumerator,
            targetDenominator = targetDenominator,
            targetValue = targetValue,
            targetKind = targetKind,
            createdAt = createdAt(createdDaysAgo),
            scheduleEffectiveFrom = daysAgo(createdDaysAgo),
        )

        // 1. Meditate — daily, ~7 months old (long enough for the calendar's scroll to matter),
        // 21-day streak running up to today, sparser before that.
        val meditateId = addHabit("Meditate", HabitType.BOOLEAN, null, 0xFF00897B.toInt(), "meditate", "rings", 210, 1, 1, null, null)
        for (daysAgo in 0 until 210) {
            if (daysAgo < 21) {
                repository.toggleCompletion(meditateId, daysAgo(daysAgo.toLong()))
            } else if (random.nextFloat() < 0.7f) {
                repository.toggleCompletion(meditateId, daysAgo(daysAgo.toLong()))
            } else if (random.nextFloat() < 0.5f) {
                repository.setSkipped(meditateId, daysAgo(daysAgo.toLong()))
            }
        }

        // 2. Read — numerical, 20 pages/day target, inconsistent, in-progress today.
        val readId = addHabit("Read", HabitType.NUMERICAL, "pages", 0xFFFB8C00.toInt(), "book", "waves", 45, 1, 1, 20.0, TargetKind.AT_LEAST)
        for (daysAgo in 1 until 45) {
            val r = random.nextFloat()
            if (r < 0.45f) {
                repository.setNumericEntry(readId, daysAgo(daysAgo.toLong()), listOf(20, 25, 30, 22, 40).random(random).toDouble())
            } else if (r < 0.6f) {
                repository.setNumericEntry(readId, daysAgo(daysAgo.toLong()), listOf(5, 8, 12, 15).random(random).toDouble())
            }
        }
        repository.setNumericEntry(readId, today, 12.0)

        // 3. Gym — 3x/week, medium score, recent 3-day active streak.
        val gymId = addHabit("Gym", HabitType.BOOLEAN, null, 0xFF8E24AA.toInt(), "fitness", "zigzag", 50, 3, 7, null, null)
        for (daysAgo in 3 until 50) {
            if (random.nextFloat() < 0.35f) repository.toggleCompletion(gymId, daysAgo(daysAgo.toLong()))
        }
        for (daysAgo in 0 until 3) repository.toggleCompletion(gymId, daysAgo(daysAgo.toLong()))

        // 4. No Sugar — just started, short streak.
        val noSugarId = addHabit("No Sugar", HabitType.BOOLEAN, null, 0xFFD81B60.toInt(), "noSmoking", "sparkle", 4, 1, 1, null, null)
        repository.toggleCompletion(noSugarId, daysAgo(3))
        repository.setSkipped(noSugarId, daysAgo(2))
        repository.toggleCompletion(noSugarId, daysAgo(1))
        repository.toggleCompletion(noSugarId, today)

        // 5. Journal — long history, broken streak (missed last 4 days), decaying score.
        val journalId = addHabit("Journal", HabitType.BOOLEAN, null, 0xFF43A047.toInt(), "checklist", null, 70, 1, 1, null, null)
        for (daysAgo in 4 until 70) {
            if (random.nextFloat() < 0.55f) repository.toggleCompletion(journalId, daysAgo(daysAgo.toLong()))
        }

        // 6. Water — 8 glasses/day, ~4.5 months old, solid recent streak.
        val waterId = addHabit("Water", HabitType.NUMERICAL, "glasses", 0xFF039BE5.toInt(), "water", "peaks", 130, 1, 1, 8.0, TargetKind.AT_LEAST)
        for (daysAgo in 0 until 130) {
            if (daysAgo < 6) {
                repository.setNumericEntry(waterId, daysAgo(daysAgo.toLong()), listOf(8, 9, 10).random(random).toDouble())
            } else {
                val r = random.nextFloat()
                if (r < 0.5f) {
                    repository.setNumericEntry(waterId, daysAgo(daysAgo.toLong()), listOf(8, 9).random(random).toDouble())
                } else if (r < 0.7f) {
                    repository.setNumericEntry(waterId, daysAgo(daysAgo.toLong()), listOf(3, 5, 6).random(random).toDouble())
                }
            }
        }

        // 7. Sleep before 11 — ~6.5 months old, near-perfect record, second long-history habit.
        val sleepId = addHabit("Sleep before 11", HabitType.BOOLEAN, null, 0xFF3949AB.toInt(), "sleep", "dots", 195, 1, 1, null, null)
        for (daysAgo in 0 until 195) {
            if (random.nextFloat() < 0.93f) {
                repository.toggleCompletion(sleepId, daysAgo(daysAgo.toLong()))
            } else if (random.nextFloat() < 0.5f) {
                repository.setSkipped(sleepId, daysAgo(daysAgo.toLong()))
            }
        }

        // 8. Learn Spanish — every-2-days, brand new, low data.
        val spanishId = addHabit("Learn Spanish", HabitType.NUMERICAL, "min", 0xFFFFB300.toInt(), "language", "leaves", 2, 1, 2, 15.0, TargetKind.AT_LEAST)
        repository.setNumericEntry(spanishId, daysAgo(1), 15.0)
    }
}
