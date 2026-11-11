package dev.dhev.nines.reminders

import java.time.LocalTime

internal class RecordingAlarms : ReminderAlarms {
    val scheduled = mutableListOf<Pair<Long, LocalTime>>()
    val cancelled = mutableListOf<Long>()

    override fun schedule(
        habitId: Long,
        localTime: LocalTime,
    ) {
        scheduled += habitId to localTime
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
