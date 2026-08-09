package dev.dhev.nines.data

import dev.dhev.nines.data.local.EntryDao
import dev.dhev.nines.data.local.HabitDao
import dev.dhev.nines.data.local.ScheduleVersionDao
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import dev.dhev.nines.data.local.entity.TargetKind
import dev.dhev.nines.scoring.ScheduleTarget
import dev.dhev.nines.scoring.ScorePoint
import dev.dhev.nines.scoring.computeScoreSeries
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

class HabitRepository(
    private val habitDao: HabitDao,
    private val scheduleVersionDao: ScheduleVersionDao,
    private val entryDao: EntryDao,
) {
    fun observeActiveHabits(): Flow<List<HabitEntity>> = habitDao.observeActiveHabits()

    fun observeHabit(habitId: Long): Flow<HabitEntity?> = habitDao.observeById(habitId)

    fun observeEntries(habitId: Long): Flow<List<EntryEntity>> = entryDao.observeEntries(habitId)

    suspend fun scheduleEffectiveOn(habitId: Long, date: LocalDate): ScheduleVersionEntity? =
        scheduleVersionDao.versionEffectiveOn(habitId, date)

    suspend fun createHabit(
        name: String,
        type: HabitType,
        color: Int,
        iconKey: String,
        backgroundStyleKey: String? = null,
        targetNumerator: Int = 1,
        targetDenominator: Int = 1,
        unit: String? = null,
        targetValue: Double? = null,
        targetKind: TargetKind? = null,
        // Overridable only so debug demo-data seeding can backdate a habit's creation date
        // (to populate its calendar history) — real habit creation always uses the default.
        createdAt: Instant = Instant.now(),
        scheduleEffectiveFrom: LocalDate = LocalDate.now(),
    ): Long {
        val position = habitDao.nextPosition()
        val habitId = habitDao.insert(
            HabitEntity(
                name = name,
                type = type,
                unit = unit,
                color = color,
                iconKey = iconKey,
                backgroundStyleKey = backgroundStyleKey,
                position = position,
                createdAt = createdAt,
            ),
        )
        scheduleVersionDao.insert(
            ScheduleVersionEntity(
                habitId = habitId,
                effectiveFromDate = scheduleEffectiveFrom,
                targetNumerator = targetNumerator,
                targetDenominator = targetDenominator,
                targetValue = targetValue,
                targetKind = targetKind,
            ),
        )
        return habitId
    }

    /** Toggles a boolean habit's completion for [date]: adds it if missing, clears it if already completed. */
    suspend fun toggleCompletion(habitId: Long, date: LocalDate) {
        val existing = entryDao.entryOn(habitId, date)
        if (existing?.state == EntryState.COMPLETED) {
            entryDao.delete(existing)
            return
        }
        val now = Instant.now()
        entryDao.upsert(
            EntryEntity(
                habitId = habitId,
                date = date,
                state = EntryState.COMPLETED,
                tzOffsetMinutes = currentTzOffsetMinutes(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    /**
     * Adds [delta] to a numeric habit's running value for [date], marking the day COMPLETED
     * once it meets the schedule's target (at-least: value >= target; at-most: value <= target
     * — since the increment only grows the value, an at-most habit is "complete" for the day
     * the moment it's logged at all, and further increments push it back to IN_PROGRESS if
     * they exceed target).
     */
    suspend fun incrementNumericEntry(habitId: Long, date: LocalDate, delta: Double) {
        setNumericEntry(habitId, date, (entryDao.entryOn(habitId, date)?.numericValue ?: 0.0) + delta)
    }

    /** Sets a numeric habit's value for [date] outright, for manual entry rather than +/- taps. */
    suspend fun setNumericEntry(habitId: Long, date: LocalDate, value: Double) {
        val existing = entryDao.entryOn(habitId, date)
        val now = Instant.now()
        val newValue = value.coerceAtLeast(0.0)
        val schedule = scheduleVersionDao.versionEffectiveOn(habitId, date)
        val meetsTarget = when (schedule?.targetKind) {
            TargetKind.AT_LEAST -> schedule.targetValue != null && newValue >= schedule.targetValue
            TargetKind.AT_MOST -> schedule.targetValue != null && newValue <= schedule.targetValue
            null -> false
        }
        entryDao.upsert(
            EntryEntity(
                habitId = habitId,
                date = date,
                state = if (meetsTarget) EntryState.COMPLETED else EntryState.IN_PROGRESS,
                numericValue = newValue,
                tzOffsetMinutes = currentTzOffsetMinutes(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    /** Soft-delete: hides the habit from [observeActiveHabits] without touching its history. */
    suspend fun archiveHabit(habitId: Long) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(archivedAt = Instant.now()))
    }

    /**
     * Name/color/icon/unit are mutated in place. A schedule change (frequency or target) instead
     * inserts a new [ScheduleVersionEntity] effective today, so past compliance stays judged
     * against whatever schedule was actually in force at the time — same versioning [createHabit]
     * relies on. Habit type is intentionally not editable: it determines which entry fields
     * (boolean vs numeric) history already carries, and changing it after entries exist would
     * make that history ambiguous.
     */
    suspend fun updateHabit(
        habitId: Long,
        name: String,
        color: Int,
        iconKey: String,
        backgroundStyleKey: String?,
        unit: String?,
        targetNumerator: Int,
        targetDenominator: Int,
        targetValue: Double?,
        targetKind: TargetKind?,
    ) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(
            habit.copy(
                name = name,
                color = color,
                iconKey = iconKey,
                backgroundStyleKey = backgroundStyleKey,
                unit = unit,
            ),
        )

        val today = LocalDate.now()
        val current = scheduleVersionDao.versionEffectiveOn(habitId, today)
        val scheduleChanged = current == null ||
            current.targetNumerator != targetNumerator ||
            current.targetDenominator != targetDenominator ||
            current.targetValue != targetValue ||
            current.targetKind != targetKind
        if (scheduleChanged) {
            scheduleVersionDao.insert(
                ScheduleVersionEntity(
                    habitId = habitId,
                    effectiveFromDate = today,
                    targetNumerator = targetNumerator,
                    targetDenominator = targetDenominator,
                    targetValue = targetValue,
                    targetKind = targetKind,
                ),
            )
        }
    }

    suspend fun setSkipped(habitId: Long, date: LocalDate) {
        val existing = entryDao.entryOn(habitId, date)
        val now = Instant.now()
        entryDao.upsert(
            EntryEntity(
                habitId = habitId,
                date = date,
                state = EntryState.SKIPPED,
                tzOffsetMinutes = currentTzOffsetMinutes(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    /**
     * The score series for [habitId] between [startDate] and [endDate], under the schedule
     * that was in force on [endDate]. Pulls enough history before [startDate] to fill the
     * scoring window (see [dev.dhev.nines.scoring.computeScoreSeries]).
     */
    suspend fun scoreSeries(habitId: Long, startDate: LocalDate, endDate: LocalDate): List<ScorePoint> {
        val schedule = scheduleVersionDao.versionEffectiveOn(habitId, endDate) ?: return emptyList()
        val lookback = startDate.minusDays(schedule.targetDenominator * 2L)
        val entries = entryDao.entriesBetween(habitId, lookback, endDate)
        val completed = entries.filter { it.state == EntryState.COMPLETED }.map { it.date }.toSet()
        val skipped = entries.filter { it.state == EntryState.SKIPPED }.map { it.date }.toSet()
        val target = ScheduleTarget(schedule.targetNumerator, schedule.targetDenominator)
        return computeScoreSeries(target, completed, skipped, startDate, endDate)
    }

    private fun currentTzOffsetMinutes(): Int = OffsetDateTime.now().offset.totalSeconds / 60
}
