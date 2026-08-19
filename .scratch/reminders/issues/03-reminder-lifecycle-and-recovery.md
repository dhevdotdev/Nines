# 03 — Reliable Reminder lifecycle and recovery

**What to build:** Make configured and unresolved Reminders survive the real lifecycle events that a
person expects: denied permission, app/process recreation, reboot, local-time changes, and Habit
archive/restore.

**Blocked by:** 02 — Schedule-aware numeric and flexible Reminder resolution.

**Status:** done

- [x] Denying notification permission preserves the chosen Reminder and presents a clear in-app path
      to restore delivery later.
- [x] Configured and unresolved Reminders recover after process recreation and reboot with their
      original date intact; future scheduling follows current local time.
- [x] Archiving cancels active and scheduled delivery without losing configuration; restoring resumes
      the configured Reminder.
- [x] Delivery remains best-effort, respects Do Not Disturb, and does not request exact-alarm or
      unrestricted battery access.
- [x] Connected-device verification covers the recoverable lifecycle paths practical on the device.
