package dev.dhev.nines.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/** Delivery implementation is added with the notification coordinator; this receiver owns the alarm boundary. */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val habitId = intent.getLongExtra(ReminderScheduler.HABIT_ID, -1)
        if (habitId < 0) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                GlobalContext.get().get<ReminderNotificationCoordinator>().deliver(habitId)
            } finally {
                pending.finish()
            }
        }
    }
}
