# 01 — Extract pure detail analytics

**Blocked by:** None

**Status:** done

- [x] Add a domain-only analytics input/model and computation function.
- [x] Route detail ViewModel analytics fields through the new model.
- [x] Keep UI copy and history presentation in the detail adapter.
- [x] Bound every statistic to creation date and explicit as-of date.
- [x] Add plain-JUnit coverage for streak ranking, date bounds, numeric aggregates, typical time, and record-progress states.
- [x] `./gradlew test` passes.

## Comments

- Implemented in `HabitAnalytics.kt` and adapted by `HabitDetailViewModel`. The new tests cover future-entry exclusion, streak ranking, completed numeric aggregates, typical time-of-day, and record-progress behavior. `./gradlew test` passes.
