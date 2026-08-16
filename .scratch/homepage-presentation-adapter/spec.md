# Homepage presentation adapter

**Status:** done

## Problem

The homepage ViewModel still translates a repository observation into a
`HabitListItem` inline. The observation seam centralizes date and scoring meaning,
but the UI adapter remains embedded in a Flow chain, so a change to homepage row
presentation requires navigating through stream plumbing.

## Decision

Introduce one pure homepage adapter that accepts the repository snapshot and
returns the visible `HabitListItem` or `null`. Keep date/scoring derivation in the
shared observation and keep repository/domain modules independent of UI types.

## Non-goals

- No new repository-to-UI dependency.
- No changes to visibility, selected-date semantics, or row appearance.
- No EWMA caching or memoization.

## Acceptance criteria

- `HabitsViewModel` delegates row translation to one named adapter.
- The adapter is covered with plain-JUnit behavior tests for invisible habits,
  completed entries, numeric targets, and frequency progress.
- The homepage connected behavior remains unchanged.
