# Reminders

Status: needs-triage

Notification-based reminders for habits — never scoped or started. Deprioritized repeatedly during early development in favor of getting the core tracking experience right first.

Explicit intent: not a dismissible, easy-to-swipe-away notification. Should behave as a strong, persistent reminder — stays until the user actually takes action (completes the habit, or explicitly defers/snoozes it), not something cleared by a stray swipe.

No design decisions made yet: per-habit vs. global reminder times, whether reminders respect the versioned `Schedule` (see `CONTEXT.md`), what "taking action" means for numeric habits, and how the persistent-notification behavior interacts with Android's notification permission model and battery/background-work restrictions are all open.

## Comments
