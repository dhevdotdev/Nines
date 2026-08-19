package dev.dhev.nines.di

import androidx.room.Room
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.AppearancePreferencesStore
import dev.dhev.nines.data.appearancePreferencesStore
import dev.dhev.nines.data.OnboardingCompletionStore
import dev.dhev.nines.data.onboardingCompletionStore
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.NinesDatabase
import dev.dhev.nines.ui.habits.HabitDetailViewModel
import dev.dhev.nines.ui.habits.HabitsViewModel
import dev.dhev.nines.ui.settings.ArchivedHabitsViewModel
import dev.dhev.nines.ui.settings.AppearanceViewModel
import dev.dhev.nines.reminders.ReminderScheduler
import dev.dhev.nines.reminders.ReminderNotificationCoordinator
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

@Suppress("DEPRECATION") // viewModelOf: Koin's flagged replacement package has no such function in 4.2.2 yet
val appModule = module {
    single<AppearancePreferencesStore> { androidContext().appearancePreferencesStore() }
    single<OnboardingCompletionStore> { androidContext().onboardingCompletionStore() }
    single {
        Room.databaseBuilder(androidContext(), NinesDatabase::class.java, "nines.db")
            // Pre-release: no installed base to migrate yet. Replace with real Migrations before shipping.
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<NinesDatabase>().habitDao() }
    single { get<NinesDatabase>().scheduleVersionDao() }
    single { get<NinesDatabase>().entryDao() }
    single { get<NinesDatabase>().reminderDao() }
    single { HabitRepository(get(), get(), get()) }
    single { ReminderRepository(get()) }
    single { ReminderScheduler(androidContext()) }
    single { ReminderNotificationCoordinator(androidContext(), get(), get(), get()) }

    viewModelOf(::HabitsViewModel)
    viewModelOf(::HabitDetailViewModel)
    viewModelOf(::ArchivedHabitsViewModel)
    viewModelOf(::AppearanceViewModel)
}
