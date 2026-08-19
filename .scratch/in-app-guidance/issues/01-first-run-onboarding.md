# 01 — First-run onboarding flow

**What to build:** Show a polished four-page introduction on a fresh installation so a new user understands Nines' core mental model before creating a Habit.

**Blocked by:** None — can start immediately.

**Status:** ready-for-agent

- [ ] A fresh installation opens four pages covering welcome, Entries and Targets, Score/Streaks/Insights, and getting started.
- [ ] Each page has a focused animated Compose preview using deterministic illustrative data, not interactive fake controls or literal screenshots.
- [ ] Next, horizontal swipe, Back, Skip, and Get started follow the approved behavior, with a subtle progress indicator.
- [ ] Skip and Get started land on the real homepage and prevent automatic onboarding on later launches.
- [ ] Onboarding uses local DataStore Preferences state, dynamic color, accessibility semantics, and reduced-motion behavior.
- [ ] JVM/Compose semantics coverage and a connected fresh-install smoke flow verify the user-visible behavior.
