# 01 — Build the domain Habit observation

**What to build:** Create the shared date-bounded Habit observation that describes selected-date state and bounded calendar-day data consistently for any caller.

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] Define domain observation input/output using explicit selected and as-of dates.
- [ ] Compose the existing Habit timeline without changing score, streak, gap, or frequency-progress semantics.
- [ ] Produce bounded calendar-day observations that exclude pre-creation and post-as-of dates.
- [ ] Add plain-JUnit coverage for visibility, date clipping, schedule boundaries, ordering, and return-to-today inputs.

