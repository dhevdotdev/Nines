# 01 — Extract pure detail analytics

**Blocked by:** None

**Status:** done

- [x] Add a domain-only analytics input/model and computation function.
- [x] Route detail ViewModel analytics fields through the new model.
- [x] Keep UI copy and history presentation in the detail adapter.
- [x] Bound every statistic to creation date and explicit as-of date.
- [x] Add plain-JUnit coverage for streak ranking, both date bounds, numeric aggregates, typical time, threshold edges, and record-progress states.
- [x] `./gradlew test` passes.

## Comments

- Implemented in `HabitAnalytics.kt` and adapted by `HabitDetailViewModel`. Analytics now uses the selected date as its as-of boundary, so historical record progress cannot mix selected-date streaks with today's statistics. The new tests cover both date boundaries, streak ranking, completed numeric aggregates, typical time-of-day, threshold edges, and record-progress behavior. `./gradlew test` passes.
