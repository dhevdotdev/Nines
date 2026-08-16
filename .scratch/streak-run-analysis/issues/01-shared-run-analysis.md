# 01 — Centralize Streak and Gap analysis

**What to build:** Make the timeline and detail analytics consume one pure Habit run analysis so
their current Streak, best Streak, and longest Gap cannot drift apart.

**Blocked by:** None — can start immediately

**Status:** done

- [x] Add the pure `HabitRunAnalysis` projection while preserving existing run semantics.
- [x] Route Habit timeline and detail analytics through the shared projection.
- [x] Preserve standalone analytics behavior through the same default projector, with range validation.
- [x] Add plain-JUnit coverage for run edges, upstream reuse, and timeline/analytics agreement.
- [x] Run the full JVM and connected verification, review the final diff, and document evidence.

## Comments

- The first implementation commit was `f48b79a`; review hardening removes the dead `currentStreak`
  input, makes run analysis the sole source of timeline streak/gap scalars, and adds date-range plus
  completed-date provenance.
- Final verification: `./gradlew test` passed; connected device verification passed all 10 tests.
