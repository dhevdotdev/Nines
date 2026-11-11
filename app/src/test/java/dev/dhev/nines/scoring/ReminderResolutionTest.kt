package dev.dhev.nines.scoring

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderResolutionTest {
    private val today = LocalDate.of(2026, 3, 18)

    private fun schedule(
        numerator: Int = 1,
        denominator: Int = 1,
        targetValue: Double? = null,
        targetKind: FrequencyTargetKind? = null,
    ) = TimelineSchedule(
        effectiveFromDate = LocalDate.of(2026, 1, 1),
        target = ScheduleTarget(numerator, denominator),
        targetValue = targetValue,
        targetKind = targetKind,
    )

    private fun done(daysAgo: Long) = TimelineEntry(today.minusDays(daysAgo), TimelineEntryState.COMPLETED)

    private fun value(
        daysAgo: Long,
        amount: Double,
        state: TimelineEntryState = TimelineEntryState.IN_PROGRESS,
    ) = TimelineEntry(today.minusDays(daysAgo), state, numericValue = amount)

    private fun resolved(
        kind: TimelineHabitKind,
        schedules: List<TimelineSchedule>,
        entries: List<TimelineEntry>,
    ) = isReminderResolvedOn(kind, today, schedules, entries)

    @Test
    fun `a habit with no schedule in force is never resolved`() {
        assertFalse(resolved(TimelineHabitKind.BOOLEAN, emptyList(), listOf(done(0))))
        val notYetEffective = schedule().copy(effectiveFromDate = today.plusDays(1))
        assertFalse(resolved(TimelineHabitKind.BOOLEAN, listOf(notYetEffective), listOf(done(0))))
    }

    @Test
    fun `a skipped day resolves the reminder whatever the target`() {
        val skipped = TimelineEntry(today, TimelineEntryState.SKIPPED)
        assertTrue(resolved(TimelineHabitKind.BOOLEAN, listOf(schedule()), listOf(skipped)))
        assertTrue(
            resolved(
                TimelineHabitKind.NUMERICAL,
                listOf(schedule(targetValue = 8.0, targetKind = FrequencyTargetKind.AT_LEAST)),
                listOf(skipped),
            ),
        )
    }

    @Test
    fun `a daily boolean habit is resolved only by today's completion`() {
        val daily = listOf(schedule())
        assertTrue(resolved(TimelineHabitKind.BOOLEAN, daily, listOf(done(0))))
        assertFalse(resolved(TimelineHabitKind.BOOLEAN, daily, emptyList()))
        assertFalse(resolved(TimelineHabitKind.BOOLEAN, daily, listOf(done(1))))
    }

    @Test
    fun `a weekly boolean target counts completions inside its rolling window only`() {
        val threeInSeven = listOf(schedule(numerator = 3, denominator = 7))
        assertFalse(resolved(TimelineHabitKind.BOOLEAN, threeInSeven, listOf(done(0), done(2))))
        assertTrue(resolved(TimelineHabitKind.BOOLEAN, threeInSeven, listOf(done(0), done(2), done(6))))
        assertFalse(resolved(TimelineHabitKind.BOOLEAN, threeInSeven, listOf(done(0), done(2), done(7))))
    }

    @Test
    fun `in-progress entries do not satisfy a boolean target`() {
        val inProgress = TimelineEntry(today, TimelineEntryState.IN_PROGRESS)
        assertFalse(resolved(TimelineHabitKind.BOOLEAN, listOf(schedule()), listOf(inProgress)))
    }

    @Test
    fun `an at-least numeric target is met by the window total`() {
        val eight = listOf(schedule(targetValue = 8.0, targetKind = FrequencyTargetKind.AT_LEAST))
        assertTrue(resolved(TimelineHabitKind.NUMERICAL, eight, listOf(value(0, 8.0))))
        assertFalse(resolved(TimelineHabitKind.NUMERICAL, eight, listOf(value(0, 7.0))))

        val fifteenInTwoDays = listOf(
            schedule(denominator = 2, targetValue = 15.0, targetKind = FrequencyTargetKind.AT_LEAST),
        )
        assertTrue(resolved(TimelineHabitKind.NUMERICAL, fifteenInTwoDays, listOf(value(0, 7.0), value(1, 8.0))))
    }

    @Test
    fun `an at-most numeric target is resolved by logging anything today`() {
        val limit = listOf(schedule(targetValue = 120.0, targetKind = FrequencyTargetKind.AT_MOST))
        assertFalse(resolved(TimelineHabitKind.NUMERICAL, limit, emptyList()))
        assertFalse(resolved(TimelineHabitKind.NUMERICAL, limit, listOf(value(1, 60.0))))
        assertTrue(resolved(TimelineHabitKind.NUMERICAL, limit, listOf(value(0, 60.0))))
        assertTrue(resolved(TimelineHabitKind.NUMERICAL, limit, listOf(value(0, 200.0))))
    }

    @Test
    fun `a numeric habit with no target value is never resolved`() {
        val broken = listOf(schedule(targetValue = null, targetKind = FrequencyTargetKind.AT_LEAST))
        assertFalse(resolved(TimelineHabitKind.NUMERICAL, broken, listOf(value(0, 50.0))))
    }

    @Test
    fun `the schedule in force on the date decides, not a later one`() {
        val early = schedule(numerator = 1, denominator = 1)
        val later = schedule(numerator = 5, denominator = 7).copy(effectiveFromDate = today.plusDays(3))
        assertTrue(resolved(TimelineHabitKind.BOOLEAN, listOf(early, later), listOf(done(0))))
    }
}
