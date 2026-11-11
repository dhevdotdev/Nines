package dev.dhev.nines.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.dhev.nines.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: EntryEntity)

    @Delete
    suspend fun delete(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE habitId = :habitId ORDER BY date ASC")
    fun observeEntries(habitId: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE habitId = :habitId AND date BETWEEN :from AND :to ORDER BY date ASC")
    suspend fun entriesBetween(
        habitId: Long,
        from: LocalDate,
        to: LocalDate,
    ): List<EntryEntity>

    @Query("SELECT * FROM entries WHERE habitId = :habitId AND date = :date")
    suspend fun entryOn(
        habitId: Long,
        date: LocalDate,
    ): EntryEntity?
}
