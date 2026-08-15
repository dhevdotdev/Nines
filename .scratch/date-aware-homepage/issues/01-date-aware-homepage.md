# Date-aware homepage with a scrollable top calendar

Status: ready-for-agent

The habit list currently only ever shows today's state per habit. Idea: add a scrollable calendar at the top of the home screen so the user can pick a past date, and the list shows that date's state instead of today's.

Tied to this: since the top calendar would take over date navigation, the small per-card history-dot strip may become redundant — the thinking is this could free up space and change where/how the streak count is shown on each habit card. Not settled: whether the per-card history strip actually goes away, exactly how streak display changes, and whether the streak number itself should reflect the selected past date rather than always being "streak as of today."

Also raised in the same breath, as something to address as part of this redesign rather than separately: numeric habit editing is currently too buried/fiddly (small tap targets, not discoverable) and should become more prominent — plausibly falls out naturally from reworking the card layout here.

Genuinely underspecified — logged as the idea, not a design. Work out the actual layout, what happens to the history strip, and the numeric-edit UX during triage/spec, not now.

## Comments

Fully grilled and specced — see `spec.md` in this same directory. All the "not settled" questions above are now decided there. Next step is `/to-tickets` against the spec, not further work on this file.
