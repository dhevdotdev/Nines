# Screen presentation modules

**Status:** done

## Problem

`HabitsScreen.kt` and `HabitDetailScreen.kt` each combine screen orchestration with
large, independently changing presentation modules: swipe/reorder interaction,
habit editing, calendar dialogs, and analytics sections. This makes visual changes
hard to localize and makes the screen seams difficult to test without compiling
the entire monolith.

## Goal

Split the two screen modules along user-facing presentation responsibilities while
keeping the existing screen interfaces, state, actions, Material 3 Expressive
visual behavior, and navigation unchanged.

## Scope

- Extract the homepage habit editor and its frequency/value helpers into a
  dedicated presentation module.
- Extract the detail calendar/dialog presentation into a dedicated module.
- Extract the detail insights presentation and its small formatting helpers into
  a dedicated module.
- Keep screen-level orchestration, state ownership, gesture policy, and action
  dispatch in the existing screen files.
- Preserve package-private behavior and avoid introducing new runtime seams or
  test doubles solely to make file sizes smaller.

## Non-goals

- No visual redesign, spacing changes, or motion changes.
- No ViewModel, repository, scoring, or persistence changes.
- No new UI test framework; existing Compose semantics and connected tests remain
  the verification seam.

## Acceptance criteria

- `HabitsScreen.kt` and `HabitDetailScreen.kt` retain only orchestration and their
  directly screen-specific layout.
- Extracted modules have narrow parameter surfaces and contain no repository or
  ViewModel access.
- Existing JVM and connected Android tests pass unchanged.
- A code review confirms the split improves locality rather than creating
  pass-through middle-man files.
