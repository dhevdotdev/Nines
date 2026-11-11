package dev.dhev.nines.reminders

import dev.dhev.nines.FakeEntryDao
import dev.dhev.nines.FakeHabitDao
import dev.dhev.nines.FakeReminderDao
import dev.dhev.nines.FakeScheduleDao
import dev.dhev.nines.data.HabitRepository
import dev.dhev.nines.data.ReminderRepository
import dev.dhev.nines.data.local.entity.HabitEntity
import dev.dhev.nines.data.local.entity.HabitType
import dev.dhev.nines.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

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
        val dao = FakeReminderDao(reminders)
        val alarms = RecordingAlarms()
        val notifications = RecordingNotifications()
        val subject = HabitReminders(
            configuration = ReminderRepository(dao),
            habits = HabitRepository(
                FakeHabitDao(MutableStateFlow(habit)),
                FakeScheduleDao(MutableStateFlow(emptyList())),
                FakeEntryDao(MutableStateFlow(emptyList())),
            ),
            alarms = alarms,
            notifications = notifications,
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
}
