package dev.dhev.nines.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** Durable configuration and unresolved state for one optional Habit Reminder. */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ReminderEntity(
    @PrimaryKey val habitId: Long,
    val localTime: LocalTime,
    val activeDate: LocalDate? = null,
    val snoozedUntil: Instant? = null,
    val lastAlertedAt: Instant? = null,
)
