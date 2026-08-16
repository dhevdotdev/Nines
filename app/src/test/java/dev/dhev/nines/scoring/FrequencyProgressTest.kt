package dev.dhev.nines.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FrequencyProgressTest {

    private val selectedDate = LocalDate.of(2026, 8, 16)

    @Test
    fun `boolean progress counts explicit completions in the rolling window`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 3, denominator = 7),
            input = FrequencyProgressInput.BooleanEntries(
                completedDates = setOf(
                    selectedDate,
                    selectedDate.minusDays(2),
                    selectedDate.minusDays(6),
                    selectedDate.minusDays(7),
                ),
            ),
        )

        assertEquals(3.0, progress.achieved, 0.0)
        assertEquals(3.0, progress.target, 0.0)
        assertTrue(progress.isMet)
    }

    @Test
    fun `daily boolean progress only counts the selected date`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget.DAILY,
            input = FrequencyProgressInput.BooleanEntries(
                completedDates = setOf(selectedDate.minusDays(1)),
            ),
        )

        assertEquals(0.0, progress.achieved, 0.0)
        assertEquals(1.0, progress.target, 0.0)
    }

    @Test
    fun `every N days includes both ends and excludes the day before the window`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 1, denominator = 3),
            input = FrequencyProgressInput.BooleanEntries(
                completedDates = setOf(
                    selectedDate,
                    selectedDate.minusDays(2),
                    selectedDate.minusDays(3),
                ),
            ),
        )

        assertEquals(2.0, progress.achieved, 0.0)
    }

    @Test
    fun `empty frequency window reports zero progress`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 3, denominator = 7),
            input = FrequencyProgressInput.BooleanEntries(emptySet()),
        )

        assertEquals(0.0, progress.achieved, 0.0)
        assertEquals(false, progress.isMet)
    }

    @Test
    fun `over target boolean progress remains readable and met`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 2, denominator = 7),
            input = FrequencyProgressInput.BooleanEntries(
                completedDates = (0..4).map { selectedDate.minusDays(it.toLong()) }.toSet(),
            ),
        )

        assertEquals(5.0, progress.achieved, 0.0)
        assertTrue(progress.isMet)
    }

    @Test
    fun `numeric progress sums values in the rolling window and ignores later values`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget.DAILY,
            input = FrequencyProgressInput.NumericEntries(
                valuesByDate = mapOf(
                    selectedDate.minusDays(1) to 20.0,
                    selectedDate to 25.0,
                    selectedDate.plusDays(1) to 100.0,
                ),
                targetValue = 40.0,
                targetKind = FrequencyTargetKind.AT_LEAST,
            ),
        )

        assertEquals(25.0, progress.achieved, 0.0)
        assertEquals(40.0, progress.target, 0.0)
        assertEquals(false, progress.isMet)
    }

    @Test
    fun `at most progress is met when aggregate is at or below target`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 1, denominator = 7),
            input = FrequencyProgressInput.NumericEntries(
                valuesByDate = mapOf(selectedDate.minusDays(2) to 4.0),
                targetValue = 5.0,
                targetKind = FrequencyTargetKind.AT_MOST,
            ),
        )

        assertEquals(4.0, progress.achieved, 0.0)
        assertTrue(progress.isMet)
    }

    @Test
    fun `numeric empty window reports zero progress`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 1, denominator = 7),
            input = FrequencyProgressInput.NumericEntries(
                valuesByDate = emptyMap(),
                targetValue = 5.0,
                targetKind = FrequencyTargetKind.AT_LEAST,
            ),
        )

        assertEquals(0.0, progress.achieved, 0.0)
        assertEquals(false, progress.isMet)
    }

    @Test
    fun `numeric at least progress remains met after exceeding target`() {
        val progress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 1, denominator = 7),
            input = FrequencyProgressInput.NumericEntries(
                valuesByDate = mapOf(selectedDate to 8.0),
                targetValue = 5.0,
                targetKind = FrequencyTargetKind.AT_LEAST,
            ),
        )

        assertEquals(8.0, progress.achieved, 0.0)
        assertTrue(progress.isMet)
    }

    @Test
    fun `progress uses the target supplied by the schedule effective on the selected date`() {
        val entries = FrequencyProgressInput.BooleanEntries(setOf(selectedDate))

        val historicalProgress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 1, denominator = 7),
            input = entries,
        )
        val currentProgress = computeFrequencyProgress(
            selectedDate = selectedDate,
            target = ScheduleTarget(numerator = 3, denominator = 7),
            input = entries,
        )

        assertTrue(historicalProgress.isMet)
        assertEquals(false, currentProgress.isMet)
    }
}
