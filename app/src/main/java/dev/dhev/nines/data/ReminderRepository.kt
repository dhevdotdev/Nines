package dev.dhev.nines.data

import dev.dhev.nines.data.local.ReminderDao
import dev.dhev.nines.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

/** Owns optional Reminder configuration separately from Habit Entries and derived compliance. */
class ReminderRepository(private val reminderDao: ReminderDao) {
    fun observe(habitId: Long): Flow<ReminderEntity?> = reminderDao.observe(habitId)

    suspend fun configure(habitId: Long, localTime: LocalTime) {
        val existing = reminderDao.get(habitId)
        reminderDao.upsert((existing ?: ReminderEntity(habitId, localTime)).copy(localTime = localTime))
    }

    suspend fun remove(habitId: Long) = reminderDao.delete(habitId)
}
