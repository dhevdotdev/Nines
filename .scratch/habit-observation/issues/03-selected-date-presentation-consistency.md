# 03 — Align selected-date presentation and close the observation path

**What to build:** Ensure detail labels, calendar highlighting, history status, and “today” treatment use the same explicit date context as the shared observation, then verify homepage/detail agreement on-device.

**Blocked by:** 02 — Expose one Room-backed observation stream

**Status:** ready-for-agent

- [ ] Remove direct presentation-time `LocalDate.now()` decisions where the selected/as-of date is the relevant context.
- [ ] Preserve real-today treatment where it is intentionally distinct from the selected date.
- [ ] Add semantics or contract assertions for selected-date and today-state presentation.
- [ ] Run the full JVM suite, Room/Compose instrumentation, and the connected homepage/detail historical-date smoke path.
- [ ] Update the spec/ticket comments with evidence and run the two-axis code review before closing.

