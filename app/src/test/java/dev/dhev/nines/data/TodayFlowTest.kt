package dev.dhev.nines.data

import dev.dhev.nines.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class TodayFlowTest {
    private val evening = Instant.parse("2026-03-18T20:00:00Z")

    @Test
    fun `emits the current date straight away`() = runTest {
        val clock = TestClock(testScheduler, evening)
        val seen = mutableListOf<LocalDate>()
        val job = launch { clock.todayFlow().toList(seen) }
        runCurrent()
        job.cancel()

        assertEquals(listOf(LocalDate.of(2026, 3, 18)), seen)
    }

    @Test
    fun `picks up a wall clock jump within a minute`() = runTest {
        var offsetMillis = 0L
        val clock = object : java.time.Clock() {
            override fun getZone() = java.time.ZoneOffset.UTC

            override fun withZone(zone: java.time.ZoneId) = this

            override fun instant(): Instant = evening.plusMillis(testScheduler.currentTime + offsetMillis)
        }
        val seen = mutableListOf<LocalDate>()
        val job = launch { clock.todayFlow().toList(seen) }
        runCurrent()

        offsetMillis = 5.hours.inWholeMilliseconds
        advanceTimeBy(61.seconds)
        runCurrent()

        assertEquals(LocalDate.of(2026, 3, 19), seen.last())
        job.cancel()
    }

    @Test
    fun `emits the next date once midnight passes and not before`() = runTest {
        val clock = TestClock(testScheduler, evening)
        val seen = mutableListOf<LocalDate>()
        val job = launch { clock.todayFlow().toList(seen) }
        runCurrent()

        advanceTimeBy(3.hours + 59.minutes)
        runCurrent()
        assertEquals(listOf(LocalDate.of(2026, 3, 18)), seen)

        advanceTimeBy(2.minutes)
        runCurrent()
        assertEquals(listOf(LocalDate.of(2026, 3, 18), LocalDate.of(2026, 3, 19)), seen)

        advanceTimeBy(24.hours)
        runCurrent()
        assertEquals(LocalDate.of(2026, 3, 20), seen.last())
        job.cancel()
    }
}
