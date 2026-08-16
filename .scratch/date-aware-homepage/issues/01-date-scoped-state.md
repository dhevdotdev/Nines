# 01 — Date-scoped state and pure visibility logic

**What to build:** The data/state foundation for a date-aware homepage. `HabitsUiState` gains a `selectedDate`, defaulting to today. Every habit row's completion state, numeric value, score, and streak get computed against `selectedDate` (reusing the existing date-parameterized scoring functions) instead of hardcoded "today." A new action selects a date; the existing toggle/increment/set-value actions write to `selectedDate` instead of always today. A new pure function decides whether a habit is visible on an arbitrary date, based on when it was created. No new UI in this ticket — with `selectedDate` defaulting to today, the app behaves exactly as it does now.

**Blocked by:** None — can start immediately.

**Status:** done

- [x] `HabitsUiState` includes a `selectedDate` field defaulting to today
- [x] A new pure function determines whether a habit is visible on a given date based on its creation date, colocated with and tested the same way as the existing scoring package's pure functions
- [x] Each habit row's completion state, numeric value, score, and streak are computed against `selectedDate` (using the existing date-parameterized score/streak functions) instead of hardcoded "today"
- [x] A date-selection action exists and updates `selectedDate`
- [x] The existing toggle/increment/set-numeric-value actions write to `selectedDate` instead of always today
- [x] Field names on the per-habit list item that previously implied "today" specifically are renamed to reflect an arbitrary selected date
- [x] New automated tests cover the visibility function and at least one non-today score/streak case, following the existing scoring-package test file's plain-JUnit, given-inputs/assert-outputs style
- [x] With no UI changes yet, the app is confirmed on-device to behave identically to before this ticket (regression check)

## Comments

Done — commit `ea98401`. Unit tests green, on-device regression check passed (toggle + render identical to before).
