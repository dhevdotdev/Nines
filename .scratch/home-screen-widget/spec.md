Status: ready-for-agent

# Home-screen quick habit surface

## Problem Statement

Nines currently requires a person to open the app before they can see or act
on today's Habits. That adds friction to the habit loop: the person should be
able to glance at what remains, mark a Boolean Habit complete, or begin editing
a measurable Habit from the home screen.

The widget must feel like an extension of Nines, not a separate mini-product.
In particular, measurable Entry editing already has an established editor in
the app. Rebuilding a second editor inside the widget would create divergent
validation, copy, Target context, and save behavior.

## Solution

Add a today-focused, vertically scrollable home-screen widget that presents
the active Habits and their current Entry status. Boolean Habits can be marked
done directly from the widget. Tapping a measurable Habit launches a compact,
draggable Nines quick surface that uses the existing measurable Entry editor.
The quick surface begins as a bottom sheet over the launcher and can expand
into the full Nines app through its drag handle. It is an app activity launched
from the widget, not a system overlay and does not require display-over-other-
apps permission.

The widget and quick surface use the same selected-date domain/application
seam, with the widget always anchored to the device's local today. They reuse
existing Entry actions and the existing editor rather than introducing a
widget-specific persistence or scoring model.

## User Stories

1. As a Nines user, I want to add a Nines widget to my home screen, so that my
   Habits are available without opening the app.
2. As a Nines user, I want the widget to show today's date clearly, so that I
   understand which Entries its status represents.
3. As a Nines user, I want the widget to show my active Habits, so that I can
   see what remains to be done today.
4. As a Nines user, I want the widget to exclude archived Habits, so that the
   surface reflects the active homepage rather than old configuration.
5. As a Nines user, I want the widget to show each Habit's current status, so
   that I can distinguish completed, incomplete, skipped, and unlogged work.
6. As a Nines user, I want the widget to scroll vertically when my Habits do
   not fit in the available height, so that the widget remains useful without
   forcing an arbitrary fixed number of Habits.
7. As a Nines user, I want a clear route from the widget to the full Nines app,
   so that the widget remains a quick surface rather than a dead end.
8. As a Nines user, I want to tap a Boolean Habit in the widget to mark its
   Entry complete, so that a simple action does not require opening Nines.
9. As a Nines user, I want a completed Boolean Habit to update immediately in
   the widget, so that the surface confirms my action.
10. As a Nines user, I want tapping a completed Boolean Habit to use the same
    toggle semantics as the homepage, so that widget logging does not behave
    like a separate kind of Entry.
11. As a Nines user, I want to tap a measurable Habit to edit its value, so
    that the widget supports the action that measurable Habits actually need.
12. As a Nines user, I want measurable editing to use the same editor as the
    homepage and detail screen, so that the Habit identity, date, unit, Target,
    validation, Save, and Clear behavior remain consistent.
13. As a Nines user, I want the measurable editor launched from the widget to
    appear as a compact Nines surface over the launcher, so that the transition
    feels like a natural continuation of the widget interaction.
14. As a Nines user, I want to drag the quick surface upward, so that I can
    expand from the quick summary into the full Nines app without restarting
    the interaction.
15. As a Nines user, I want to dismiss the quick surface with Back or a
    downward gesture, so that I can return to the launcher quickly.
16. As a Nines user, I want the quick surface to preserve the same dynamic
    color, typography, shapes, and motion language as Nines, so that it does not
    feel like a visually separate product.
17. As a Nines user, I want the quick surface to identify the Habit and today's
    date while editing, so that I do not write a value to the wrong Entry.
18. As a Nines user, I want changes made in the quick surface to appear in the
    widget after saving, so that the widget and app never present contradictory
    state.
19. As a Nines user, I want changes made in the full app to appear in the
    widget, so that the widget remains a trustworthy projection of Nines data.
20. As a Nines user, I want the widget to refresh after the local date changes,
    so that it does not continue showing yesterday's status after midnight.
21. As a Nines user, I want the widget to refresh after device restart, so that
    it remains useful without requiring me to open Nines first.
22. As a Nines user with no active Habits, I want an intentional empty state,
    so that the widget explains how to create my first Habit rather than looking
    broken.
23. As a Nines user who has completed all today's Habits, I want an intentional
    all-done state, so that the widget acknowledges completion and provides a
    route back to Nines.
24. As a Nines user, I want widget interactions to remain safe when Nines'
    process is not currently running, so that quick logging does not depend on
    the app already being open.
25. As a Nines user, I want widget actions to respect the same Entry rules as
    in-app actions, so that a widget cannot create malformed or contradictory
    data.
26. As an accessibility user, I want each Habit and action to have a useful
    spoken label, so that the widget remains understandable with assistive
    technology.
27. As an accessibility user, I want the quick surface's drag handle and
    dismissal behavior to remain discoverable, so that I am not trapped in a
    partial surface.
28. As a developer, I want the widget to consume an existing today-scoped
    Habit projection, so that it does not duplicate scoring, schedule, or Entry
    semantics.
29. As a developer, I want Boolean widget completion and measurable editor Save
    to dispatch existing application actions, so that the homepage, detail
    screen, and widget cannot silently drift apart.
30. As a developer, I want widget updates to be driven by repository changes
    and date boundaries, so that refresh behavior is deterministic and does not
    rely on arbitrary polling.
31. As a developer, I want the quick surface to reuse the existing measurable
    editor presentation and state contract, so that future editor changes apply
    to every entry point.
32. As a developer, I want widget behavior to be testable at the projection and
    action seams, so that platform rendering tests do not become the only proof
    of correctness.

## Implementation Decisions

- The widget is a today-focused collection/control hybrid. It displays active
  Habits, supports vertical scrolling, and exposes direct actions for the
  small set of interactions that are safe and meaningful outside the app.
- The widget uses the device's local today. It does not expose historical date
  navigation or a second selected-date control; historical review remains an
  in-app responsibility.
- Boolean Habits support direct completion toggling from the widget. The action
  uses the existing Entry semantics for today's date and updates the widget
  projection after persistence.
- Measurable Habits launch the existing measurable Entry editor through a
  compact Nines quick surface. The editor's validation, Target context, unit,
  Save, Clear, and selected-date behavior are not reimplemented in the widget.
- The quick surface is an app-owned activity presented as a draggable Material
  bottom-sheet-like surface over the launcher. It does not use
  `SYSTEM_ALERT_WINDOW` or ask for display-over-other-apps permission.
- Expanding the quick surface navigates into the normal full Nines experience;
  it does not create a second full-app navigation stack or a separate detail
  screen variant.
- Widget rendering is an adapter over an existing today-scoped presentation
  projection. It must not calculate Score, Streak, Gap, frequency progress, or
  Target meaning independently from the domain/application layer.
- Archived Habits are excluded. Habits with no Entry, completed Entries,
  skipped Entries, and measurable progress retain their existing meanings.
- Empty and all-done states are explicit widget states, not accidental blank
  layouts.
- Widget updates respond to relevant persisted Habit/Entry changes and local
  date boundaries. The implementation must also handle process recreation and
  device restart without requiring the user to launch the app first.
- The widget and quick surface remain visually subordinate to the full app:
  they use Nines' existing Material 3 Expressive color, typography, shape, and
  motion system without inventing a widget-only design language.
- The initial feature does not define the lifetime-unlock entitlement or
  billing implementation. Premium gating is a separate product decision and
  must not contaminate the widget's data/action seam.

## Testing Decisions

- Tests should verify observable behavior and domain/application contracts, not
  implementation details of the widget host.
- The highest-value seam is the today-scoped widget projection plus the existing
  Entry actions. It should cover active/archive filtering, status rendering
  inputs, Boolean toggling, measurable-editor routing, empty state, all-done
  state, and local-date rollover behavior.
- Existing pure scoring and timeline tests remain the prior art for date-scoped
  and schedule-aware behavior. Widget tests must consume those projections
  rather than reproduce scoring fixtures in a UI layer.
- Existing repository/Room contract tests should verify that widget actions
  persist Entries through the same repository path as the app.
- Compose or widget semantics tests should cover accessible Habit labels,
  Boolean action labels, measurable-editor launch, and full-app expansion.
- Connected device smoke must verify adding the widget, vertical scrolling,
  Boolean completion while the app process is stopped, measurable editing,
  save/update propagation, drag expansion, dismissal, reboot/date refresh, and
  the empty/all-done states where practical.
- Widget rendering should be checked on the connected portrait device with
  launcher and Recents visible. A black or missing launcher background in the
  quick surface or Recents snapshot is a defect unless it is proven to be only
  a system thumbnail artifact.

## Out of Scope

- Historical date selection from the widget.
- Rebuilding the measurable Entry editor specifically for the widget.
- A separate widget-only visual language, data model, scoring implementation,
  or persistence path.
- Display-over-other-apps permission or a system-level overlay service.
- Categories, filters, habit selection rules, or pinned-habit configuration.
- Multiple widget instances with independent configurations.
- Advanced widget customization, themes, or user-selected layouts.
- Export/import, billing, lifetime-unlock enforcement, or other monetization
  work.
- Notifications or reminder scheduling; those remain the existing Reminder
  feature.
- Wear OS, lock-screen widgets, Android Auto, or other non-home-screen
  surfaces.

## Further Notes

Android's widget model supports vertically scrolling collection widgets and
direct interactions such as marking an item complete, which makes this feature
more than a decorative shortcut. The quick surface is deliberately treated as
an app entry point with a bottom-sheet presentation, so the platform boundary
is explicit and the existing measurable editor remains the single source of
editing behavior.
