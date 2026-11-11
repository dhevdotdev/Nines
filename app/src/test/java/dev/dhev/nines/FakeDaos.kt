package dev.dhev.nines

import dev.dhev.nines.data.local.EntryDao
import dev.dhev.nines.data.local.HabitDao
import dev.dhev.nines.data.local.ReminderDao
import dev.dhev.nines.data.local.ScheduleVersionDao
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.ReminderEntity
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** In-memory Room DAOs shared by the JVM tests. */
internal class FakeHabitDao(
    private val habit: MutableStateFlow<HabitEntity?>,
) : HabitDao {
    override fun observeActiveHabits(): Flow<List<HabitEntity>> = habit.value?.let { MutableStateFlow(listOf(it)) } ?: MutableStateFlow(emptyList())

    override fun observeArchivedHabits(): Flow<List<HabitEntity>> = MutableStateFlow(emptyList())

    override suspend fun getById(habitId: Long): HabitEntity? = habit.value?.takeIf { it.id == habitId }

    override fun observeById(habitId: Long): Flow<HabitEntity?> = if (habit.value?.id ==
        habitId
    ) {
        habit
    } else {
        flowOf(null)
    }

    override suspend fun insert(habit: HabitEntity): Long = habit.id

    override suspend fun update(habit: HabitEntity) {
        this.habit.value = habit
    }

    override suspend fun nextPosition(): Int = 0

    override suspend fun updatePosition(
        habitId: Long,
        position: Int,
    ) = Unit
}

internal class FakeScheduleDao(
    private val schedules: MutableStateFlow<List<ScheduleVersionEntity>>,
) : ScheduleVersionDao {
    override suspend fun insert(scheduleVersion: ScheduleVersionEntity): Long {
        schedules.value = schedules.value + scheduleVersion
        return scheduleVersion.id
    }

    override fun observeVersions(habitId: Long): Flow<List<ScheduleVersionEntity>> = schedules.map { values -> values.filter { it.habitId == habitId } }

    override suspend fun getVersions(habitId: Long): List<ScheduleVersionEntity> = schedules.value.filter {
        it.habitId ==
            habitId
    }

    override suspend fun versionEffectiveOn(
        habitId: Long,
        date: LocalDate,
    ): ScheduleVersionEntity? = schedules.value
        .filter { it.habitId == habitId && !it.effectiveFromDate.isAfter(date) }
        .maxWithOrNull(compareBy<ScheduleVersionEntity> { it.effectiveFromDate }.thenBy { it.id })
}

internal class FakeEntryDao(
    val entries: MutableStateFlow<List<EntryEntity>>,
) : EntryDao {
    override suspend fun upsert(entry: EntryEntity) {
        entries.value = entries.value.filterNot { it.habitId == entry.habitId && it.date == entry.date } + entry
    }

    override suspend fun delete(entry: EntryEntity) {
        entries.value = entries.value - entry
    }

    override fun observeEntries(habitId: Long): Flow<List<EntryEntity>> = entries.map { values -> values.filter { it.habitId == habitId } }

    override suspend fun entriesBetween(
        habitId: Long,
        from: LocalDate,
        to: LocalDate,
    ): List<EntryEntity> = entries.value.filter { it.habitId == habitId && !it.date.isBefore(from) && !it.date.isAfter(to) }

    override suspend fun entryOn(
        habitId: Long,
        date: LocalDate,
    ): EntryEntity? = entries.value.firstOrNull {
        it.habitId ==
            habitId &&
            it.date == date
    }
}

internal class FakeReminderDao(
    initial: List<ReminderEntity> = emptyList(),
) : ReminderDao {
    private val reminders = MutableStateFlow(initial)

    override suspend fun upsert(reminder: ReminderEntity) {
        reminders.value = reminders.value.filterNot { it.habitId == reminder.habitId } + reminder
    }

    override suspend fun get(habitId: Long): ReminderEntity? = reminders.value.firstOrNull { it.habitId == habitId }

    override fun observe(habitId: Long): Flow<ReminderEntity?> = reminders.map { list -> list.firstOrNull { it.habitId == habitId } }

    override suspend fun all(): List<ReminderEntity> = reminders.value

    override suspend fun delete(habitId: Long) {
        reminders.value = reminders.value.filterNot { it.habitId == habitId }
    }
}
