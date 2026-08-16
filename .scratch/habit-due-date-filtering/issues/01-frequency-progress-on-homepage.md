# 01 — Frequency progress on the homepage

**What to build:** Show compact rolling-window frequency progress on every active homepage habit card. Boolean habits show explicit completed Entries against their frequency numerator; numeric habits show the aggregate of explicit numeric Entries against the configured target. The result updates when the user logs an Entry or changes the Selected date, while all existing habit visibility, Entry actions, Score, Streak, and Gap behavior remain intact.

**Blocked by:** None — can start immediately.

**Status:** done

- [x] A pure domain seam returns boolean frequency progress for an inclusive window ending on the Selected date.
- [x] The same pure seam returns numeric aggregate progress and respects AT_LEAST and AT_MOST targets.
- [x] Daily and non-daily denominators produce the correct rolling window.
- [x] Only explicit completed or numeric Entries contribute; incomplete and skipped Entries do not count as completions.
- [x] The homepage row state exposes the calculated progress without duplicating the date math in the ViewModel or UI.
- [x] Every active habit remains visible regardless of frequency progress.
- [x] Progress is visually distinct from the Score ring, selected-date Entry state, and Entry action.
- [x] Progress updates after logging/editing an Entry and after selecting another date.
- [x] Plain JUnit tests cover the pure seam; ViewModel entry-state mapping and on-device verification cover incomplete/skipped handling, homepage behavior, and compact M3 Expressive presentation. Daily targets reuse the existing selected-date status rather than repeating a one-day progress line.

## Comments

- Implemented with a pure scoring seam and homepage wiring. `./gradlew test` passes. On-device verification covered logging/editing, selected-date changes, active-habit visibility, and the compact progress presentation.
