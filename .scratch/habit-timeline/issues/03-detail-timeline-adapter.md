# 03 — Route detail state through the Habit timeline

**What to build:** Make the detail screen consume the shared Habit timeline for its date-aware Entry, Schedule, Score, Streak, Gap, and completion-derived inputs, while leaving detail-specific analytics and presentation unchanged for the later analytics candidate.

**Blocked by:** 02 — Make historical Score schedule-correct.

**Status:** ready-for-agent

- [x] `HabitDetailViewModel` consumes the timeline result rather than duplicating shared Entry/Schedule/Score/Streak/Gap derivation.
- [x] Existing detail UiState fields and action routing remain compatible.
- [x] Detail history and selected-date Entry editing retain their current behavior.
- [x] Detail-specific analytics remain outside the shared timeline seam.
- [x] ViewModel contract tests cover missing Habit, Entry updates, date changes, and action routing through the adapter.
- [ ] On-device verification confirms homepage and detail agree for today and a historical date.

## Comments

- The detail adapter now owns an explicit selected date and feeds selected-date state, Score, Streak, target, value, and logging actions through the shared timeline. Numeric cycle labels are intentionally omitted because the timeline's value-based frequency progress is already represented by the numeric target UI. ViewModel contract coverage is supplied by test-infrastructure ticket 01, including missing habits, binary and numeric selected-date actions, Entry updates, date changes, and return-to-today behavior. Homepage/detail agreement verification remains open. `./gradlew test` passes and the detail/history flow was smoke-tested on-device.
