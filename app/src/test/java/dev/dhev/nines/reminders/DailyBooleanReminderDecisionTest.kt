package dev.dhev.nines.reminders

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DailyBooleanReminderDecisionTest {
    private val date = LocalDate.of(2026, 8, 19)
    private val reminderTime = LocalTime.of(19, 0)

    @Test
    fun `is pending before its configured time`() {
        assertEquals(
            ReminderDelivery.PENDING,
            decideDailyBooleanReminder(
                date = date,
                reminderTime = reminderTime,
                now = LocalDateTime.of(date, LocalTime.of(18, 59)),
                entryState = null,
                snoozedUntil = null,
            ),
        )
    }

    @Test
    fun `is due after its configured time until an explicit entry resolves it`() {
        assertEquals(
            ReminderDelivery.DUE,
            decideDailyBooleanReminder(
                date = date,
                reminderTime = reminderTime,
                now = LocalDateTime.of(date, LocalTime.of(19, 1)),
                entryState = null,
                snoozedUntil = null,
            ),
        )
        assertEquals(
            ReminderDelivery.RESOLVED,
            decideDailyBooleanReminder(
                date = date,
                reminderTime = reminderTime,
                now = LocalDateTime.of(date, LocalTime.of(19, 1)),
                entryState = ReminderEntryState.COMPLETED,
                snoozedUntil = null,
            ),
        )
    }

    @Test
    fun `snooze defers a due reminder without resolving it`() {
        assertEquals(
            ReminderDelivery.PENDING,
            decideDailyBooleanReminder(
                date = date,
                reminderTime = reminderTime,
                now = LocalDateTime.of(date, LocalTime.of(19, 10)),
                entryState = null,
                snoozedUntil = LocalDateTime.of(date, LocalTime.of(19, 15)),
            ),
        )
        assertEquals(
            ReminderDelivery.DUE,
            decideDailyBooleanReminder(
                date = date,
                reminderTime = reminderTime,
                now = LocalDateTime.of(date, LocalTime.of(19, 15)),
                entryState = null,
                snoozedUntil = LocalDateTime.of(date, LocalTime.of(19, 15)),
            ),
        )
    }
}
