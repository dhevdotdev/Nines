package dev.dhev.nines.reminders

import java.time.Instant
import java.time.LocalTime

internal class RecordingAlarms : ReminderAlarms {
    val scheduled = mutableListOf<Pair<Long, LocalTime>>()
    val scheduledAt = mutableListOf<Pair<Long, Instant>>()
    val cancelled = mutableListOf<Long>()

    override fun schedule(
        habitId: Long,
        localTime: LocalTime,
    ) {
        scheduled += habitId to localTime
    }

    override fun scheduleAt(
        habitId: Long,
        instant: Instant,
    ) {
        scheduledAt += habitId to instant
    }

    override fun cancel(habitId: Long) {
        cancelled += habitId
    }
}

internal class RecordingNotifications : ReminderNotifications {
    val cancelled = mutableListOf<Long>()

    override fun cancel(habitId: Long) {
        cancelled += habitId
    }
}
