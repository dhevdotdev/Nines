# 04 — Combine simultaneous Reminders

**What to build:** Let a person who prefers fewer interruptions optionally combine multiple Habits
due in the same scheduled minute into one Nines notification, while preserving individual delivery
as the default.

**Blocked by:** 03 — Reliable Reminder lifecycle and recovery.

**Status:** ready-for-agent

- [ ] Individual ongoing notifications remain the default and retain their existing actions.
- [ ] A persisted global preference combines same-minute due Reminders into one notification that
      opens Nines.
- [ ] The combined notification offers no bulk Mark done, Snooze, or Not today action; each Habit’s
      Reminder remains independently unresolved until acted on.
- [ ] Preferences, grouping boundaries, accessibility labels, and restoration behavior have focused
      automated and connected-device coverage.
