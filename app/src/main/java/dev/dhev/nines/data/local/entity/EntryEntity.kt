package dev.dhev.nines.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.Instant
import java.time.LocalDate

enum class EntryState { COMPLETED, SKIPPED }

/**
 * One day's record for a habit. A missed day has no row at all — absence is the miss.
 * [tzOffsetMinutes] records the offset the entry was created under, for auditability; the
 * date itself stays timezone-naive and is never rewritten by a later timezone change.
 */
@Entity(
    tableName = "entries",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId")],
)
data class EntryEntity(
    val habitId: Long,
    val date: LocalDate,
    val state: EntryState,
    val numericValue: Double? = null,
    val notes: String? = null,
    val tzOffsetMinutes: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
