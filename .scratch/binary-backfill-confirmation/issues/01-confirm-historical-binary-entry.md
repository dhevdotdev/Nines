# Confirm historical binary Entry changes

**Category:** bug

**Status:** needs-triage

## Reported problem

When editing a past date for a yes/no Habit, a single tap immediately changes that date’s Entry to
done or not done. Historical corrections are easy to make accidentally and currently have no
confirmation step.

## Scope to clarify during triage

- Whether confirmation is required for both marking a past Entry done and marking it not done.
- Whether this applies only to the detail-history calendar/editor, or to every binary interaction
  while the homepage or detail screen has a selected date before today.
- The confirmation language and whether it should state the affected date and resulting Entry state.

## Constraints

- Today’s normal binary logging should remain one-tap fast.
- A confirmation must not change the selected date, score derivation, streak/gap derivation, or the
  existing numeric Entry editor.
