# Reorder habits via long-press instead of a dedicated mode toggle

Status: needs-triage

Currently, reordering the habit list requires tapping a top-bar icon to enter a dedicated reorder mode (drag handles appear, swipe-to-edit/archive is disabled) — see `docs/adr/0003-custom-swipe-gesture.md`. The ask: reconsider this in favor of a more standard long-press-to-drag interaction directly on a row, without a separate mode.

Real constraint to solve, not just implement: a long-press-drag gesture living on the same row as swipe-to-edit/archive is exactly what caused three earlier redesigns of the swipe gesture (see the same ADR) — the mode toggle exists specifically because stacking both gesture detectors on one touch surface broke in genuinely hard-to-diagnose ways. Any long-press-based redesign needs to solve that conflict for real, not reintroduce it.

## Comments
