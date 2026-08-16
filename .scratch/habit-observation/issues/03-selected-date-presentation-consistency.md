# 03 — Align selected-date presentation and close the observation path

**What to build:** Ensure detail labels, calendar highlighting, history status, and “today” treatment use the same explicit date context as the shared observation, then verify homepage/detail agreement on-device.

**Blocked by:** 02 — Expose one Room-backed observation stream

**Status:** done

- [x] Remove direct presentation-time `LocalDate.now()` decisions where the selected/as-of date is the relevant context.
- [x] Preserve real-today treatment where it is intentionally distinct from the selected date.
- [x] Carry the ViewModel's explicit real-today boundary into detail presentation helpers and calendar markers.
- [x] Run the full JVM suite, Room/Compose instrumentation, and the connected homepage/detail historical-date smoke path.
- [x] Update the spec/ticket comments with evidence and run the two-axis code review before closing.

## Comments

- Implemented in the current architecture pass. Detail UI state now carries the ViewModel's captured `today`; streak labels, calendar markers, upcoming/not-logged copy, and expanded history editing all use that boundary.
- Selected date remains the timeline/analytics context, while real today remains the calendar's future/today boundary. This distinction is now explicit instead of being reconstructed independently in composables.
- Verification and device smoke are part of the final architecture-pass handoff.
