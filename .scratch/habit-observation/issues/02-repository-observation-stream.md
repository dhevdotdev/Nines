# 02 — Expose one Room-backed observation stream

**What to build:** Make the repository expose one domain observation stream that the homepage and detail adapters can consume without independently combining raw Habit, Entry, and Schedule streams.

**Blocked by:** 01 — Build the domain Habit observation

**Status:** done

- [x] Map the real Room streams into the domain observation input.
- [x] Route both homepage and detail ViewModels through the shared observation stream.
- [x] Preserve selected-date actions, historical scores, analytics inputs, and existing UI state shapes.
- [x] Existing coroutine contracts pass against the shared stream; Room-backed stream mapping remains covered by the repository instrumentation harness.

## Comments

- Implemented in the current architecture pass. `HabitRepository.observeHabitObservation` combines Room habit metadata, entries, schedules, and the selected-date flow, then delegates to the domain projection.
- Homepage and detail ViewModels now consume the same timeline/entry projection. Detail history is bounded by its explicit real-today/as-of date rather than the selected historical date.
- Verification: `./gradlew :app:testDebugUnitTest --tests dev.dhev.nines.scoring.HabitObservationTest --tests dev.dhev.nines.ui.habits.HabitViewModelContractTest` passed.
