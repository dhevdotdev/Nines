package dev.dhev.nines.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.entity.EntryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate

class ReminderNotificationCoordinator(
    private val context: Context,
    private val habits: HabitRepository,
    private val reminders: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend fun deliver(habitId: Long) = withContext(Dispatchers.IO) {
        val habit = habits.habit(habitId) ?: return@withContext
        val reminder = reminders.get(habitId) ?: return@withContext
        if (habit.archivedAt != null) return@withContext
        if (reminder.snoozedUntil?.isAfter(Instant.now()) == true) return@withContext
        val date = reminder.activeDate ?: LocalDate.now()
        val entry = habits.entryOn(habitId, date)
        if (entry?.state == EntryState.COMPLETED || entry?.state == EntryState.SKIPPED) return@withContext
        reminders.activate(habitId, date, Instant.now())
        scheduler.schedule(habitId, reminder.localTime)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return@withContext
        ensureChannel()
        NotificationManagerCompat.from(context).notify(habitId.toInt(), notification(habitId, habit.name))
    }

    fun cancel(habitId: Long) = NotificationManagerCompat.from(context).cancel(habitId.toInt())

    private fun ensureChannel() {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Habit reminders", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    private fun notification(habitId: Long, habitName: String) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(habitName)
        .setContentText("Time for your habit")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.OPEN))
        .addAction(0, "Mark done", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.MARK_DONE))
        .addAction(0, "Not today", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.NOT_TODAY))
        .addAction(0, "Snooze 15m", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.SNOOZE_15))
        .build()

    companion object { const val CHANNEL_ID = "habit_reminders" }
}
