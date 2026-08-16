# 01 — Extract tested gesture policy

**Blocked by:** None

**Status:** done

- [x] Add pure policy types and functions for reorder eligibility and swipe action mapping.
- [x] Route the homepage's reorder gate and settled swipe actions through the policy.
- [x] Preserve release-gated anchored drag behavior and existing action meanings.
- [x] Add plain-JUnit coverage for date gating, both directions, no direction, and reorder exclusivity.
- [x] `./gradlew test` passes.

## Comments

- Added `GesturePolicy.kt` as the domain-independent policy seam. `HabitsScreen` still owns `AnchoredDraggableState`, release-gated settling, haptics, and animations; the policy now owns only action meaning and reorder/date exclusivity. The focused policy tests, full JVM suite, and nine connected Android tests pass.
