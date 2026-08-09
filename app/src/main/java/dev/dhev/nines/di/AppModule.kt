package dev.dhev.nines.di

import androidx.room.Room
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.NinesDatabase
import dev.dhev.nines.ui.habits.HabitsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

@Suppress("DEPRECATION") // viewModelOf: Koin's flagged replacement package has no such function in 4.2.2 yet
val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), NinesDatabase::class.java, "nines.db")
            // Pre-release: no installed base to migrate yet. Replace with real Migrations before shipping.
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<NinesDatabase>().habitDao() }
    single { get<NinesDatabase>().scheduleVersionDao() }
    single { get<NinesDatabase>().entryDao() }
    single { HabitRepository(get(), get(), get()) }

    viewModelOf(::HabitsViewModel)
}
