# 02 — Make historical Score schedule-correct

**What to build:** Correct the Habit timeline's historical Score derivation so a Schedule change affects only dates from its effective date onward. The homepage and any existing detail Score presentation must retain their current meaning while historical ranges stop applying today's/end-date Schedule retroactively.

**Blocked by:** 01 — Route the homepage through the Habit timeline.

**Status:** done

- [x] A Score range containing a Schedule change uses the Schedule in force on each scored date.
- [x] Earlier Schedule changes do not alter the interpretation of dates before their effective date.
- [x] Later Entries and later Schedules cannot leak into a historical Selected-date Score.
- [x] The timeline contract tests include a schedule-change range with independently expected Score results.
- [x] Existing Score, Streak, Gap, and frequency semantics remain unchanged outside the historical Schedule correction.
- [x] On-device history browsing demonstrates that Score remains date-scoped while browsing a past date.

## Comments

- Implemented schedule-aware repository Score ranges using all versions effective through the range end. The pure timeline and Score contract tests cover the effective-date boundary; `./gradlew test` passes. On-device past-date browsing showed the date-scoped historical projection remains usable.
