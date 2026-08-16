# 02 — Expose one Room-backed observation stream

**What to build:** Make the repository expose one domain observation stream that the homepage and detail adapters can consume without independently combining raw Habit, Entry, and Schedule streams.

**Blocked by:** 01 — Build the domain Habit observation

**Status:** done

- [x] Map the real Room streams into the domain observation input.
- [x] Route both homepage and detail ViewModels through the shared observation stream.
- [x] Preserve selected-date actions, historical scores, analytics inputs, and existing UI state shapes.
- [x] Existing coroutine contracts pass against the shared stream; Room-backed snapshot mapping is covered by the repository instrumentation harness.

## Comments

- Implemented in the current architecture pass. `HabitRepository.observeHabitSnapshot` combines Room habit metadata, entries, schedules, and the selected-date flow, then delegates to the domain projection.
- Homepage and detail ViewModels now consume the same repository snapshot. Detail history is bounded by the effective selected date, capped by the explicit as-of date, so historical views cannot show future entries.
- Verification: `./gradlew test` and `./gradlew :app:connectedDebugAndroidTest` passed; `HabitRepositoryRoomTest.observationStreamMapsRoomRowsAndSelectedDateBoundary` covers Room-to-snapshot mapping.
