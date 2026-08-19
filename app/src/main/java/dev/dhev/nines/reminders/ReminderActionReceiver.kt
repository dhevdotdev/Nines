package dev.dhev.nines.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.dhev.nines.MainActivity
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.time.Instant
import java.time.LocalDate

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(ReminderScheduler.HABIT_ID, -1)
        if (habitId < 0) return
        if (intent.action == OPEN) {
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = GlobalContext.get().get<HabitRepository>()
                val reminders = GlobalContext.get().get<ReminderRepository>()
                val notifications = GlobalContext.get().get<ReminderNotificationCoordinator>()
                val activeDate = reminders.get(habitId)?.activeDate ?: LocalDate.now()
                when (intent.action) {
                    MARK_DONE -> habits.toggleCompletion(habitId, activeDate)
                    NOT_TODAY -> habits.setSkipped(habitId, activeDate)
                    SNOOZE_15 -> reminders.snooze(habitId, Instant.now().plusSeconds(15 * 60))
                }
                if (intent.action == SNOOZE_15) {
                    GlobalContext.get().get<ReminderScheduler>().schedule(habitId, java.time.LocalTime.now().plusMinutes(15))
                } else {
                    reminders.resolve(habitId)
                }
                notifications.cancel(habitId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val OPEN = "dev.dhev.nines.reminders.OPEN"
        const val MARK_DONE = "dev.dhev.nines.reminders.MARK_DONE"
        const val NOT_TODAY = "dev.dhev.nines.reminders.NOT_TODAY"
        const val SNOOZE_15 = "dev.dhev.nines.reminders.SNOOZE_15"

        fun pendingIntent(context: Context, habitId: Long, action: String): PendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt() * 10 + action.hashCode(),
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(action)
                .putExtra(ReminderScheduler.HABIT_ID, habitId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
