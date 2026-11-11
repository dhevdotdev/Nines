package dev.dhev.nines.reminders

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderTimingTest {
    private val utc = ZoneId.of("UTC")
    private val nine = LocalTime.of(9, 0)

    private fun at(
        iso: String,
        zone: ZoneId = utc,
    ): ZonedDateTime = ZonedDateTime.parse(iso).withZoneSameInstant(zone)

    @Test
    fun `a time still ahead today fires today`() {
        val now = at("2026-03-18T07:30:00Z")

        assertEquals(Instant.parse("2026-03-18T09:00:00Z"), nextReminderInstant(now, nine))
    }

    @Test
    fun `a time already past today fires tomorrow`() {
        val now = at("2026-03-18T09:30:00Z")

        assertEquals(Instant.parse("2026-03-19T09:00:00Z"), nextReminderInstant(now, nine))
    }

    @Test
    fun `the exact reminder minute rolls to tomorrow rather than firing twice`() {
        val now = at("2026-03-18T09:00:00Z")

        assertEquals(Instant.parse("2026-03-19T09:00:00Z"), nextReminderInstant(now, nine))
    }

    @Test
    fun `the zone decides what today means`() {
        val kolkata = ZoneId.of("Asia/Kolkata")
        val now = at("2026-03-18T20:00:00Z", kolkata) // 01:30 on the 19th in Kolkata

        assertEquals(Instant.parse("2026-03-19T03:30:00Z"), nextReminderInstant(now, nine))
    }

    @Test
    fun `a reminder inside a spring-forward gap lands after the gap`() {
        val berlin = ZoneId.of("Europe/Berlin")
        val now = at("2026-03-28T12:00:00Z", berlin) // the day before clocks jump 02:00 to 03:00

        assertEquals(Instant.parse("2026-03-29T01:30:00Z"), nextReminderInstant(now, LocalTime.of(2, 30)))
    }

    @Test
    fun `tomorrow is tomorrow in the local zone`() {
        val now = at("2026-03-18T23:30:00Z")

        assertEquals(Instant.parse("2026-03-19T09:00:00Z"), tomorrowReminderInstant(now, nine))
    }
}
