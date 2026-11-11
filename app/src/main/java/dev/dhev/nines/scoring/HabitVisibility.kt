package dev.dhev.nines.scoring

import java.time.LocalDate

/** Whether a habit created on [createdAt] should appear on [date] — false for any date before
 * it existed. Inclusive of the creation date itself. */
fun isHabitVisibleOn(
    createdAt: LocalDate,
    date: LocalDate,
): Boolean = !date.isBefore(createdAt)
