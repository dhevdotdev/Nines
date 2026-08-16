# 02 — Historical and schedule-version frequency progress

**What to build:** Make frequency progress truthful while browsing history. The progress window must end on the Selected date, exclude Entries after that date, and use the Schedule effective on that date rather than the schedule currently active today. A user changing the Schedule must not rewrite the meaning of historical progress.

**Blocked by:** None — ticket 01 is done.

**Status:** done

- [x] Selecting a past date recalculates boolean and numeric frequency progress for that date's window.
- [x] Entries after the Selected date never contribute to historical progress.
- [x] The Schedule effective on the Selected date determines the window and target.
- [x] A Schedule change creates an observable boundary where dates before it use the old target and dates on/after it use the new target.
- [x] Returning to today restores today's frequency progress without disturbing existing Entry, Score, Streak, or Gap behavior.
- [x] Plain JUnit tests cover historical windows and differing Schedule targets; schedule-version selection is verified through the existing effective-date repository path and on-device historical browsing.
- [x] On-device verification covers the day rail/calendar, past-date progress, schedule editing, and return-to-today behavior.

## Comments

- Implemented by resolving the Schedule through `scheduleEffectiveOn(selectedDate)` before computing progress. `./gradlew test` passes, and on-device historical browsing confirmed future Entries do not leak into the selected date.
