# Homepage Entry actions and measurable value editor

**Status:** done

## Problem

The homepage currently presents more controls than the daily logging task needs. The measurable card's secondary action opens a small exact-value dialog whose hierarchy is weak and whose Target context is missing even though the data is available. The action grammar also makes Habit/Schedule editing and selected-date Entry editing look too similar.

## Outcome

Make the homepage's action surface unambiguous:

- Boolean habits show a completion control with a check state and a separate Habit/Schedule edit action.
- Measurable habits show one selected-date Entry value action. It opens a polished value editor; there is no separate homepage increment/decrement action.
- The value editor shows the habit identity, the explicit Selected date, the current value, unit, and read-only Target context. It edits only that date's numeric Entry.
- The existing Habit/Schedule editor remains separate and is reached through the boolean edit action or the existing swipe-right gesture.
- Card geometry remains compact and stable; opening or saving the editor must not resize or reorder the list.

## Interaction rules

1. Boolean completion writes the Entry for the selected date and updates the card immediately.
2. Boolean edit opens Habit/Schedule configuration, not an Entry editor.
3. Measurable value action opens Entry editing for the selected date.
4. Saving a measurable value dismisses the editor and updates the card immediately.
5. Target is read-only context in the value editor and respects `AT_LEAST` / `AT_MOST`.
6. Historical selected dates use the same actions and save behavior as today.
7. Card tap still opens habit detail; swipe/archive and reorder gating remain unchanged.

## Design direction

Use a compact expressive modal bottom sheet for measurable Entry editing: a clear header with habit identity and selected date, a prominent value treatment, target/unit context, a focused numeric field, and explicit Cancel/Save actions. Use the existing app spacing, dynamic color, expressive shapes, and motion scheme without introducing a new persistence model.

## Verification seam

Verify the observable homepage and editor on-device: boolean completion/edit actions, measurable value editing, Target/target-kind copy, selected historical date, cancellation, save/update behavior, compact card geometry, accessibility labels, and existing swipe/reorder behavior. No new Compose UI test infrastructure is required.
