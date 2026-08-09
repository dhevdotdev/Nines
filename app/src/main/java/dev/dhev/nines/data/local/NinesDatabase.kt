package dev.dhev.nines.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity

@Database(
    entities = [HabitEntity::class, ScheduleVersionEntity::class, EntryEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class NinesDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun scheduleVersionDao(): ScheduleVersionDao
    abstract fun entryDao(): EntryDao
}
