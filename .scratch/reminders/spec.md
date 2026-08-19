# Opt-in, schedule-aware Habit Reminders

**Status:** ready-for-agent

## Problem Statement

Nines helps a person record and understand Habits, but it never prompts them at the moment they planned to act. A person must remember to open the app, and cannot safely defer a prompt, choose “Not today,” or recover an unresolved prompt after an app or phone restart.

## Solution

Let a person optionally set one local-time Reminder on each Habit. A due Reminder is a persistent, actionable notification associated with its original date until it is resolved, Snoozed, or explicitly skipped. Reminders respect the Habit’s Schedule and Target, but remain separate from Entries and derived compliance measures.

## User Stories

1. As a person creating or editing a Habit, I want to opt into one local-time Reminder, so that I receive a prompt only for Habits I choose.
2. As a person who does not use reminders, I want every Habit to remain notification-free by default, so that Nines never interrupts me without consent.
3. As a person enabling my first Reminder, I want Nines to ask for notification permission only at that point, so that the request has clear context.
4. As a person with a daily yes/no Habit, I want a due prompt at my chosen local time, so that I can mark it done without first opening Nines.
5. As a person with a numeric Habit, I want the prompt to take me to the existing value editor, so that I can record the correct value rather than force a binary answer.
6. As a person with an `AT_LEAST` Target, I want the prompt to resolve only after meeting the Target, so that partially logged progress is not mistaken for completion.
7. As a person with an `AT_MOST` Target, I want the prompt to resolve after recording that day’s value, so that Nines can derive the Target result without repeatedly demanding a value.
8. As a person with a flexible Schedule, I want Nines to prompt only when the Habit is next due, so that a weekly Target does not behave like a daily obligation.
9. As a person who completes a Habit before its reminder time, I want no due prompt, so that Nines does not notify me about work already resolved.
10. As a person receiving a due prompt, I want it to remain visible when I accidentally swipe it, so that an accidental gesture cannot erase a needed reminder.
11. As a person who cannot act now, I want to Snooze for 15 minutes, 1 hour, 3 hours, or tomorrow, so that I can defer the prompt without altering my Habit data.
12. As a person choosing Not today, I want Nines to create the existing `SKIPPED` Entry, so that my decision is recorded honestly rather than silently discarded.
13. As a person travelling, I want future reminders to follow my current local time, so that a “7:00 PM” reminder remains meaningful after a time-zone change.
14. As a person whose app process is killed or whose phone restarts, I want configured and unresolved Reminders restored, so that Nines does not silently forget them.
15. As a person who archives a Habit, I want active prompts cancelled while its configuration stays recoverable, so that an inactive Habit cannot continue interrupting me.
16. As a person restoring an archived Habit, I want its configured Reminder to resume, so that I do not have to recreate it.
17. As a person who denies notification permission, I want Nines to retain my Reminder setting and show how to recover, so that I can enable delivery later without losing my choice.
18. As a person with several Habits due at the same time, I want individual prompts by default, so that I can act on each Habit precisely.
19. As a person who prefers less notification clutter, I want to optionally combine same-minute prompts into one Nines notification, so that I can open the app without any unsafe bulk action.
20. As a person using Do Not Disturb, I want Nines to respect it, so that a Reminder stays helpful without bypassing my device’s interruption choices.

## Implementation Decisions

- A Reminder is optional and belongs to one Habit. It stores a local time and durable state needed to restore a due or Snoozed Reminder after process recreation or reboot; it is never an Entry.
- Reminder due, resolution, Snooze, and next-delivery decisions are represented through one pure projection over the Habit, its Schedule versions, its Entries, the local clock, and reminder state. That projection is the source of truth for both delivery and in-app recovery messaging.
- The first vertical slice proves the complete daily boolean path. Later slices expand the same interfaces to numerical Habits, flexible Targets, lifecycle recovery, and same-minute combining.
- Reminder scheduling uses best-effort local-time Android delivery. Nines does not request exact alarm access, blanket battery-optimization exemptions, or Do Not Disturb bypass.
- A due individual notification is ongoing and alerts once. Swiping it does not resolve it; resolving it requires an explicit notification action or an appropriate action in Nines.
- Notification actions reuse existing Entry semantics: boolean Mark done creates a `COMPLETED` Entry, Not today creates a `SKIPPED` Entry, numeric Log value opens the existing numeric editor, and Snooze changes only Reminder state.
- Tomorrow Snooze retains the configured local time and schedules only if the Habit is next due.
- Reminders are suspended while a Habit is archived and resumed after restoration, retaining their configuration. A declined notification permission preserves configuration and surfaces recovery.
- Individual notifications are the default. A persisted global preference may combine Habits due in the same minute into one notification that only opens Nines; it has no bulk completion, Snooze, or Not today action.
- Existing Reminder and Snooze vocabulary in `CONTEXT.md` remains authoritative; this feature adds no new scoring terminology and does not alter Score, Streak, Gap, or Habit timeline derivation.

## Testing Decisions

- The primary seam is observable Reminder decisions: given a Habit, versioned Schedule, Entries, configured Reminder, and local time, tests assert whether it is due, resolved, Snoozed, or should next deliver. Tests cover boolean and numerical Habit kinds, both Target kinds, flexible targets, time-zone/local-time boundaries, and original-date preservation.
- Existing plain-JUnit scoring tests establish the preferred style for pure date-aware decisions.
- Existing Room contract tests verify persistence and archive/restore behavior through real DAOs; they extend to reminder configuration and durable due state.
- Existing Compose semantics and connected-device smoke tests verify the editor, permission recovery, notification action routing, and restored visible state. Device checks include reboot/Doze behavior where the platform permits meaningful best-effort verification.
- Tests assert user-observable Reminder and Entry outcomes, not scheduler implementation details.

## Out of Scope

- Multiple times per Habit, a global time that silently applies to every Habit, or reminders enabled by default.
- Exact-alarm permission, bypassing Do Not Disturb, or mandatory unrestricted battery access.
- Bulk Snooze, bulk Not today, or a new skip-day domain model.
- Changing Score, Streak, Gap, Schedule-version semantics, or treating a dismissed notification as an Entry.
- Copying code, assets, or implementation from uHabit or any other reference app.

## Further Notes

- The prior grilled brief remains in `idea.md` as historical decision context.
- The implementation must remain safe on early pre-release schemas today, while making the Reminder persistence boundary explicit enough for proper migrations before public release.
