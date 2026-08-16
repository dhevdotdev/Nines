Status: superseded

# Deepen the selected-date Habit timeline

The architecture review found that the homepage and detail ViewModels independently derive selected-date status, Schedule lookups, Score, Streak/Gap, and frequency context. This creates low locality around historical behavior and leaves the most important cross-screen rules without an automated contract test.

The approved direction is one date-bounded Habit timeline module. It will become the shared seam for selected-date derivation and historical Schedule semantics; screen-specific analytics remain a later candidate.

Superseded by: `.scratch/habit-timeline/spec.md` and its implementation tickets.
