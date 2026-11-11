package dev.dhev.nines.reminders

import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/** Schedules a Habit's daily reminder alarm. The system adapter is [ReminderScheduler]. */
interface ReminderAlarms {
    fun schedule(
        habitId: Long,
        localTime: LocalTime,
    )

    /** Fires once at an already-resolved [instant], used by snooze. */
    fun scheduleAt(
        habitId: Long,
        instant: Instant,
    )

    fun cancel(habitId: Long)
}

/** What the user can do to a posted reminder without opening the app. */
sealed interface ReminderAction {
    data object MarkDone : ReminderAction

    data object NotToday : ReminderAction

    data class Snooze(
        val duration: Duration,
    ) : ReminderAction

    data object SnoozeUntilTomorrow : ReminderAction
}

/** Removes a Habit's posted reminder notification. The system adapter is [ReminderNotificationCoordinator]. */
interface ReminderNotifications {
    fun cancel(habitId: Long)
}

/**
 * Everything the app does to a Habit's reminder when the Habit changes: configure or clear it,
 * pause it on archive, bring it back on restore, and rebuild every alarm after a reboot or a
 * process restart. Callers say what happened to the Habit; this module keeps the stored
 * configuration, the alarm and the notification in step.
 */
class HabitReminders(
    private val configuration: ReminderRepository,
    private val habits: HabitRepository,
    private val alarms: ReminderAlarms,
    private val notifications: ReminderNotifications,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun observeTime(habitId: Long): Flow<LocalTime?> = configuration.observe(habitId).map { it?.localTime }

    /** Sets the daily reminder time, or clears the reminder when [time] is null. */
    suspend fun configure(
        habitId: Long,
        time: LocalTime?,
    ) {
        if (time != null) {
            configuration.configure(habitId, time)
            alarms.schedule(habitId, time)
        } else {
            configuration.remove(habitId)
            alarms.cancel(habitId)
        }
    }

    /** An archived Habit stops alerting but keeps its configuration for [onRestored]. */
    fun onArchived(habitId: Long) {
        alarms.cancel(habitId)
        notifications.cancel(habitId)
    }

    suspend fun onRestored(habitId: Long) {
        configuration.get(habitId)?.let { alarms.schedule(habitId, it.localTime) }
    }

    /** Re-arms the alarm of every configured reminder whose Habit is still active. */
    suspend fun rescheduleAll() {
        configuration.all().forEach { reminder ->
            if (habits.habit(reminder.habitId)?.archivedAt == null) {
                alarms.schedule(reminder.habitId, reminder.localTime)
            }
        }
    }

    /**
     * Applies a notification action. Marking done or skipping settles today's reminder and arms
     * the next occurrence; a snooze keeps it open and re-arms at the snooze time. The posted
     * notification is cleared either way.
     */
    suspend fun respond(
        habitId: Long,
        action: ReminderAction,
    ) {
        val reminder = configuration.get(habitId) ?: return
        val date = reminder.activeDate ?: LocalDate.now(clock)
        when (action) {
            ReminderAction.MarkDone -> {
                habits.toggleCompletion(habitId, date)
                settle(habitId, reminder.localTime)
            }

            ReminderAction.NotToday -> {
                habits.setSkipped(habitId, date)
                settle(habitId, reminder.localTime)
            }

            is ReminderAction.Snooze -> snoozeUntil(habitId, clock.instant().plus(action.duration))

            ReminderAction.SnoozeUntilTomorrow ->
                snoozeUntil(habitId, tomorrowReminderInstant(ZonedDateTime.now(clock), reminder.localTime))
        }
        notifications.cancel(habitId)
    }

    private suspend fun settle(
        habitId: Long,
        localTime: LocalTime,
    ) {
        configuration.resolve(habitId)
        alarms.schedule(habitId, localTime)
    }

    private suspend fun snoozeUntil(
        habitId: Long,
        until: Instant,
    ) {
        configuration.snooze(habitId, until)
        alarms.scheduleAt(habitId, until)
    }
}
