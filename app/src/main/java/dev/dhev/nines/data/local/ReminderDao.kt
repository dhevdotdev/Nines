package dev.dhev.nines.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.dhev.nines.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE habitId = :habitId")
    suspend fun get(habitId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE habitId = :habitId")
    fun observe(habitId: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders")
    suspend fun all(): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE habitId = :habitId")
    suspend fun delete(habitId: Long)
}
