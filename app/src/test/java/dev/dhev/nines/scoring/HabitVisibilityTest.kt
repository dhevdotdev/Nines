package dev.dhev.nines.scoring

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitVisibilityTest {
    @Test
    fun `a habit is visible on the exact date it was created`() {
        val createdAt = LocalDate.of(2026, 3, 10)
        assertTrue(isHabitVisibleOn(createdAt, createdAt))
    }

    @Test
    fun `a habit is visible on any date after it was created`() {
        val createdAt = LocalDate.of(2026, 3, 10)
        assertTrue(isHabitVisibleOn(createdAt, createdAt.plusDays(30)))
    }

    @Test
    fun `a habit is not visible before it was created`() {
        val createdAt = LocalDate.of(2026, 3, 10)
        assertFalse(isHabitVisibleOn(createdAt, createdAt.minusDays(1)))
    }
}
