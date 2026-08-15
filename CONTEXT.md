# Nines

A habit tracker whose compliance model is deliberately framed as an SRE reliability metric: a habit's Score behaves like an uptime SLO, where a miss spends part of an error budget that decays over time rather than resetting instantly.

## Language

**Habit**:
A trackable behavior the user wants to build, either boolean (done/not-done each day) or numeric (a value logged against a target).
_Avoid_: Task, item.

**Entry**:
A single day's recorded state for a habit — `COMPLETED`, `IN_PROGRESS`, or `SKIPPED` — plus a numeric value for numeric habits.
_Avoid_: Record, log (as the noun for one day's data).

**Schedule** (`ScheduleVersion`):
The frequency/target rule in force for a habit as of a given date. Versioned by effective-from date, so a rule change never retroactively changes how past days were judged.
_Avoid_: Config, settings — as a synonym for the versioned rule itself.

**Target**:
What a habit's schedule requires to count as met: either a numerator/denominator frequency (boolean habits) or a `targetValue` + `targetKind` (`AT_LEAST` / `AT_MOST`, numeric habits).

**Score**:
A habit's current compliance level, computed as an exponentially-weighted moving average (EWMA) over completions — not a raw percentage and not a streak count. Always recomputed from entries; never stored as the source of truth.
_Avoid_: Percentage, rate — as the name for this value; those describe flat-window math, which Score deliberately isn't.

**Streak** / **Gap**:
A consecutive run of completed (streak) or missed (gap) days, computed on demand from entries, never persisted as a stored count.
_Avoid_: Persisting streak length as a field on the habit.

**Archive**:
Soft-removing a habit from the active list (`archivedAt` set on the entity) without touching its history. Restorable from Settings → Archived Habits.
_Avoid_: Delete — nothing is ever hard-deleted through the app's own delete/swipe-to-archive UI, even though the UI affordance (icon, action name) sometimes still says "delete."
