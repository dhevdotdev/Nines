package dev.dhev.nines

import android.app.Application
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.di.appModule
import dev.dhev.nines.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

open class NinesApplication : Application() {
    /** Extra Koin modules for a build variant. Debug adds demo-data tooling. */
    protected open fun variantModules(): List<Module> = emptyList()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NinesApplication)
            modules(listOf(appModule) + variantModules())
        }
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = GlobalContext.get().get<ReminderRepository>()
            val habits = GlobalContext.get().get<HabitRepository>()
            val scheduler = GlobalContext.get().get<ReminderScheduler>()
            reminders.all().forEach { reminder ->
                if (habits.habit(reminder.habitId)?.archivedAt == null) {
                    scheduler.schedule(reminder.habitId, reminder.localTime)
                }
            }
        }
    }
}
