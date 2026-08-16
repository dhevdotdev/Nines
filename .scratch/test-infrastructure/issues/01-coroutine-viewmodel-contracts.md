# 01 — Add coroutine and ViewModel contract test harness

**Blocked by:** None

**Status:** done

- [x] Add `kotlinx-coroutines-test` and deterministic test scheduling.
- [x] Test selected-date StateFlow changes and return-to-today behavior for homepage/detail adapters.
- [x] Test Entry actions route to the selected date.
- [x] Test missing-habit and Entry-update flow behavior for detail state.
- [x] Keep tests at observable interface boundaries rather than private implementation details.
- [x] `./gradlew test` passes.

## Comments

- Added the coroutine test dependency and observable homepage/detail contract tests using deterministic DAO doubles. Tests cover selected-date changes, return to today, missing habits, and selected-date action routing.
