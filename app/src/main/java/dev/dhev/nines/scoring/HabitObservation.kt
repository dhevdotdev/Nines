package dev.dhev.nines.scoring

import java.time.LocalDate

data class HabitObservationInput(
    val kind: TimelineHabitKind,
    val createdDate: LocalDate,
    val selectedDate: LocalDate,
    val asOfDate: LocalDate,
    val historyStartDate: LocalDate,
    val entries: List<TimelineEntry>,
    val schedules: List<TimelineSchedule>,
)

data class ObservationDay(
    val date: LocalDate,
    val state: TimelineEntryState?,
    val numericValue: Double?,
)

data class HabitObservation(
    val timeline: HabitTimeline,
    val entriesThroughAsOfDate: List<TimelineEntry>,
    val calendarDays: List<ObservationDay>,
)

/** Projects one Habit into the date-bounded read model shared by homepage and detail adapters. */
fun buildHabitObservation(input: HabitObservationInput): HabitObservation {
    val boundedEntries = input.entries
        .filter { entry ->
            !entry.date.isBefore(input.createdDate) && !entry.date.isAfter(input.asOfDate)
        }
        .sortedBy { it.date }
    val entriesByDate = boundedEntries.associateBy { it.date }
    val calendarStart = maxOf(input.createdDate, input.historyStartDate)
    // The selected date changes the timeline's score/streak projection, but it must not
    // truncate the historical calendar. Callers use the explicit as-of date to decide how
    // much history is safe to show.
    val calendarEnd = input.asOfDate
    val calendarDays = if (calendarEnd.isBefore(calendarStart)) {
        emptyList()
    } else {
        generateSequence(calendarStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(calendarEnd) }
            .map { date ->
                val entry = entriesByDate[date]
                ObservationDay(date, entry?.state, entry?.numericValue)
            }
            .toList()
    }

    return HabitObservation(
        timeline = buildHabitTimeline(
            HabitTimelineInput(
                kind = input.kind,
                createdDate = input.createdDate,
                selectedDate = input.selectedDate,
                entries = boundedEntries,
                schedules = input.schedules,
            ),
        ),
        entriesThroughAsOfDate = boundedEntries,
        calendarDays = calendarDays,
    )
}
