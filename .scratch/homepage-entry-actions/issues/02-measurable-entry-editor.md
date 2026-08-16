# 02 — Redesign measurable Entry editor

**What to build:** Replace the weak measurable exact-value dialog with a compact expressive modal bottom sheet for editing the selected date's numeric Entry.

**Blocked by:** 01 — Simplify homepage Entry action grammar.

**Status:** done

- [x] The editor identifies the Habit and explicit Selected date.
- [x] The current numeric value is visually prominent and editable.
- [x] The unit is shown when present.
- [x] The Target is shown as read-only context and respects `AT_LEAST` / `AT_MOST`.
- [x] Cancel dismisses without writing; Save writes the selected date's value and dismisses.
- [x] The editor has clear accessibility labels and readable dynamic-color contrast.
- [x] On-device verification covers today, a historical date, missing value, and Target display; the no-Target branch is represented by the conditional UI path.
