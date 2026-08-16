# 04 — Remove the shallow Habit observation wrapper

**What to build:** Keep the shared repository seam focused on the date-bounded
Habit timeline and move detail-only calendar-day projection next to its detail
consumer.

**Blocked by:** 03 — Align selected-date presentation and close the observation path

**Status:** done

- [x] Inline timeline projection into `HabitRepository.observeHabitSnapshot`.
- [x] Remove the `HabitObservation` wrapper and its detail-only `calendarDays` field.
- [x] Build detail history cells in the detail adapter from bounded Entries.
- [x] Preserve repository, JVM, and connected test coverage.
