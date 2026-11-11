package dev.dhev.nines.ui.habits

import dev.dhev.nines.scoring.TimelineEntry
import dev.dhev.nines.scoring.TimelineEntryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitDetailHistoryTest {
    private val created = LocalDate.of(2026, 8, 1)
    private val selected = LocalDate.of(2026, 8, 5)
    private val asOf = LocalDate.of(2026, 8, 10)

    @Test
    fun `detail history clips to creation selected and as-of dates`() {
        val history =
            buildDetailHistory(
                entries =
                listOf(
                    entry(created.minusDays(1)),
                    entry(created),
                    entry(selected),
                    entry(asOf),
                    entry(asOf.plusDays(1)),
                ),
                createdDate = created,
                historyStartDate = created.minusDays(3),
                selectedDate = selected,
                asOfDate = asOf,
                today = asOf,
            )

        assertEquals(created, history.first().date)
        assertEquals(selected, history.last().date)
        assertEquals(2, history.count { it.state == dev.dhev.nines.data.local.entity.EntryState.COMPLETED })
    }

    @Test
    fun `detail history is empty before habit creation`() {
        val history =
            buildDetailHistory(
                entries = emptyList(),
                createdDate = selected.plusDays(1),
                historyStartDate = created,
                selectedDate = selected,
                asOfDate = asOf,
                today = asOf,
            )

        assertTrue(history.isEmpty())
    }

    @Test
    fun `only dates before today require binary backfill confirmation`() {
        val today = LocalDate.of(2026, 8, 10)

        assertTrue(requiresHistoricalBinaryConfirmation(today.minusDays(1), today))
        assertTrue(!requiresHistoricalBinaryConfirmation(today, today))
        assertTrue(!requiresHistoricalBinaryConfirmation(today.plusDays(1), today))
    }

    private fun entry(date: LocalDate) = TimelineEntry(date, TimelineEntryState.COMPLETED)
}
