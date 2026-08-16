package dev.dhev.nines.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.dhev.nines.data.local.entity.ScheduleVersionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ScheduleVersionDao {
    @Insert
    suspend fun insert(scheduleVersion: ScheduleVersionEntity): Long

    @Query("SELECT * FROM schedule_versions WHERE habitId = :habitId ORDER BY effectiveFromDate ASC, id ASC")
    fun observeVersions(habitId: Long): Flow<List<ScheduleVersionEntity>>

    @Query("SELECT * FROM schedule_versions WHERE habitId = :habitId ORDER BY effectiveFromDate ASC, id ASC")
    suspend fun getVersions(habitId: Long): List<ScheduleVersionEntity>

    /** The schedule that was actually in force on [date] — the most recent version at or before it. */
    @Query(
        """
        SELECT * FROM schedule_versions
        WHERE habitId = :habitId AND effectiveFromDate <= :date
        ORDER BY effectiveFromDate DESC, id DESC
        LIMIT 1
        """,
    )
    suspend fun versionEffectiveOn(habitId: Long, date: LocalDate): ScheduleVersionEntity?
}
