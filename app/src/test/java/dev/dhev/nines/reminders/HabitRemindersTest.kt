package dev.dhev.nines.reminders

import dev.dhev.nines.FakeEntryDao
import dev.dhev.nines.FakeHabitDao
import dev.dhev.nines.FakeReminderDao
import dev.dhev.nines.FakeScheduleDao
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.entity.EntryEntity
import dev.dhev.nines.data.local.entity.EntryState
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class HabitRemindersTest {
    private val nine = LocalTime.of(9, 0)

    private fun habit(archived: Boolean = false) = HabitEntity(
        id = 1L,
        name = "Stretch",
        type = HabitType.BOOLEAN,
        color = 0,
        iconKey = "check",
        position = 0,
        archivedAt = if (archived) Instant.parse("2026-03-01T00:00:00Z") else null,
        createdAt = Instant.parse("2026-02-01T00:00:00Z"),
    )

    private class Rig(
        habit: HabitEntity,
        reminders: List<ReminderEntity> = emptyList(),
    ) {
        val clock: Clock = Clock.fixed(Instant.parse("2026-03-18T20:00:00Z"), ZoneOffset.UTC)
        val entries = MutableStateFlow(emptyList<EntryEntity>())
        val dao = FakeReminderDao(reminders)
        val alarms = RecordingAlarms()
        val notifications = RecordingNotifications()
        val subject = HabitReminders(
            configuration = ReminderRepository(dao),
            habits = HabitRepository(
                FakeHabitDao(MutableStateFlow(habit)),
                FakeScheduleDao(MutableStateFlow(emptyList())),
                FakeEntryDao(entries),
                clock,
            ),
            alarms = alarms,
            notifications = notifications,
            clock = clock,
        )
    }

    @Test
    fun `configuring a time stores it and schedules the alarm`() = runBlocking {
        val rig = Rig(habit())

        rig.subject.configure(1L, nine)

        assertEquals(nine, rig.subject.observeTime(1L).first())
        assertEquals(listOf(1L to nine), rig.alarms.scheduled)
    }

    @Test
    fun `configuring no time clears the reminder and cancels the alarm`() = runBlocking {
        val rig = Rig(habit(), listOf(ReminderEntity(1L, nine)))

        rig.subject.configure(1L, null)

        assertNull(rig.subject.observeTime(1L).first())
        assertEquals(listOf(1L), rig.alarms.cancelled)
    }

    @Test
    fun `archiving stops the alarm and notification but keeps the configuration`() = runBlocking {
        val rig = Rig(habit(), listOf(ReminderEntity(1L, nine)))

        rig.subject.onArchived(1L)

        assertEquals(listOf(1L), rig.alarms.cancelled)
        assertEquals(listOf(1L), rig.notifications.cancelled)
        assertEquals(nine, rig.subject.observeTime(1L).first())
    }

    @Test
    fun `restoring schedules the kept reminder and ignores a habit without one`() = runBlocking {
        val withReminder = Rig(habit(), listOf(ReminderEntity(1L, nine)))
        withReminder.subject.onRestored(1L)
        assertEquals(listOf(1L to nine), withReminder.alarms.scheduled)

        val without = Rig(habit())
        without.subject.onRestored(1L)
        assertEquals(emptyList<Pair<Long, LocalTime>>(), without.alarms.scheduled)
    }

    @Test
    fun `rescheduling everything skips reminders of archived habits`() = runBlocking {
        val active = Rig(habit(), listOf(ReminderEntity(1L, nine)))
        active.subject.rescheduleAll()
        assertEquals(listOf(1L to nine), active.alarms.scheduled)

        val archived = Rig(habit(archived = true), listOf(ReminderEntity(1L, nine)))
        archived.subject.rescheduleAll()
        assertEquals(emptyList<Pair<Long, LocalTime>>(), archived.alarms.scheduled)
    }

    private val today = LocalDate.of(2026, 3, 18)

    @Test
    fun `marking done logs the active date, settles the reminder and re-arms tomorrow`() = runBlocking {
        val yesterday = today.minusDays(1)
        val rig = Rig(habit(), listOf(ReminderEntity(1L, nine, activeDate = yesterday, lastAlertedAt = Instant.EPOCH)))

        rig.subject.respond(1L, ReminderAction.MarkDone)

        assertEquals(listOf(yesterday to EntryState.COMPLETED), rig.entries.value.map { it.date to it.state })
        val reminder = rig.dao.get(1L)
        assertNull(reminder?.activeDate)
        assertNull(reminder?.lastAlertedAt)
        assertEquals(listOf(1L to nine), rig.alarms.scheduled)
        assertEquals(listOf(1L), rig.notifications.cancelled)
    }

    @Test
    fun `not today skips the day and re-arms`() = runBlocking {
        val rig = Rig(habit(), listOf(ReminderEntity(1L, nine)))

        rig.subject.respond(1L, ReminderAction.NotToday)

        assertEquals(listOf(today to EntryState.SKIPPED), rig.entries.value.map { it.date to it.state })
        assertEquals(listOf(1L to nine), rig.alarms.scheduled)
        assertEquals(listOf(1L), rig.notifications.cancelled)
    }

    @Test
    fun `a snooze keeps the reminder open and re-arms at the snooze time`() = runBlocking {
        val rig = Rig(habit(), listOf(ReminderEntity(1L, nine, activeDate = today)))

        rig.subject.respond(1L, ReminderAction.Snooze(Duration.ofHours(1)))

        val until = Instant.parse("2026-03-18T21:00:00Z")
        assertEquals(until, rig.dao.get(1L)?.snoozedUntil)
        assertEquals(today, rig.dao.get(1L)?.activeDate)
        assertEquals(listOf(1L to until), rig.alarms.scheduledAt)
        assertEquals(emptyList<Pair<Long, LocalTime>>(), rig.alarms.scheduled)
        assertEquals(emptyList<EntryEntity>(), rig.entries.value)
        assertEquals(listOf(1L), rig.notifications.cancelled)
    }

    @Test
    fun `snoozing until tomorrow lands on the reminder time tomorrow`() = runBlocking {
        val rig = Rig(habit(), listOf(ReminderEntity(1L, nine)))

        rig.subject.respond(1L, ReminderAction.SnoozeUntilTomorrow)

        val tomorrow = Instant.parse("2026-03-19T09:00:00Z")
        assertEquals(tomorrow, rig.dao.get(1L)?.snoozedUntil)
        assertEquals(listOf(1L to tomorrow), rig.alarms.scheduledAt)
    }

    @Test
    fun `an action for a habit with no reminder does nothing`() = runBlocking {
        val rig = Rig(habit())

        rig.subject.respond(1L, ReminderAction.MarkDone)

        assertEquals(emptyList<EntryEntity>(), rig.entries.value)
        assertEquals(emptyList<Long>(), rig.notifications.cancelled)
    }
}
