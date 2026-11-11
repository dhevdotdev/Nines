package dev.dhev.nines.scoring

import java.time.LocalDate

/** The direction in which a numeric target is satisfied. */
enum class FrequencyTargetKind {
    AT_LEAST,
    AT_MOST,
}

/** Input data for the two kinds of habit Entries supported by frequency progress. */
sealed interface FrequencyProgressInput {
    data class BooleanEntries(
        val completedDates: Set<LocalDate>,
    ) : FrequencyProgressInput

    data class NumericEntries(
        val valuesByDate: Map<LocalDate, Double>,
        val targetValue: Double,
        val targetKind: FrequencyTargetKind,
    ) : FrequencyProgressInput
}

/** A target's explicit progress inside its rolling Schedule window. */
data class FrequencyProgress(
    val achieved: Double,
    val target: Double,
    val targetKind: FrequencyTargetKind,
) {
    val isMet: Boolean
        get() =
            when (targetKind) {
                FrequencyTargetKind.AT_LEAST -> achieved >= target
                FrequencyTargetKind.AT_MOST -> achieved <= target
            }
}

/**
 * Computes explicit progress in the Schedule window ending on [selectedDate].
 *
 * The denominator is the number of inclusive calendar days in the window. A boolean habit's
 * target is its numerator; a numeric habit's target is supplied with its numeric input.
 */
fun computeFrequencyProgress(
    selectedDate: LocalDate,
    target: ScheduleTarget,
    input: FrequencyProgressInput,
): FrequencyProgress {
    val windowStart = selectedDate.minusDays(target.denominator.toLong() - 1)
    val inWindow: (LocalDate) -> Boolean = { date ->
        !date.isBefore(windowStart) && !date.isAfter(selectedDate)
    }

    return when (input) {
        is FrequencyProgressInput.BooleanEntries -> {
            FrequencyProgress(
                achieved = input.completedDates.count(inWindow).toDouble(),
                target = target.numerator.toDouble(),
                targetKind = FrequencyTargetKind.AT_LEAST,
            )
        }

        is FrequencyProgressInput.NumericEntries -> {
            FrequencyProgress(
                achieved =
                input.valuesByDate
                    .filterKeys(inWindow)
                    .values
                    .sum(),
                target = input.targetValue,
                targetKind = input.targetKind,
            )
        }
    }
}
