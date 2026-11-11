package dev.dhev.nines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** A Clock that moves with a test scheduler's virtual time, so delays and dates advance together. */
@OptIn(ExperimentalCoroutinesApi::class)
class TestClock(
    private val scheduler: TestCoroutineScheduler,
    private val start: Instant,
    private val zone: ZoneId = ZoneId.of("UTC"),
) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = TestClock(scheduler, start, zone)

    override fun instant(): Instant = start.plusMillis(scheduler.currentTime)
}
