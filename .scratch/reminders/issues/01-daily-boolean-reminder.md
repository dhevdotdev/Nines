# 01 — Daily boolean Reminder

**What to build:** Let a person opt into a local-time Reminder while editing a daily yes/no Habit,
then receive one ongoing, actionable notification that lets them Mark done, choose Not today, or
Snooze without first opening Nines.

**Blocked by:** None — can start immediately.

**Status:** done

- [x] A daily yes/no Habit can opt into one local reminder time from Habit editing; no permission is
      requested until the person attempts their first enablement.
- [x] A due Reminder is ongoing, alerts once, is not resolved by a swipe, and remains associated with
      its original date until the person acts.
- [x] Mark done creates the existing `COMPLETED` Entry, Not today creates the existing `SKIPPED`
      Entry, and Snooze offers 15 minutes, 1 hour, 3 hours, and tomorrow without creating an Entry.
- [x] A pure due/resolution decision and real Room-backed configuration cover the daily boolean path;
      connected-device checks verify editor, notification actions, and accessible labels.
