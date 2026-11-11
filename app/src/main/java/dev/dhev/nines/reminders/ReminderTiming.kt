package dev.dhev.nines.reminders

import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * The next moment a daily reminder at [localTime] should fire: today if that time is still ahead
 * of [now], otherwise tomorrow. Resolved in [now]'s zone, so a daylight-saving gap or overlap is
 * handled by the zone rules rather than by adding 24 hours.
 */
fun nextReminderInstant(
    now: ZonedDateTime,
    localTime: LocalTime,
): Instant {
    val today = now.toLocalDate().atTime(localTime).atZone(now.zone)
    return (if (today.isAfter(now)) today else now.toLocalDate().plusDays(1).atTime(localTime).atZone(now.zone)).toInstant()
}

/** [localTime] tomorrow, used by "snooze until tomorrow". */
fun tomorrowReminderInstant(
    now: ZonedDateTime,
    localTime: LocalTime,
): Instant = now.toLocalDate().plusDays(1).atTime(localTime).atZone(now.zone).toInstant()
