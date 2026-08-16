# Date-bounded detail analytics

**Status:** done

## Problem

`HabitDetailViewModel` mixed timeline projection, date-window analytics, and UI-state mapping in one flow. The analytics rules were difficult to test independently and could accidentally inspect entries beyond the detail screen's as-of date.

## Solution

Add a pure `computeHabitAnalytics` module. It accepts the timeline's habit kind, creation date, an explicit as-of date, timeline entries, and the current streak. It returns one analytics model containing streak ranking, completion totals, weekday rates, momentum, typical time of day, longest gap, numeric aggregates, and record-progress state. The detail ViewModel adapts that model into existing UI fields and owns user-facing record copy/history cells.

## Invariants

- Analytics never reads Room, Compose, ViewModel state, or the system clock.
- Entries before creation or after the as-of date do not affect any returned statistic.
- Numeric totals and averages use positive values from completed entries only.
- Existing thresholds remain stable: weekday rates require at least 14 days of history, momentum requires at least 60 days, typical time requires five completed entries, and gaps shorter than three days are not surfaced.
- UI copy remains outside the domain module.

## Out of scope

- Redesigning the detail screen.
- Moving history-cell presentation into the analytics module.
- Changing the meaning of score, streak, or schedule selection owned by the Habit timeline.
