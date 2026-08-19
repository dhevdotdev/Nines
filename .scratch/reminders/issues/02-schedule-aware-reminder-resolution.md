# 02 — Schedule-aware numeric and flexible Reminder resolution

**What to build:** Extend the daily Reminder path so every Habit kind and its active Schedule/Target
determine whether a Reminder is due and whether an existing due Reminder has been resolved.

**Blocked by:** 01 — Daily boolean Reminder.

**Status:** ready-for-agent

- [ ] Numerical Reminders open the existing value editor; `AT_LEAST` resolves only when its Target is
      met, while `AT_MOST` resolves once the value is recorded.
- [ ] Current and historical Schedule versions, flexible Targets, early completion, and the next due
      date govern delivery consistently rather than treating every Habit as daily.
- [ ] Tomorrow Snooze returns at the original configured local time only when the Habit is next due.
- [ ] Plain-JUnit decision coverage and connected behavior checks verify representative Boolean and
      numerical Habits without changing Score, Streak, Gap, or Habit timeline semantics.

