# 01 — Centralize Streak and Gap analysis

**What to build:** Make the timeline and detail analytics consume one pure Habit run analysis so
their current Streak, best Streak, and longest Gap cannot drift apart.

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] Add the pure `HabitRunAnalysis` projection while preserving existing run semantics.
- [ ] Route Habit timeline and detail analytics through the shared projection.
- [ ] Preserve standalone analytics behavior through the same fallback projector.
- [ ] Add plain-JUnit coverage for run edges, upstream reuse, and timeline/analytics agreement.
- [ ] Run the full JVM and connected verification, review the final diff, and document evidence.
