Status: ready-for-agent

# Date-aware homepage

## Problem Statement

The homepage (habit list) only ever shows today's state. If a habit was accidentally left unchecked yesterday, or a numeric value was logged wrong two days ago, there's no way to fix it from the homepage — the user has to navigate into the habit's detail screen, find the right day in its history, and edit it there. For a quick correction, that's a lot of friction for something that should be nearly as fast as logging today's entry.

Separately, editing a numeric habit's value today is itself fiddly — the only way in is tapping a small piece of value text on the row, which is easy to miss.

## Solution

Add a horizontally scrollable day rail to the top of the homepage, with an affordance to open a full calendar for dates further back. Selecting a date — anything from the past, or today — turns the whole homepage into a read/write view of that date: every row shows and can edit that date's entry directly, no detour through the detail screen. Future dates are never reachable. Removing the per-row history-dot strip (redundant once the rail/calendar is the shared date-navigation control) frees up row space, which goes toward making numeric editing genuinely more discoverable instead of just tightening the row.

## User Stories

1. As a user correcting a mistake, I want to select a past date from the homepage, so that I can fix an entry without navigating to the habit detail screen.
2. As a user browsing my recent history, I want a horizontally scrollable day rail at the top of the homepage, so that I can quickly jump to any of the last several days.
3. As a user wanting to correct something further back, I want an affordance on the day rail that opens a full calendar, so that I can reach any past date, not just the recent ones.
4. As a user, I want the rail/calendar to only ever offer past dates and today, so that I never accidentally create an entry for a date that hasn't happened yet.
5. As a user viewing a past date, I want each habit row to show that date's actual completion/value state, so that the homepage genuinely represents the day I've selected, not always today.
6. As a user viewing a past date, I want to toggle or edit a habit's entry for that date directly on the homepage, so that correcting history feels as fast as logging today.
7. As a user, I want editing a past date's entry to behave exactly like editing today's — no extra confirmation step — so that quick corrections stay quick.
8. As a user browsing a date before a habit was created, I want that habit to not appear in the list, so that the homepage never shows an impossible entry.
9. As a user, I want a clear but unobtrusive signal on the homepage whenever I'm not looking at today, so that I never mistake a past-date correction for logging today's entry.
10. As a user, I want each row's streak count to reflect the streak as it stood on the selected date, so that the number I see matches the date I'm actually looking at.
11. As a user, I want each row's score ring to reflect the score as of the selected date, so that the ring's fill genuinely matches what I'm viewing, not a stale "today" value.
12. As a user, I want swipe-to-archive and swipe-to-edit to keep working no matter which date I've selected, so that habit-level actions aren't artificially gated behind being on today.
13. As a user, I want the reorder-mode toggle to be unavailable while I'm viewing a past date, so that I'm not offered an action that has nothing to do with the date I'm looking at.
14. As a user, I want the reorder toggle to become available again as soon as I return to today, so that reordering isn't permanently harder to reach after browsing history.
15. As a user who finds numeric habit editing fiddly today, I want a more prominent, discoverable way to edit a numeric habit's value, so that I don't have to hunt for a small tap target.
16. As a user, I want the vertical space freed by removing the per-row history-dot strip to go toward that more prominent edit affordance, so the row isn't just slightly tighter with everything else unchanged.
17. As a developer maintaining this feature, I want the date-membership and as-of-date score/streak logic to live in pure, dependency-free functions, so they're testable the same way the rest of the scoring logic already is.
18. As a developer extending this feature later, I want the date-selection plumbing to not hard-code assumptions that would block adding frequency-based due-date filtering afterward, so that deferred backlog item stays buildable on top of this without a rewrite.

## Implementation Decisions

- **`HabitsUiState` gains a `selectedDate`** (defaulting to today). Every read/write path that currently hard-codes "today" (`completedToday`, `currentValue`, and the `ToggleHabit`/`IncrementHabit`/`SetNumericValue` action handlers) is re-anchored to this selected date instead. Field names that currently imply "today" specifically (e.g. `completedToday`) should be renamed to reflect that they now represent an arbitrary selected date, not always today.
- **`HabitsAction` gains a `SelectDate` variant** carrying the new date. Selecting a date beyond today is not a reachable action from the UI at all — the rail/calendar never offers a future date, so no validation/rejection path is needed for it.
- **Per-row history strip is removed from the homepage only.** `HabitListItem.recentDays` (and the `HistoryStrip` composable's use in `HabitRow.kt`) goes away. The shared `DayCell` type stays — it's still used by the habit detail screen's own history view, which is untouched by this spec.
- **List membership for a given date is a pure function, not a DAO query.** Given the already-loaded active-habit list (still just `archivedAt IS NULL`, unchanged), a new pure function decides whether a habit is visible on a given date by comparing that date against the habit's `createdAt`. This keeps the logic colocated with, and tested the same way as, the rest of the domain's pure functions rather than adding a new parameterized Room query for what's a small, in-memory list.
- **Score/streak as-of-date reuses existing pure functions, unchanged.** `HabitRepository.scoreSeries` already accepts an arbitrary `endDate`; the streak/gap pure functions already take a date range. The only change at the call site is passing `selectedDate` instead of always today. No new repository method.
- **Reorder mode is gated on `selectedDate`.** The reorder-mode toggle becomes unavailable whenever `selectedDate != today`, and if a past date gets selected while already in reorder mode, reorder mode exits. This is an extension of the existing gesture-mode design (mutually-exclusive reorder mode vs. swipe-to-edit/archive) — see the ADR on the custom swipe gesture — not a change to how reorder mode itself works.
- **Swipe-to-edit/archive is unaffected by `selectedDate`** — both stay available regardless of which date is selected, since they're habit-level actions.
- **Non-today indicator**: some clear-but-unobtrusive UI signal (exact treatment left to implementation, informed by Material 3 Expressive guidance) shows whenever `selectedDate != today`.
- **Numeric-edit affordance becomes more prominent** using the space freed by the strip's removal — exact layout left to implementation, but it must be a materially larger/more discoverable target than the current small value-text tap zone, not a cosmetic tweak.
- **Frequency-based due-date filtering is explicitly not built.** The membership/visibility function this spec introduces must not encode any assumption that would make adding that filtering later require reworking this feature's plumbing — see Out of Scope.

## Testing Decisions

- Only pure, dependency-free logic gets automated tests — matching the one existing test file's approach (`scoring` package, plain JUnit, given-inputs/assert-outputs, no mocking). Test external behavior (given a set of habits with various `createdAt`/`archivedAt` values and a target date, which habits are visible) rather than internal structure.
- New tests: the date-based visibility/membership function.
- Extended coverage, if not already present: an explicit case exercising streak/score computation with a non-"today" end date, to lock in that this feature's as-of-date behavior is real and covered, not incidental.
- `HabitsViewModel`'s state/action wiring and all UI (day rail, calendar dialog, row layout, mode-gating) are explicitly not covered by automated tests. No Compose UI test or ViewModel test infrastructure exists in this repo, and introducing it just for this feature was explicitly ruled out as disproportionate cost. Verification for that layer is manual, on-device, screenshot-confirmed — consistent with how every other feature in this project has been verified.

## Out of Scope

- Frequency-based "due today" filtering (which specific days count for a habit with a frequency target). Separately tracked as its own backlog item. This spec's implementation must not preclude it later, but does not build it now.
- Any change to the habit detail screen's own history/calendar view — it stays exactly as it is.
- Exact visual design of the day rail, the full-calendar affordance, and the reworked numeric-edit control — these are Material 3 Expressive design decisions made during implementation, not pre-specified here.
- Any change to how habits are created, archived, or reordered themselves — only *when* those affordances are available changes (reorder), or nothing changes at all (archive/edit).
- Everything else currently sitting in the backlog (widget, export/import, categories, filters, reminders, in-app guidance) — unrelated to this spec.

## Further Notes

- This spec extends, and should stay consistent with, several existing architectural decisions: archiving is reversible and habit-level (so swipe-to-archive staying available regardless of date is correct, not an oversight); score and streaks are always derived from raw entries and never persisted as truth (so as-of-date computation is a natural use of existing machinery, not a new storage decision); the reorder-mode toggle exists specifically to keep two gesture systems from fighting on the same touch surface (so gating it on `selectedDate` is extending that same reasoning, not introducing a new one); and every screen in this app follows the same single-`UiState`-plus-sealed-`Action`-via-`onAction()` shape, which this spec's `HabitsUiState`/`HabitsAction` additions should match exactly.
- Came out of a full `/grilling` session (10 rounds of settled decisions) plus one follow-up round narrowing the due-date-filtering deferral. The deferred item from that session is tracked separately so it isn't lost, and this spec was written to not block it.
