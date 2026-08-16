# 02 — Make historical Score schedule-correct

**What to build:** Correct the Habit timeline's historical Score derivation so a Schedule change affects only dates from its effective date onward. The homepage and any existing detail Score presentation must retain their current meaning while historical ranges stop applying today's/end-date Schedule retroactively.

**Blocked by:** 01 — Route the homepage through the Habit timeline.

**Status:** ready-for-agent

- [ ] A Score range containing a Schedule change uses the Schedule in force on each scored date.
- [ ] Earlier Schedule changes do not alter the interpretation of dates before their effective date.
- [ ] Later Entries and later Schedules cannot leak into a historical Selected-date Score.
- [ ] The timeline contract tests include a schedule-change range with independently expected Score results.
- [ ] Existing Score, Streak, Gap, and frequency semantics remain unchanged outside the historical Schedule correction.
- [ ] On-device history browsing demonstrates the corrected Score before and after a Schedule edit.
