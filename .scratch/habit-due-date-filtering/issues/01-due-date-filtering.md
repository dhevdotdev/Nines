# Frequency-based "due today" filtering for habits

Status: needs-triage

Right now every non-archived, already-created habit shows on the list every single day, regardless of its target frequency — a "3×/week" habit shows daily exactly the same as a daily habit. There is no concept anywhere in the app of a habit being "not due" on a given date.

Surfaced while grilling the date-aware-homepage redesign (`.scratch/date-aware-homepage/`): a real "is this habit due" filter needs to answer a question the current schedule model was never designed to answer — for a frequency-target habit (numerator/denominator, e.g. 3/7), *which specific days* count is undefined. The EWMA scoring math (`docs/adr/0002-score-and-streaks-are-derived.md`) works fine on a ratio without ever needing to pick specific days, so this gap has never mattered until now.

Deliberately deferred out of the date-aware-homepage redesign rather than solved shallowly just for that ticket — see that redesign's grilling notes. When this gets picked up for real, it needs its own design pass on the which-days-count question before any filtering UI gets built on top of it.

## Comments
