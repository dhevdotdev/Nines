# 01 — Build the domain Habit observation

**What to build:** Create the shared date-bounded Habit observation that describes selected-date state and bounded calendar-day data consistently for any caller.

**Blocked by:** None — can start immediately

**Status:** done

- [x] Define domain observation input/output using explicit selected and as-of dates.
- [x] Compose the existing Habit timeline without changing score, streak, gap, or frequency-progress semantics.
- [x] Produce bounded calendar-day observations that exclude pre-creation and post-as-of dates.
- [x] Add plain-JUnit coverage for visibility, date clipping, schedule boundaries, ordering, and return-to-today inputs.

## Comments

- Added the domain-only `HabitObservation` module. It preserves the existing timeline implementation and adds explicitly bounded Entry and calendar-day projections. `HabitObservationTest` passes.
