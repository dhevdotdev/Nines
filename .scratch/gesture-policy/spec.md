# Tested habit gesture policy

**Status:** done

## Solution

Add a pure gesture-policy module that maps mode, selected-date eligibility, and released swipe direction to an action. Compose remains responsible for pointer tracking, anchored settling, haptics, and animation.

## Invariants

- Reorder mode is enterable only when the selected date is today.
- Start-to-end means edit; end-to-start means archive.
- No direction and any swipe while reorder mode is active produce no action.
- This preserves the existing custom anchored-drag behavior and ADR-0003's release-gated action semantics.
