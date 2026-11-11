package dev.dhev.nines.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.dhev.nines.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.time.Duration

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
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
        val action = reminderActionFor(intent.action) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                GlobalContext.get().get<HabitReminders>().respond(habitId, action)
            } finally {
                pending.finish()
            }
        }
    }

    private fun reminderActionFor(intentAction: String?): ReminderAction? = when (intentAction) {
        MARK_DONE -> ReminderAction.MarkDone
        NOT_TODAY -> ReminderAction.NotToday
        SNOOZE_15 -> ReminderAction.Snooze(Duration.ofMinutes(15))
        SNOOZE_1H -> ReminderAction.Snooze(Duration.ofHours(1))
        SNOOZE_3H -> ReminderAction.Snooze(Duration.ofHours(3))
        SNOOZE_TOMORROW -> ReminderAction.SnoozeUntilTomorrow
        else -> null
    }

    companion object {
        const val OPEN = "dev.dhev.nines.reminders.OPEN"
        const val MARK_DONE = "dev.dhev.nines.reminders.MARK_DONE"
        const val NOT_TODAY = "dev.dhev.nines.reminders.NOT_TODAY"
        const val SNOOZE_15 = "dev.dhev.nines.reminders.SNOOZE_15"
        const val SNOOZE_1H = "dev.dhev.nines.reminders.SNOOZE_1H"
        const val SNOOZE_3H = "dev.dhev.nines.reminders.SNOOZE_3H"
        const val SNOOZE_TOMORROW = "dev.dhev.nines.reminders.SNOOZE_TOMORROW"

        fun pendingIntent(
            context: Context,
            habitId: Long,
            action: String,
        ): PendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt() * 10 + action.hashCode(),
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(action)
                .putExtra(ReminderScheduler.HABIT_ID, habitId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
