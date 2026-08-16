# 03 — Align selected-date presentation and close the observation path

**What to build:** Ensure detail labels, calendar highlighting, history status, and “today” treatment use the same explicit date context as the shared observation, then verify homepage/detail agreement on-device.

**Blocked by:** 02 — Expose one Room-backed observation stream

**Status:** done

- [x] Remove direct presentation-time `LocalDate.now()` decisions where the selected/as-of date is the relevant context.
- [x] Preserve real-today treatment where it is intentionally distinct from the selected date.
- [x] Carry the ViewModel's explicit real-today boundary into detail presentation helpers and calendar markers.
- [x] Run the full JVM suite, Room/Compose instrumentation, and the connected client smoke suite; the historical agreement contract is covered at domain/Room layers because the current device suite has no detail-navigation fixture.
- [x] Update the spec/ticket comments with evidence and run the two-axis code review before closing.

## Comments

- Implemented in the current architecture pass. Detail UI state now carries the ViewModel's captured `today`; streak labels, calendar markers, upcoming/not-logged copy, and expanded history editing all use that boundary.
- Selected date remains the timeline/analytics/calendar context, while real today remains the future/today visual boundary. This distinction is now explicit instead of being reconstructed independently in composables.
- Verification: `./gradlew test` passed; `./gradlew :app:connectedDebugAndroidTest` passed all 9 device tests. No new UI-specific historical test was invented because the existing device suite has no detail navigation fixture; the domain and Room contracts cover the boundary directly.
