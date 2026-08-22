# Nines

A habit tracker whose compliance model is deliberately framed as an SRE reliability metric: a habit's Score behaves like an uptime SLO, where a miss spends part of an error budget that decays over time rather than resetting instantly.

## Language

**Habit**:
A trackable behavior the user wants to build, either boolean (done/not-done each day) or numeric (a value logged against a target).
_Avoid_: Task, item.

**Entry**:
A single day's recorded state for a habit — `COMPLETED`, `IN_PROGRESS`, or `SKIPPED` — plus a numeric value for numeric habits.
_Avoid_: Record, log (as the noun for one day's data).

**Selected date**:
The calendar date whose Entries, Score, Streak, and homepage status are currently being viewed or edited. It defaults to today, but may be any past date.
_Avoid_: Current day — the selected date may be historical.

**Habit timeline**:
A date-bounded view of a Habit's Entries and the Schedule in force across those dates, used to derive selected-date status and historical compliance measures consistently across screens.
_Avoid_: History — that describes the presentation of past Entries, not the date-aware domain view used for derivation.

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

An archived Habit has no active Reminder. Restoring it resumes its configured Reminder.

**Reminder**:
An optional, per-Habit prompt at a user-chosen local time to take the next needed Habit action. A boolean or `AT_LEAST` numeric Habit resolves its Reminder by meeting its Target; an `AT_MOST` numeric Habit resolves it by recording its value, after which Nines derives whether the Target was met. An unresolved Reminder remains associated with its original date until resolved, including after device restart. A Reminder is not an Entry and does not itself affect Score, Streak, or Gap.
_Avoid_: Treating a notification dismissal as completion or a miss.

**Snooze**:
Temporarily deferring a Reminder without changing the Habit’s Entry. In contrast, “Not today” is an explicit `SKIPPED` Entry and is therefore a real tracking decision.

**App color source**:
The user-facing origin of Nines’ root appearance: Wallpaper, a named Nines or theme-inspired palette, or Custom. It determines the app-wide Material color scheme, not a Habit’s identity color.

**Theme palette recipe**:
A coordinated visual identity used to derive an app color source’s Material roles. A recipe may express several related accent characteristics while preserving accessible role pairings; it is not a literal export of another theme’s implementation or role table.

**Material color role**:
A semantic color position in the app-wide scheme, such as primary, secondary, tertiary, surface, container, or an `on*` paired foreground. Roles describe intended use and contrast relationships, not decorative swatches.
