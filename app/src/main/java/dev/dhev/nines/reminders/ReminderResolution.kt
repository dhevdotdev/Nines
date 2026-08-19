package dev.dhev.nines.reminders

import dev.dhev.nines.scoring.FrequencyTargetKind

data class ReminderResolutionInput(
    val completed: Boolean? = null,
    val numericValue: Double? = null,
    val targetValue: Double? = null,
    val targetKind: FrequencyTargetKind? = null,
) {
    companion object {
        fun boolean(completed: Boolean) = ReminderResolutionInput(completed = completed)

        fun numeric(value: Double?, target: Double, kind: FrequencyTargetKind) = ReminderResolutionInput(
            numericValue = value,
            targetValue = target,
            targetKind = kind,
        )
    }
}

fun isReminderResolved(input: ReminderResolutionInput): Boolean = when {
    input.completed != null -> input.completed
    input.targetKind == FrequencyTargetKind.AT_MOST -> input.numericValue != null
    input.targetKind == FrequencyTargetKind.AT_LEAST -> input.numericValue != null && input.numericValue >= (input.targetValue ?: Double.POSITIVE_INFINITY)
    else -> false
}
