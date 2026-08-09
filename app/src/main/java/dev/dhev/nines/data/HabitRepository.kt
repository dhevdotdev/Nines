package dev.dhev.nines.data

import dev.dhev.nines.data.local.EntryDao
import dev.dhev.nines.data.local.HabitDao
import dev.dhev.nines.data.local.ScheduleVersionDao
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
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

    fun observeEntries(habitId: Long): Flow<List<EntryEntity>> = entryDao.observeEntries(habitId)

    suspend fun createHabit(
        name: String,
        type: HabitType,
        color: Int,
        targetNumerator: Int = 1,
        targetDenominator: Int = 1,
        unit: String? = null,
    ): Long {
        val position = habitDao.nextPosition()
        val habitId = habitDao.insert(
            HabitEntity(
                name = name,
                type = type,
                unit = unit,
                color = color,
                position = position,
                createdAt = Instant.now(),
            ),
        )
        scheduleVersionDao.insert(
            ScheduleVersionEntity(
                habitId = habitId,
                effectiveFromDate = LocalDate.now(),
                targetNumerator = targetNumerator,
                targetDenominator = targetDenominator,
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
