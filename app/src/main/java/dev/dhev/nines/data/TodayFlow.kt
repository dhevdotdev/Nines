package dev.dhev.nines.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Delays are monotonic, so a wall-clock or time-zone change while waiting would leave a long delay
 * stale. Re-reading the clock at least this often keeps the date correct within a minute.
 */
private const val MAX_POLL_MILLIS = 60_000L

/** The current local date, emitted now and again each time midnight passes. */
fun Clock.todayFlow(): Flow<LocalDate> {
    val clock = this
    return flow {
        while (true) {
            val now = ZonedDateTime.now(clock)
            emit(now.toLocalDate())
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(clock.zone)
            delay(Duration.between(now, nextMidnight).toMillis().coerceIn(1L, MAX_POLL_MILLIS))
        }
    }.distinctUntilChanged()
}
