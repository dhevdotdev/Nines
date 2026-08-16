# 01 — Build the domain Habit observation

**What to build:** Create the shared date-bounded Habit observation that describes selected-date state and bounded calendar-day data consistently for any caller.

**Blocked by:** None — can start immediately

**Status:** done

- [x] Define domain observation input/output using explicit selected and as-of dates.
- [x] Compose the existing Habit timeline without changing score, streak, gap, or frequency-progress semantics.
- [x] Produce bounded calendar-day observations that exclude pre-creation and post-as-of dates.
- [x] Add plain-JUnit coverage for visibility, date clipping, and future-date projection; existing timeline and ViewModel contract suites retain schedule, ordering, and return-to-today coverage.

> Historical note: this ticket records the original observation projection. Its
> shallow wrapper was removed by ticket 04; the shared production seam is now the
> `HabitTimelineSnapshot`, while detail history remains detail-only.

## Comments

- Added the domain-only `HabitObservation` module. It preserves the existing timeline implementation and adds explicitly bounded Entry and calendar-day projections. `HabitObservationTest` passes.
- Future selected dates now project at the explicit as-of date. Added coverage for future projection and post-creation invisibility; Room coverage verifies the real snapshot mapping and selected-date calendar boundary.
