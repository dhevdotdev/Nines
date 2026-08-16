# 01 — Route the homepage through the Habit timeline

**What to build:** Make the homepage derive its selected-date Habit rows through one date-bounded Habit timeline. The homepage must retain its current visible habits, selected-date Entry actions, Score, Streak, Gap, and frequency progress while moving their shared date/Schedule interpretation behind the timeline seam.

**Blocked by:** None — can start immediately.

**Status:** done

- [x] A pure Habit timeline interface returns selected-date status, effective Schedule, Score, Streak/Gap, and frequency progress for a supplied date-bounded input.
- [x] The timeline excludes Entries after the Selected date and respects creation-date visibility.
- [x] A Schedule effective-date boundary is represented in the timeline input and older dates use the older Schedule.
- [x] `HabitsViewModel` adapts the timeline result instead of repeating selected-date derivation and frequency/Score/Streak orchestration.
- [x] Existing homepage Entry actions still write to the Selected date.
- [x] JVM contract tests cover empty, incomplete, skipped, daily, non-daily, numeric target-kind, historical, and schedule-boundary behavior.
- [x] Existing homepage behavior is verified on-device for today, a past date, logging/editing, and return to today.

## Comments

- Implemented through the pure Habit timeline seam and homepage adapter. `./gradlew test` passes. On-device verification covered today's homepage, Aug 15 history, future-entry exclusion, Gym frequency progress, and return-to-today navigation.
