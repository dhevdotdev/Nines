package dev.dhev.nines.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderPreferencesStore
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.entity.HabitType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate

class ReminderNotificationCoordinator(
    private val context: Context,
    private val habits: HabitRepository,
    private val reminders: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val preferences: ReminderPreferencesStore,
) : ReminderNotifications {
    @SuppressLint("MissingPermission")
    suspend fun deliver(habitId: Long) = withContext(Dispatchers.IO) {
        val habit = habits.habit(habitId) ?: return@withContext
        val reminder = reminders.get(habitId) ?: return@withContext
        if (habit.archivedAt != null) return@withContext
        if (reminder.snoozedUntil?.isAfter(Instant.now()) == true) return@withContext
        val date = reminder.activeDate ?: LocalDate.now()
        if (habits.isReminderResolved(habitId, date)) {
            reminders.resolve(habitId)
            scheduler.schedule(habitId, reminder.localTime)
            return@withContext
        }
        reminders.activate(habitId, date, Instant.now())
        scheduler.schedule(habitId, reminder.localTime)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return@withContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext
        }
        ensureChannel()
        val combine = preferences.preferencesFlow.first().combineSimultaneous
        val sameMinuteCount = if (combine) {
            reminders.all().count { other ->
                other.habitId != habitId &&
                    other.localTime == reminder.localTime &&
                    other.snoozedUntil?.isAfter(Instant.now()) != true &&
                    habits.habit(other.habitId)?.let { it.archivedAt == null } == true &&
                    !habits.isReminderResolved(other.habitId, other.activeDate ?: date)
            } + 1
        } else {
            1
        }
        val shouldCombine = combine && sameMinuteCount > 1
        NotificationManagerCompat.from(context).notify(
            if (shouldCombine) COMBINED_NOTIFICATION_ID else habitId.toInt(),
            if (shouldCombine) combinedNotification(sameMinuteCount) else notification(habitId, habit.name, habit.type),
        )
    }

    @SuppressLint("MissingPermission")
    override fun cancel(habitId: Long) {
        NotificationManagerCompat.from(context).cancel(habitId.toInt())
        NotificationManagerCompat.from(context).cancel(COMBINED_NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Habit reminders", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    private fun notification(habitId: Long, habitName: String, habitType: HabitType) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(habitName)
        .setContentText("Time for your habit")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.OPEN))
        .addAction(
            0,
            if (habitType == HabitType.NUMERICAL) "Log value" else "Mark done",
            ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.OPEN),
        )
        .addAction(0, "Not today", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.NOT_TODAY))
        .addAction(0, "Snooze 15m", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.SNOOZE_15))
        .addAction(0, "Snooze 1h", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.SNOOZE_1H))
        .addAction(0, "Snooze 3h", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.SNOOZE_3H))
        .addAction(0, "Snooze tomorrow", ReminderActionReceiver.pendingIntent(context, habitId, ReminderActionReceiver.SNOOZE_TOMORROW))
        .build()

    private fun combinedNotification(count: Int) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle("Habit reminders")
        .setContentText("$count habits are ready to review")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(ReminderActionReceiver.pendingIntent(context, 0L, ReminderActionReceiver.OPEN))
        .build()

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val COMBINED_NOTIFICATION_ID = 0x4E494E45
    }
}
