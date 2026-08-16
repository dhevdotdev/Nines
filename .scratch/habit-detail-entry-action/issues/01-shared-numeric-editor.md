# 01 — Extract and reuse the shared numeric Entry editor

**What to build:** Make the existing expressive numeric Entry editor reusable by the homepage and other Entry-editing surfaces, while preserving its current value/Target/date context and adding the approved empty-unlogged behavior.

**Blocked by:** None — can start immediately

**Status:** done

- [x] Extract the existing homepage editor behind an explicit shared interface.
- [x] Keep Habit identity, Selected date, current value, unit, Target, validation, and Save/Cancel context.
- [x] Start unlogged Entries empty while pre-filling existing values.
- [x] Keep zero and values above an `AT_MOST` Target valid.
- [x] Preserve homepage behavior and tests.
