# 02 — Add real Room persistence contract tests

**Blocked by:** 01

**Status:** done

- [x] Add the Room test dependency and an in-memory `NinesDatabase` fixture.
- [x] Cover habit, schedule-version, Entry, archive, and ordering contracts through real DAOs/repository.
- [x] Cover schedule-aware historical Score integration through the repository.
- [x] Ensure database lifecycle cleanup is deterministic.
- [x] `./gradlew test` and the relevant Android test task pass.

## Comments

- Added five real Room/repository instrumentation contracts with an in-memory database. The suite verifies creation and ordering, archive/restore history retention, numeric persistence and timeline observation, same-day schedule precedence, and historical score boundaries. `./gradlew test` and `./gradlew :app:connectedDebugAndroidTest` both pass on the connected device.
