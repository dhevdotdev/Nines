package dev.dhev.nines.reminders

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** The observable delivery state of one day's boolean Reminder. */
enum class ReminderDelivery { PENDING, DUE, RESOLVED }

/** The only Entry states that can resolve a boolean Reminder. */
enum class ReminderEntryState { COMPLETED, SKIPPED }

/**
 * Decides whether a daily boolean Reminder should be shown for [date]. Snoozing delays delivery,
 * while an explicit Entry resolves the Reminder for its original date without creating new data.
 */
fun decideDailyBooleanReminder(
    date: LocalDate,
    reminderTime: LocalTime,
    now: LocalDateTime,
    entryState: ReminderEntryState?,
    snoozedUntil: LocalDateTime?,
): ReminderDelivery = when {
    entryState != null -> ReminderDelivery.RESOLVED
    now.toLocalDate() != date -> ReminderDelivery.PENDING
    snoozedUntil != null && now.isBefore(snoozedUntil) -> ReminderDelivery.PENDING
    now.toLocalTime().isBefore(reminderTime) -> ReminderDelivery.PENDING
    else -> ReminderDelivery.DUE
}
