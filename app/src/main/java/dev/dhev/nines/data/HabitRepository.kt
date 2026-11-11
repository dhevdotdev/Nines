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
import dev.dhev.nines.scoring.FrequencyProgressInput
import dev.dhev.nines.scoring.FrequencyTargetKind
import dev.dhev.nines.scoring.HabitTimeline
import dev.dhev.nines.scoring.HabitTimelineData
import dev.dhev.nines.scoring.HabitTimelineInput
import dev.dhev.nines.scoring.ScheduleTarget
import dev.dhev.nines.scoring.ScorePoint
import dev.dhev.nines.scoring.TimelineEntry
import dev.dhev.nines.scoring.TimelineEntryState
import dev.dhev.nines.scoring.TimelineHabitKind
import dev.dhev.nines.scoring.TimelineSchedule
import dev.dhev.nines.scoring.buildHabitTimeline
import dev.dhev.nines.scoring.computeFrequencyProgress
import dev.dhev.nines.scoring.computeScheduleAwareScoreSeries
import dev.dhev.nines.scoring.isReminderResolvedOn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

class HabitRepository(
    private val habitDao: HabitDao,
    private val scheduleVersionDao: ScheduleVersionDao,
    private val entryDao: EntryDao,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    data class HabitTimelineSnapshot(
        val habit: HabitEntity,
        val timeline: HabitTimeline,
        val entriesThroughAsOfDate: List<TimelineEntry>,
    )

    fun observeActiveHabits(): Flow<List<HabitEntity>> = habitDao.observeActiveHabits()

    fun observeArchivedHabits(): Flow<List<HabitEntity>> = habitDao.observeArchivedHabits()

    fun observeHabit(habitId: Long): Flow<HabitEntity?> = habitDao.observeById(habitId)

    suspend fun habit(habitId: Long): HabitEntity? = habitDao.getById(habitId)

    suspend fun entryOn(habitId: Long, date: LocalDate): EntryEntity? = entryDao.entryOn(habitId, date)

    /** True when the Schedule in force says this habit no longer needs a reminder for [date]. */
    suspend fun isReminderResolved(habitId: Long, date: LocalDate): Boolean {
        val habit = habitDao.getById(habitId) ?: return true
        val schedules = scheduleVersionDao.getVersions(habitId).map { it.toTimelineSchedule() }
        val longestWindow = schedules.maxOfOrNull { it.target.denominator } ?: 1
        val entries = entryDao.entriesBetween(habitId, date.minusDays(longestWindow - 1L), date)
        return isReminderResolvedOn(
            kind = habit.type.toTimelineKind(),
            date = date,
            schedules = schedules,
            entries = entries.map { it.toTimelineEntry() },
        )
    }

    fun observeEntries(habitId: Long): Flow<List<EntryEntity>> = entryDao.observeEntries(habitId)

    fun observeScheduleVersions(habitId: Long): Flow<List<ScheduleVersionEntity>> = scheduleVersionDao.observeVersions(habitId)

    fun observeHabitTimelineData(habitId: Long): Flow<HabitTimelineData> = observeEntries(habitId).combine(observeScheduleVersions(habitId)) { entries, schedules ->
        HabitTimelineData(
            entries = entries.map { it.toTimelineEntry() },
            schedules = schedules.map { it.toTimelineSchedule() },
        )
    }

    /** Observes the complete date-bounded read model and the metadata needed by screen adapters. */
    fun observeHabitSnapshot(
        habitId: Long,
        selectedDate: Flow<LocalDate>,
        asOfDate: LocalDate,
    ): Flow<HabitTimelineSnapshot?> = combine(
        observeHabit(habitId),
        observeHabitTimelineData(habitId),
        selectedDate,
    ) { habit, timelineData, date ->
        habit?.let {
            val createdDate = it.createdAt.atZone(clock.zone).toLocalDate()
            val effectiveSelectedDate = minOf(date, asOfDate)
            val boundedEntries = timelineData.entries
                .filter { entry ->
                    !entry.date.isBefore(createdDate) && !entry.date.isAfter(asOfDate)
                }
                .sortedBy { it.date }
            HabitTimelineSnapshot(
                habit = it,
                timeline = buildHabitTimeline(
                    HabitTimelineInput(
                        kind = it.type.toTimelineKind(),
                        createdDate = createdDate,
                        selectedDate = effectiveSelectedDate,
                        entries = boundedEntries,
                        schedules = timelineData.schedules,
                    ),
                ),
                entriesThroughAsOfDate = boundedEntries,
            )
        }
    }

    suspend fun scheduleEffectiveOn(habitId: Long, date: LocalDate): ScheduleVersionEntity? = scheduleVersionDao.versionEffectiveOn(habitId, date)

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
                createdAt = clock.instant(),
            ),
        )
        scheduleVersionDao.insert(
            ScheduleVersionEntity(
                habitId = habitId,
                effectiveFromDate = LocalDate.now(clock),
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
        val now = clock.instant()
        entryDao.upsert(
            EntryEntity(
                habitId = habitId,
                date = date,
                state = EntryState.COMPLETED,
                notes = existing?.notes,
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
        val now = clock.instant()
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
                notes = existing?.notes,
                tzOffsetMinutes = currentTzOffsetMinutes(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    /** Removes the numeric Entry for [date], restoring the domain's absent-entry state. */
    suspend fun clearNumericEntry(habitId: Long, date: LocalDate) {
        entryDao.entryOn(habitId, date)?.let { entryDao.delete(it) }
    }

    /** Soft-delete: hides the habit from [observeActiveHabits] without touching its history. */
    suspend fun archiveHabit(habitId: Long) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(archivedAt = clock.instant()))
    }

    /** Undoes [archiveHabit]. Appended to the end of the active list (via [HabitDao.nextPosition])
     * rather than restored to its old position, which may now belong to a different habit. */
    suspend fun restoreHabit(habitId: Long) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(archivedAt = null, position = habitDao.nextPosition()))
    }

    /** Renumbers every habit's position to match [orderedIds] - a full renumber rather than
     * shifting a range is simpler to reason about and cheap at the scale a habit list ever is. */
    suspend fun reorderHabits(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, habitId -> habitDao.updatePosition(habitId, index) }
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

        val today = LocalDate.now(clock)
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
        val now = clock.instant()
        entryDao.upsert(
            EntryEntity(
                habitId = habitId,
                date = date,
                state = EntryState.SKIPPED,
                notes = existing?.notes,
                tzOffsetMinutes = currentTzOffsetMinutes(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    /** The score series for [habitId], applying each schedule version only from its effective date.
     * Pulls enough history before [startDate] to fill the largest schedule window in the range. */
    suspend fun scoreSeries(habitId: Long, startDate: LocalDate, endDate: LocalDate): List<ScorePoint> {
        val schedules = scheduleVersionDao.getVersions(habitId)
            .filter { !it.effectiveFromDate.isAfter(endDate) }
        if (schedules.isEmpty()) return emptyList()
        val lookbackDays = schedules.maxOf { it.targetDenominator * 2L }
        val lookback = startDate.minusDays(lookbackDays)
        val entries = entryDao.entriesBetween(habitId, lookback, endDate)
        val completed = entries.filter { it.state == EntryState.COMPLETED }.map { it.date }.toSet()
        val skipped = entries.filter { it.state == EntryState.SKIPPED }.map { it.date }.toSet()
        return computeScheduleAwareScoreSeries(
            scheduleForDate = { date ->
                schedules.lastOrNull { !it.effectiveFromDate.isAfter(date) }?.let {
                    ScheduleTarget(it.targetNumerator, it.targetDenominator)
                }
            },
            completedDates = completed,
            skippedDates = skipped,
            startDate = startDate,
            endDate = endDate,
        )
    }

    private fun currentTzOffsetMinutes(): Int = OffsetDateTime.now(clock).offset.totalSeconds / 60
}
