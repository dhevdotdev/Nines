package dev.dhev.nines.data

import dev.dhev.nines.data.local.ReminderDao
import dev.dhev.nines.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** Owns optional Reminder configuration separately from Habit Entries and derived compliance. */
class ReminderRepository(private val reminderDao: ReminderDao) {
    fun observe(habitId: Long): Flow<ReminderEntity?> = reminderDao.observe(habitId)

    suspend fun get(habitId: Long): ReminderEntity? = reminderDao.get(habitId)

    suspend fun all(): List<ReminderEntity> = reminderDao.all()

    suspend fun configure(habitId: Long, localTime: LocalTime) {
        val existing = reminderDao.get(habitId)
        reminderDao.upsert((existing ?: ReminderEntity(habitId, localTime)).copy(localTime = localTime))
    }

    suspend fun remove(habitId: Long) = reminderDao.delete(habitId)

    suspend fun activate(habitId: Long, date: LocalDate, alertedAt: Instant) {
        val reminder = reminderDao.get(habitId) ?: return
        reminderDao.upsert(reminder.copy(activeDate = date, snoozedUntil = null, lastAlertedAt = alertedAt))
    }

    suspend fun snooze(habitId: Long, until: Instant) {
        val reminder = reminderDao.get(habitId) ?: return
        reminderDao.upsert(reminder.copy(snoozedUntil = until, lastAlertedAt = null))
    }

    suspend fun resolve(habitId: Long) {
        val reminder = reminderDao.get(habitId) ?: return
        reminderDao.upsert(reminder.copy(activeDate = null, snoozedUntil = null, lastAlertedAt = null))
    }
}
