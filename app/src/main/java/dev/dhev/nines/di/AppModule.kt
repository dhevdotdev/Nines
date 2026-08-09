package dev.dhev.nines.di

import androidx.room.Room
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.local.NinesDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), NinesDatabase::class.java, "nines.db").build()
    }
    single { get<NinesDatabase>().habitDao() }
    single { get<NinesDatabase>().scheduleVersionDao() }
    single { get<NinesDatabase>().entryDao() }
    single { HabitRepository(get(), get(), get()) }
}
