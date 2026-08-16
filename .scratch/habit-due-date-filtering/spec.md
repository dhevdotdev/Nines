Status: done

# Frequency-aware progress windows

## Problem Statement

The homepage treats every active habit as if it had the same daily obligation. A habit with a target such as three completions per week is visible every day, but the interface gives the user no useful way to understand how its frequency target is being met. This creates pressure to invent arbitrary due dates for a Schedule that specifies a frequency ratio, not particular weekdays.

The existing domain model already represents frequency targets as a numerator and denominator, and it deliberately computes Score as an EWMA rather than a flat percentage. The missing concept is a user-facing view of progress within the Schedule's frequency window. It must work for the Selected date, including historical dates, without changing the meaning of Score, Streak, or Gap.

## Solution

Represent frequency as progress over a rolling window anchored to the Selected date.

For a boolean habit, count explicit completed Entries in the inclusive window ending on the Selected date and compare that count with the Schedule numerator. For a numeric habit, aggregate explicit numeric Entry values in the same Schedule window and compare the aggregate with the Schedule target according to its target kind.

Keep every active habit visible on the homepage. Frequency does not make a habit disappear, become blocked, or acquire an arbitrary set of due weekdays. The homepage shows compact frequency progress so the user can understand the target without confusing it with the existing EWMA Score.

## User Stories

1. As a habit tracker user, I want a habit with a frequency target to show progress toward that target, so that I understand what the Schedule requires.
2. As a habit tracker user, I want a three-times-per-week habit to remain visible throughout the week, so that I can log it on the day I actually perform it.
3. As a habit tracker user, I want frequency progress to use a rolling window ending on the Selected date, so that the result does not depend on an arbitrary calendar-week boundary.
4. As a habit tracker user, I want the window length to come from the Schedule in force for the Selected date, so that displayed progress matches the historical configuration.
5. As a habit tracker user, I want explicit completed boolean Entries to count toward a boolean frequency target, so that progress reflects what I actually logged.
6. As a habit tracker user, I want incomplete boolean Entries not to count as completions, so that progress does not overstate performance.
7. As a habit tracker user, I want a skipped Entry to remain neutral, so that skipping does not appear as a completion or an additional failure.
8. As a habit tracker user, I want numeric Entries to be aggregated across the frequency window, so that a measurable habit can express a total amount per period.
9. As a habit tracker user, I want numeric AT_LEAST and AT_MOST targets to retain their existing meaning, so that frequency progress respects the direction of my target.
10. As a habit tracker user, I want the homepage to show all active habits regardless of frequency progress, so that I can access and log any habit on any past date.
11. As a habit tracker user, I want frequency progress to be visually distinct from Score, so that I do not mistake a rolling target ratio for the EWMA reliability metric.
12. As a habit tracker user, I want the progress display to work while browsing a past Selected date, so that history correction and review remain coherent.
13. As a habit tracker user, I want progress for a past Selected date to exclude Entries after that date, so that future information cannot leak into historical views.
14. As a habit tracker user, I want changing a Schedule to affect only dates on or after its effective date, so that historical progress is not silently recalculated under a newer target.
15. As a habit tracker user, I want a daily target to use a one-day window, so that existing daily habits do not gain surprising weekly behavior.
16. As a habit tracker user, I want an every-N-days target to use its configured N-day window, so that progress represents the Schedule I selected.
17. As a habit tracker user, I want a habit with no Entries in its window to show an honest empty or zero state, so that the card does not imply work was completed.
18. As a habit tracker user, I want a habit that exceeds a target to communicate completion without creating an invalid ratio, so that progress remains readable after overachievement.
19. As a habit tracker user, I want frequency progress to update immediately after logging or editing an Entry, so that the card reflects my action without navigation or reload friction.
20. As a habit tracker user, I want frequency progress to update when I select another date, so that the homepage remains a genuine view of the Selected date.
21. As a developer, I want frequency-window math in one pure domain seam, so that UI and persistence layers cannot drift into different interpretations.
22. As a developer, I want the pure seam to support boolean and numeric targets, so that the same date math is not implemented twice.
23. As a developer, I want the existing EWMA Score calculation unchanged, so that this feature does not undermine the app's SRE-inspired compliance model.
24. As a developer, I want Streak and Gap derivation unchanged, so that frequency progress does not introduce persisted or duplicated state.
25. As a developer, I want the feature compatible with the existing Selected-date homepage state, so that historical editing does not require a second date-selection model.

## Implementation Decisions

- Add one pure frequency-progress seam in the scoring/domain layer. It accepts the Selected date, the Schedule in force for that date, and the habit's Entries, and returns the progress result needed for rendering.
- Use an inclusive rolling window ending at the Selected date. Its length is the Schedule denominator; a daily Schedule therefore has a one-day window, while a weekly Schedule has a seven-day window.
- For boolean habits, the numerator is the number of explicit completed Entries in the window. Incomplete and skipped Entries do not increase the completed count. No automatic completion or coverage Entries are generated.
- For numeric habits, aggregate explicit numeric Entry values in the window and compare the result with the Schedule target value using the existing AT_LEAST or AT_MOST target kind.
- Keep the returned progress representation independent of Compose. It must be sufficient for the UI to render compact amount-versus-target context and a completed state without reimplementing the calculation.
- Resolve the Schedule as of the Selected date. A newer Schedule must not judge dates before its effective-from date.
- Add the progress result to the existing homepage row state for the Selected date. The ViewModel orchestrates this but does not contain a second copy of the window calculation.
- Keep all active, already-created habits visible using the existing visibility rules. Frequency progress is informational context, not a list-membership predicate.
- Keep Score, Streak, and Gap as separate derived concepts. This feature does not alter their formulas, persistence, or labels.
- Preserve existing Selected-date behavior: future dates remain unreachable, and logging/editing a past Selected date remains supported.
- Display non-daily frequency progress compactly in the habit card's existing middle/status area. A daily target is already represented by the selected-date status (`Done`/`Not logged` or the selected numeric value/target); do not repeat the same one-day target as a second line. Exact copy, iconography, and M3 Expressive motion are implementation decisions, but the treatment must not masquerade as the Score ring or Entry action.
- Do not copy implementation or source from uHabits/Loop. Its behavior is a UX and domain reference only, consistent with the reference-app ADR.

## Testing Decisions

- Test the externally observable result of the pure frequency-progress seam, not internal loops or data structures.
- Add plain JUnit tests in the existing scoring test area, matching the repository's dependency-free testing approach.
- Cover the pure seam's daily, weekly, and every-N-days windows; no Entries; over-target completion; a historical Selected date with later Entries excluded; and different targets supplied by historical/current Schedule versions. Entry-state filtering for incomplete and skipped Entries is an adapter concern covered by the ViewModel's explicit-entry mapping and on-device verification, not by a new ViewModel test harness.
- Cover numeric aggregation for both AT_LEAST and AT_MOST targets, including empty windows and values that meet or exceed the target.
- Verify a Schedule effective-date boundary through the existing repository query and on-device historical browsing; no duplicate persistence test seam is introduced.
- Verify homepage/ViewModel wiring manually on-device: active habits remain visible, progress changes after logging, Selected-date changes update progress, and Score/Entry controls retain their meanings.
- Do not introduce Compose UI or ViewModel test infrastructure for this feature. UI verification remains on-device, consistent with the existing project decision.

## Out of Scope

- Hiding or filtering habits based on frequency progress.
- Assigning exact weekdays to a numerator/denominator frequency such as three times per week.
- A separate exact-weekday Schedule type.
- Automatically generated covered, not-due, or equivalent Entries.
- Changing the EWMA Score formula or display scale.
- Changing Streak or Gap derivation.
- Adding skip functionality; existing skip semantics remain unchanged.
- Changing habit creation, archiving, reordering, reminders, or unrelated detail-screen history presentation.
- Copying code from uHabits/Loop or making Nines GPL-licensed.

## Further Notes

- This spec replaces the original due-today interpretation. The original question was valid, but the approved answer is that frequency should remain flexible and visible rather than being converted into arbitrary due dates.
- The feature is deliberately small in domain scope: one pure calculation seam plus orchestration and presentation. It should not introduce a second Schedule model or a new persistence table.
- The work was split into tickets 01 and 02, implemented, tested, reviewed, and verified on-device.
