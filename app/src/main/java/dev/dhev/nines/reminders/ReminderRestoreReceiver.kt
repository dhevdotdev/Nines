package dev.dhev.nines.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class ReminderRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = GlobalContext.get().get<ReminderRepository>()
                val habits = GlobalContext.get().get<HabitRepository>()
                val scheduler = GlobalContext.get().get<ReminderScheduler>()
                reminders.all().forEach { reminder ->
                    if (habits.habit(reminder.habitId)?.archivedAt == null) {
                        scheduler.schedule(reminder.habitId, reminder.localTime)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
