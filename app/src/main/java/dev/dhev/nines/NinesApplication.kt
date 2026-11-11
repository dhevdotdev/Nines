package dev.dhev.nines

import android.app.Application
import dev.dhev.nines.di.appModule
import dev.dhev.nines.reminders.HabitReminders
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
            GlobalContext.get().get<HabitReminders>().rescheduleAll()
        }
    }
}
