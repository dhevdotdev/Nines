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
import java.time.Duration

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(ReminderScheduler.HABIT_ID, -1)
        if (habitId < 0) return
        if (intent.action == OPEN) {
            context.startActivity(
                Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .apply {
                        if (habitId > 0L) {
                            putExtra(MainActivity.OPEN_HABIT_ID, habitId)
                            putExtra(MainActivity.OPEN_VALUE_EDITOR, true)
                        }
                    },
            )
            return
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = GlobalContext.get().get<HabitRepository>()
                val reminders = GlobalContext.get().get<ReminderRepository>()
                val notifications = GlobalContext.get().get<ReminderNotificationCoordinator>()
                val reminder = reminders.get(habitId) ?: return@launch
                val activeDate = reminder.activeDate ?: LocalDate.now()
                when (intent.action) {
                    MARK_DONE -> habits.toggleCompletion(habitId, activeDate)
                    NOT_TODAY -> habits.setSkipped(habitId, activeDate)
                    SNOOZE_15 -> snooze(reminders, habitId, Duration.ofMinutes(15))
                    SNOOZE_1H -> snooze(reminders, habitId, Duration.ofHours(1))
                    SNOOZE_3H -> snooze(reminders, habitId, Duration.ofHours(3))
                    SNOOZE_TOMORROW -> {
                        val tomorrow = LocalDate.now().plusDays(1)
                            .atTime(reminder.localTime)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toInstant()
                        reminders.snooze(habitId, tomorrow)
                        GlobalContext.get().get<ReminderScheduler>().scheduleAt(habitId, tomorrow)
                    }
                }
                if (intent.action in setOf(SNOOZE_15, SNOOZE_1H, SNOOZE_3H)) {
                    val until = Instant.now().plus(snoozeDuration(intent.action ?: SNOOZE_15))
                    GlobalContext.get().get<ReminderScheduler>().scheduleAt(habitId, until)
                } else if (intent.action != SNOOZE_TOMORROW) {
                    reminders.resolve(habitId)
                    GlobalContext.get().get<ReminderScheduler>().schedule(habitId, reminder.localTime)
                }
                notifications.cancel(habitId)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun snooze(reminders: ReminderRepository, habitId: Long, duration: Duration) {
        reminders.snooze(habitId, Instant.now().plus(duration))
    }

    private fun snoozeDuration(action: String): Duration = when (action) {
        SNOOZE_1H -> Duration.ofHours(1)
        SNOOZE_3H -> Duration.ofHours(3)
        else -> Duration.ofMinutes(15)
    }

    companion object {
        const val OPEN = "dev.dhev.nines.reminders.OPEN"
        const val MARK_DONE = "dev.dhev.nines.reminders.MARK_DONE"
        const val NOT_TODAY = "dev.dhev.nines.reminders.NOT_TODAY"
        const val SNOOZE_15 = "dev.dhev.nines.reminders.SNOOZE_15"
        const val SNOOZE_1H = "dev.dhev.nines.reminders.SNOOZE_1H"
        const val SNOOZE_3H = "dev.dhev.nines.reminders.SNOOZE_3H"
        const val SNOOZE_TOMORROW = "dev.dhev.nines.reminders.SNOOZE_TOMORROW"

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
