Status: done

# Selected-date Habit timeline

## Problem Statement

Nines has a strong pure scoring seam, but the same Habit timeline concepts are rebuilt in multiple ViewModels. The homepage independently combines Entries, Schedule lookup, visibility, Score, Streak, and frequency progress. The detail screen independently rebuilds much of that history and derives additional date-based values. The repository's Score series also resolves one Schedule for the end date and applies it across the series, which risks violating the domain rule that a versioned Schedule judges only the dates on which it was in force.

This duplication gives selected-date behavior low locality. A change to historical Schedule semantics can require edits in several callers, and the homepage and detail screen can silently disagree. Device verification currently catches some of this, but there is no high-level contract test that exercises the shared behavior through one interface.

## Solution

Create one deep Habit timeline module. It accepts a Habit's date-bounded Entries, the versioned Schedules relevant to that range, and a Selected date, then returns the derived timeline view needed by callers. The module owns Schedule-as-of-date selection, Entry interpretation, selected-date status, Score, Streak/Gap, and frequency progress. The homepage and detail ViewModels adapt that result into their existing UiState types and retain ownership of screen-specific presentation and analytics.

The timeline must apply the Schedule in force on each date when deriving historical compliance. A Schedule change therefore affects only its effective date and later dates. Entries after the Selected date are excluded from every result for that selection. Existing active-habit visibility and selected-date editing behavior remain intact.

## User Stories

1. As a user browsing a Habit on a past Selected date, I want its selected-date Entry status to come from the same timeline rules as the homepage, so that history is trustworthy.
2. As a user, I want the homepage and detail screen to agree about the Schedule in force on a date, so that the same Habit does not have two historical meanings.
3. As a user, I want Entries after the Selected date excluded from status and compliance results, so that future information cannot leak into history.
4. As a user, I want Score to use the Schedule in force on each scored date, so that a later frequency change does not rewrite earlier compliance.
5. As a user, I want Streak and Gap to remain derived from explicit Entries, so that the shared timeline does not change their meaning.
6. As a user, I want frequency progress to use the Schedule effective on the Selected date, so that the homepage target remains truthful while browsing history.
7. As a user, I want skipped Entries to remain neutral in completion-derived measures, so that skipping is not silently treated as completion or failure.
8. As a user, I want incomplete boolean Entries not to count as completed days, so that compliance is not overstated.
9. As a user, I want numeric Entry values to retain their existing target-kind semantics, so that AT_LEAST and AT_MOST remain distinguishable after projection.
10. As a user, I want active-habit visibility to remain based on creation date and archive state, so that timeline deepening does not hide Habits accidentally.
11. As a user editing a past Selected date, I want the existing Entry actions to continue writing to that date, so that the shared read model does not break editing.
12. As a user returning to today, I want the timeline to restore today's state without stale historical values, so that date navigation remains reversible.
13. As a developer, I want the timeline's externally observable behavior tested through one interface, so that future refactors preserve domain meaning.
14. As a developer, I want screen ViewModels to adapt timeline results rather than repeat date math, so that changes have locality.
15. As a developer, I want the timeline to be independent of Compose, so that domain tests remain fast and stable.
16. As a developer, I want the existing Score, Streak, Gap, and frequency seams reused where their contracts remain correct, so that the new module gains depth without duplicating formulas.
17. As a developer, I want schedule-version behavior tested across an effective-date change, so that the most important historical invariant is regression-protected.
18. As a developer, I want the repository to provide the timeline's required date-bounded data without exposing Room details to screen ViewModels, so that persistence remains an adapter.
19. As a developer, I want screen-specific analytics to remain outside the shared timeline, so that the timeline does not become a grab bag for every detail statistic.
20. As a developer, I want the existing per-screen UiState and onAction architecture preserved, so that the new seam does not introduce a second state model.

## Implementation Decisions

- The Habit timeline is the shared domain module and test seam. Its interface represents a date-bounded Habit view, not a mock of the repository.
- The timeline receives the Habit identity/creation date, Entries, versioned Schedules, and Selected date as domain inputs. It does not know about Compose, Room, Android lifecycle, or screen copy.
- Schedule resolution is an as-of-date operation: for any date, use the newest Schedule whose effective date is not after that date.
- The timeline exposes enough derived state for homepage and detail adapters: selected-date Entry/status, selected Schedule, completed/skipped interpretation, Score, Streak/Gap, and frequency progress.
- Historical Score computation must use the Schedule in force for each date in the Score range, not one Schedule chosen only from the range end.
- Existing pure scoring, visibility, and frequency functions remain the formula seams where their contracts are sound; the timeline composes them behind one higher seam.
- The repository remains responsible for loading and persisting data. It provides date-bounded Entries and Schedule versions to the timeline without making ViewModels coordinate multiple Room queries.
- `HabitsViewModel` and `HabitDetailViewModel` remain screen adapters that map timeline results into their existing immutable UiState types and route actions through their existing sealed action interfaces.
- Detail-only analytics such as weekday rates, momentum, typical time of day, numeric aggregates, record messaging, and history presentation remain out of this spec and belong to the later analytics candidate.
- No new persistence table or schema migration is required.
- The timeline must preserve active-habit visibility and selected-date editing semantics.

## Testing Decisions

- The primary test surface is the Habit timeline interface. Tests assert returned behavior from known Habit timelines, not private loops or copied implementation formulas.
- Contract coverage includes: no Entries; incomplete and skipped Entries; selected-date changes; Entries after the Selected date; daily and non-daily Schedules; numeric AT_LEAST and AT_MOST targets; creation-date visibility; Streak/Gap; and a Schedule effective-date boundary.
- Historical Score tests must include a Schedule change inside the scored range, proving earlier dates use the older Schedule and later dates use the newer one.
- The repository adapter will be tested later through the real Room implementation and an in-memory NinesDatabase in candidate 2. Candidate 1 must not invent DAO mocks as a substitute.
- ViewModel tests should verify Flow wiring, Selected-date state changes, and action routing through the timeline adapter, not duplicate every domain case.
- Existing scoring, visibility, and frequency tests remain and are extended only where the timeline reveals a missing contract.
- On-device verification covers rendering, navigation, Entry editing, and return-to-today behavior; it is not the only test surface for domain rules.

## Out of Scope

- Screen-specific detail analytics and their visual redesign.
- Room contract-harness infrastructure beyond the smallest adapter changes needed to supply the timeline.
- New filters, categories, reminders, export/import, widgets, or backlog features.
- Replacing the existing Score formula, Streak/Gap definitions, visibility rules, or selected-date action model.
- Replacing the custom swipe/reorder gesture.
- Adding a generic repository-mocking interface solely for tests.
- Adding a second persistence model or caching derived timeline values.

## Further Notes

- This work is the first foundation candidate from the architecture review. Candidate 2 depends on the data-loading seam established here; candidate 3 can consume the timeline's date-bounded input without owning Schedule resolution again.
- The shared domain term is recorded as **Habit timeline** in `CONTEXT.md`.
