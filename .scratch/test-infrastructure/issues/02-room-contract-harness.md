# 02 — Add real Room persistence contract tests

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Add the Room test dependency and an in-memory `NinesDatabase` fixture.
- [ ] Cover habit, schedule-version, Entry, archive, and ordering contracts through real DAOs/repository.
- [ ] Cover schedule-aware historical Score integration through the repository.
- [ ] Ensure database lifecycle cleanup is deterministic.
- [ ] `./gradlew test` and the relevant Android test task pass.
