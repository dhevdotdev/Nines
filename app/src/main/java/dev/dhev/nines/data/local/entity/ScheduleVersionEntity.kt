package dev.dhev.nines.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class TargetKind { AT_LEAST, AT_MOST }

/**
 * A habit's target, versioned by the date it took effect. Unlike a single mutable target on
 * the habit row, this lets a frequency change apply only going forward, so past compliance is
 * always judged against the schedule that was actually in force at the time.
 */
@Entity(
    tableName = "schedule_versions",
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
data class ScheduleVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val effectiveFromDate: LocalDate,
    val targetNumerator: Int,
    val targetDenominator: Int,
    val targetValue: Double? = null,
    val targetKind: TargetKind? = null,
)
