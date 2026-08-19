package dev.dhev.nines.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Best-effort local-time scheduling; delivery is intentionally not an exact-alarm use case. */
class ReminderScheduler(private val context: Context) {
    fun schedule(habitId: Long, localTime: LocalTime) {
        val now = LocalDateTime.now()
        val next = now.toLocalDate().atTime(localTime).let { candidate ->
            if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
        }
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(habitId),
        )
    }

    fun cancel(habitId: Long) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(habitId))
    }

    private fun pendingIntent(habitId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        habitId.toInt(),
        Intent(context, ReminderAlarmReceiver::class.java).putExtra(HABIT_ID, habitId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object { const val HABIT_ID = "habit_id" }
}
