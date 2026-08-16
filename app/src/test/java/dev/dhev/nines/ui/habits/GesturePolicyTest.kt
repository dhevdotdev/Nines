package dev.dhev.nines.ui.habits

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GesturePolicyTest {
    private val today = LocalDate.of(2026, 8, 16)

    @Test
    fun `reorder is available only for today`() {
        assertTrue(canEnterReorderMode(today, today))
        assertFalse(canEnterReorderMode(today.minusDays(1), today))
        assertTrue(isReorderModeActive(true, today, today))
        assertFalse(isReorderModeActive(true, today.minusDays(1), today))
    }

    @Test
    fun `swipe directions map to edit and archive`() {
        assertEquals(HabitSwipeAction.EDIT, action(HabitSwipeDirection.START_TO_END))
        assertEquals(HabitSwipeAction.ARCHIVE, action(HabitSwipeDirection.END_TO_START))
        assertEquals(HabitSwipeAction.NONE, action(null))
    }

    @Test
    fun `reorder mode disables swipe actions`() {
        assertEquals(
            HabitSwipeAction.NONE,
            habitSwipeAction(
                HabitGesturePolicyInput(today, today, reorderMode = true, HabitSwipeDirection.START_TO_END),
            ),
        )
    }

    private fun action(direction: HabitSwipeDirection?) = habitSwipeAction(
        HabitGesturePolicyInput(today, today, reorderMode = false, swipeDirection = direction),
    )
}
