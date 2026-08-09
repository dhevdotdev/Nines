package dev.dhev.nines.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

enum class HabitType { BOOLEAN, NUMERICAL }

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val type: HabitType,
    val unit: String? = null,
    val color: Int,
    val iconKey: String,
    val position: Int,
    val archivedAt: Instant? = null,
    val createdAt: Instant,
)
