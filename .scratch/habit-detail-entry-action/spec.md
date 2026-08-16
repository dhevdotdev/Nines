# Shared numeric Entry editor

**Status:** ready-for-agent

## Problem Statement

Numeric habits currently have three different Entry interactions: the homepage
opens an expressive value editor, the detail hero silently increments or
decrements the selected date, and detail history opens a simpler numeric dialog.
The same numeric Entry therefore has different affordances and different context
depending on where it is edited.

## Solution

Extract the existing homepage numeric Entry editor into one shared presentation
module and use it from the homepage row, detail hero, and detail history/calendar.
The detail numeric hero becomes the same compact expressive edit action used by
the homepage. Boolean habits keep their direct check-off action.

The editor always operates on the explicit Selected date. It shows habit identity,
Selected date, current value, unit, and Target. Unlogged Entries start empty;
existing Entries are prefilled. Save accepts zero and positive decimals, including
values above an `AT_MOST` Target, and remains disabled for empty/invalid input.
An explicit Clear entry action deletes an existing Entry and restores `Not logged`,
with an undo Snackbar. Empty Save never means clear.

## User Stories

1. As a user, I want the homepage numeric edit action to open the same editor as detail, so that numeric Entry editing has one grammar.
2. As a user, I want the detail numeric hero to say Edit rather than silently increment, so that tapping it never changes data unexpectedly.
3. As a user, I want detail history/calendar numeric editing to use the same editor, so that correction feels consistent everywhere.
4. As a user, I want boolean habits to remain one-tap check-off habits, so that numeric precision does not complicate boolean logging.
5. As a user, I want the editor to identify the Habit and Selected date, so that I know exactly which Entry I am changing.
6. As a user, I want the editor to show the current value and unit, so that I can correct an existing Entry confidently.
7. As a user, I want the editor to show the Target and whether it is an at-least or at-most rule, so that the value has meaning while I edit it.
8. As a user, I want an unlogged numeric Entry to open with an empty field, so that not logged is not confused with zero.
9. As a user, I want an existing numeric Entry to be prefilled, so that small corrections are quick.
10. As a user, I want to enter zero, so that zero remains a legitimate numeric value.
11. As a user, I want to enter a value above an at-most Target, so that an honest violation is recordable rather than hidden by input validation.
12. As a user, I want Save disabled for empty or invalid input, so that malformed Entries cannot be submitted.
13. As a user, I want to clear an existing Entry explicitly, so that I can return a date to not logged.
14. As a user, I want clearing to be undoable, so that an accidental clear is recoverable without a confirmation interruption.
15. As a user, I want saving or clearing a past Selected date to keep me on that date, so that historical correction does not unexpectedly return me to today.
16. As a user, I want the edit action to look like the existing expressive homepage action, so that numeric and boolean card actions feel related.
17. As an accessibility user, I want the hero action to announce that it edits the named Habit's value for the Selected date, so that its purpose is unambiguous.

## Implementation Decisions

- The existing homepage numeric editor is the source design and behavior; it is
  extracted and shared rather than reimplemented.
- The shared editor is a Compose presentation module with explicit value, Target,
  unit, Habit identity, Selected date, save, clear, and dismiss inputs/callbacks.
- The editor keeps the existing M3 Expressive bottom-sheet treatment, animated
  value feedback, Target context, and Save/Cancel structure.
- The numeric hero uses the existing 44dp expressive edit action grammar: edit icon
  in both logged and unlogged states, outline when unlogged, and filled
  Cookie9Sided shape when an Entry exists.
- Numeric editing always dispatches the explicit Selected date. Boolean editing
  continues to dispatch the existing direct toggle action.
- Clearing deletes the Entry row. It never writes zero, `SKIPPED`, or another
  placeholder Entry. Undo restores the deleted Entry through the same domain
  action semantics.
- Numeric input is non-negative and decimal; Target is not an input ceiling.
- No scoring, Schedule, or persistence meaning changes beyond the explicit delete
  and restore required for Clear entry.
- The existing detail-only numeric dialog is removed after all callers migrate.

## Testing Decisions

- Test the shared editor through existing Compose semantics seams: labels,
  selected-date/Target context, Save enabled state, clear action, and action
  callbacks. Do not test private Compose structure.
- Extend ViewModel/repository contract coverage for SetValue, clear, undo, and
  historical Selected-date preservation.
- Preserve existing numeric scoring/timeline tests; they remain the domain seam
  proving that input UI changes do not alter compliance semantics.
- Run the full JVM suite and connected Android suite, including existing homepage,
  detail, and Room/Compose contracts.

## Out of Scope

- New `+`/`−` controls inside the editor.
- Changing boolean Entry behavior.
- Changing Score, Streak, Gap, Schedule, Target, or frequency semantics.
- Redesigning the homepage/detail cards beyond replacing the misleading numeric
  hero affordance.
- A new numeric editor design separate from the existing homepage editor.

## Further Notes

This work closes a real presentation inconsistency while preserving the domain
model: an absent Entry remains distinct from numeric zero, and a value above an
`AT_MOST` Target remains recordable so the app can show the violation honestly.
