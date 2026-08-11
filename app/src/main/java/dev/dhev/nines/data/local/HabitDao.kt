package dev.dhev.nines.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.dhev.nines.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archivedAt IS NULL ORDER BY position ASC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    // Most recently archived first - what you just swiped away is what you're most likely to
    // come back looking for.
    @Query("SELECT * FROM habits WHERE archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    fun observeArchivedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getById(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun observeById(habitId: Long): Flow<HabitEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM habits")
    suspend fun nextPosition(): Int

    @Query("UPDATE habits SET position = :position WHERE id = :habitId")
    suspend fun updatePosition(habitId: Long, position: Int)
}
