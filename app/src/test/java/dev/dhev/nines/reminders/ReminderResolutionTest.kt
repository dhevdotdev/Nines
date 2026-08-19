package dev.dhev.nines.reminders

import dev.dhev.nines.scoring.FrequencyTargetKind
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderResolutionTest {
    @Test
    fun `boolean and at least numeric reminders resolve only when their target is met`() {
        assertTrue(isReminderResolved(ReminderResolutionInput.boolean(completed = true)))
        assertFalse(isReminderResolved(ReminderResolutionInput.boolean(completed = false)))
        assertTrue(isReminderResolved(ReminderResolutionInput.numeric(8.0, 8.0, FrequencyTargetKind.AT_LEAST)))
        assertFalse(isReminderResolved(ReminderResolutionInput.numeric(7.0, 8.0, FrequencyTargetKind.AT_LEAST)))
    }

    @Test
    fun `at most numeric reminder resolves once a value is recorded`() {
        assertFalse(isReminderResolved(ReminderResolutionInput.numeric(null, 120.0, FrequencyTargetKind.AT_MOST)))
        assertTrue(isReminderResolved(ReminderResolutionInput.numeric(130.0, 120.0, FrequencyTargetKind.AT_MOST)))
    }
}
