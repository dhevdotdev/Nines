# Nines onboarding and in-app metric guidance

**Status:** ready-for-agent

## Problem Statement

Nines surfaces a growing set of derived values and visual indicators, but a
person seeing them for the first time has no explanation of what they mean or
how to use them. Score, Streak, Gap, Momentum, Target progress, frequency
progress, and calendar dot states are meaningful parts of the Nines model, but
their meaning is currently discoverable only by already understanding the
product.

The first launch has the same problem at a larger scale. A new user arrives at
an empty homepage without a clear mental model for Entries, Targets, Score,
Streaks, or Insights. A short, beautiful introduction should explain the model
before asking the user to create a Habit.

## Solution

Add two related but distinct guidance surfaces:

1. A four-page first-run onboarding flow shown once after a fresh installation.
2. Lightweight contextual explanations attached to complex metrics and
   indicators throughout the existing screens.

The onboarding teaches the core mental model with focused, animated Compose
previews using fixed illustrative data. It does not use literal screenshots or
interactive fake controls. After onboarding, the user lands on the normal empty
homepage and creates a real Habit through the existing flow.

Contextual guidance explains individual metrics in plain language when the user
encounters them. It uses a consistent lightweight interaction, such as an info
affordance or tappable metric opening a compact M3 Expressive explanation
surface. It does not replace the existing metric presentation or invent fake
data.

## User Stories

1. As a first-time user, I want to understand what Nines is for before I create
   a Habit, so that the empty homepage feels intentional rather than confusing.
2. As a first-time user, I want to see how a Habit Entry changes its status, so
   that I understand what logging means.
3. As a first-time user, I want to understand how a Target relates to an Entry,
   so that I know what completion means for Boolean and measurable Habits.
4. As a first-time user, I want to understand that Score represents gradual
   compliance rather than a simple streak count, so that I interpret it correctly.
5. As a first-time user, I want to understand why Streaks and Insights appear,
   so that I know what value consistent logging creates.
6. As a first-time user, I want each onboarding page to show a focused visual
   preview, so that I can learn by seeing the behavior rather than reading a
   manual.
7. As a first-time user, I want onboarding animations to feel expressive and
   polished, so that the product feels inviting from the first launch.
8. As a first-time user, I want to move forward with Next or a horizontal swipe,
   so that onboarding works for both deliberate and exploratory navigation.
9. As a first-time user, I want to go back to a previous page, so that I can
   review something I missed.
10. As a first-time user, I want to skip onboarding, so that I am not blocked
    from using Nines.
11. As a first-time user, I want onboarding to end at the real empty homepage,
    so that my first action uses the actual Add Habit flow.
12. As a returning user, I want onboarding not to reappear after I skip or
    complete it, so that normal launches remain fast.
13. As a returning user, I want to replay “How Nines works” from Settings, so
    that I can revisit the mental model later.
14. As a user viewing a Score, I want a short explanation of what the value
    represents, so that I do not mistake it for a flat percentage.
15. As a user viewing a Streak or Gap, I want to know how the run is defined,
    so that I understand what actions affect it.
16. As a user viewing Momentum, I want to understand that it describes recent
    direction rather than a permanent rating, so that I read the trend correctly.
17. As a user viewing Target progress, I want to know how actual Entries are
    compared with the Target, so that the progress indicator is actionable.
18. As a user viewing a history dot or calendar state, I want to know what each
    visual state means, so that the calendar is readable without guesswork.
19. As a user who prefers reduced motion, I want the explanation and controls to
    remain clear without decorative animation, so that onboarding stays usable.
20. As a user who changes theme or uses dynamic color, I want onboarding and
    guidance to use the same visual system as the rest of Nines, so that they feel
    like part of the app.

## Implementation Decisions

- The feature is split into first-run onboarding and contextual metric guidance.
  They share terminology and visual language but are not the same screen or
  state machine.
- First-run onboarding contains four pages:
  1. Welcome to Nines.
  2. Log Entries against Targets.
  3. See Score, Streaks, and Insights.
  4. Get started.
- Each page has a focused, live Compose preview with fixed illustrative data.
  Previews are illustrative and guided by animation; users do not interact with
  fake controls during onboarding.
- The flow supports horizontal swipes and explicit Next navigation. The bottom
  action changes to Get started on the final page.
- A visible Skip action appears in the top-right on every page. Android Back
  navigates to the previous page; Back on the first page exits to the homepage.
- A subtle progress indicator shows the current page.
- Onboarding appears on a fresh installation after the theme is ready and before
  the empty homepage is shown. It does not reappear merely because the user
  later has no Habits.
- Skip and Get started both prevent automatic onboarding on later launches.
  Completion is stored locally using DataStore Preferences. No Room schema
  change, account dependency, or server state is needed.
- Settings gains a single “How Nines works” entry that reopens the same flow.
  Replaying it does not reset the first-run completion state.
- Future app updates use a separate, lightweight “What’s new” surface when
  needed. The onboarding flow is not automatically versioned or forced again.
- Contextual guidance uses a consistent lightweight info/tap affordance and a
  compact M3 Expressive explanation surface. It explains the metric in plain
  language, with optional deeper explanation where useful, without exposing
  implementation jargon as the default.
- The first contextual guidance set covers Score, Streak, Gap, Momentum, Target
  progress, frequency progress, and history/calendar dot states.
- Onboarding and guidance use the existing dynamic color, typography, motion,
  shape, haptic, and accessibility conventions. Reduced-motion settings remove
  or shorten decorative transitions while preserving content and navigation.
- Onboarding preview content is deterministic and independent of the user's
  Habits or Entries.

## Testing Decisions

- Tests verify externally visible behavior and state transitions, not individual
  animation frames or private composable structure.
- A small onboarding completion-store contract covers fresh state, Skip,
  Get started, and replay without resetting first-run completion.
- Compose semantics tests cover page content, Next, swipe/back behavior, Skip,
  Get started, contextual explanation labels, and reduced-motion content.
- A connected smoke test clears app data, verifies first launch enters
  onboarding, completes or skips it, verifies the homepage on relaunch, then
  opens Settings and replays the flow.
- The tests should reuse the existing Compose semantics and connected smoke
  infrastructure. A fake in-memory store may be used at the UI seam; the
  DataStore implementation gets a small Android contract test.
- Existing UI and repository tests remain unchanged unless their public
  behavior is affected by the new onboarding entry point.

## Out of Scope

- Habit categories, grouping, or filters.
- A home-screen widget, reminders, export/import, or weekly activity overview.
- A forced onboarding replay after future app updates.
- A separate technical documentation system for the EWMA or decay model.
- Interactive fake Habit creation or logging inside onboarding previews.
- Personalized onboarding based on existing user data.
- Reworking the existing homepage, Detail screen, calendar, or Insights metrics
  beyond adding the approved guidance affordances.

## Further Notes

The onboarding previews should be designed against real device density and
dynamic color. Avoid full-screen screenshot mockups with tiny text; each preview
should focus attention on one concept. The onboarding should feel like Nines,
not a generic product tour.
