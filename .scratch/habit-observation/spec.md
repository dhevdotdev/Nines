# Shared Habit observation

**Status:** done

## Problem Statement

The homepage and detail screen both consume the same Habit history, but each adapter still reconstructs parts of the observation independently. The homepage builds a selected-date timeline, while the detail adapter separately builds a selected-date timeline, a roughly nine-month history list, and date-sensitive presentation decisions. Some detail presentation helpers still call `LocalDate.now()` directly, which can disagree with the selected date or cross midnight differently from the ViewModel.

## Solution

Create one domain-only Habit observation module. Given a Habit creation date, Entries, versioned Schedules, a selected date, and an explicit as-of date, it returns the date-bounded Habit timeline plus a bounded calendar-day observation that both screen adapters can map into their existing UI state. The Room repository supplies one domain observation stream; the homepage and detail ViewModels become thin adapters over that stream. Detail analytics remains its own module and consumes the observation's bounded inputs. UI copy, colors, Compose state, and history presentation remain in screen adapters.

## User Stories

1. As a habit user, I want the homepage and detail screen to agree about a selected date, so that the same Entry status is never shown differently in two places.
2. As a habit user, I want browsing a past date to use only Entries and Schedules available on or before that date, so that future activity cannot leak into historical views.
3. As a habit user, I want a Habit created later than the selected date to be absent from the observation, so that pre-creation days are not presented as missed history.
4. As a habit user, I want the detail calendar to show the same bounded date range regardless of which screen opened it, so that history has one predictable meaning.
5. As a habit user, I want returning to today to restore today's observation everywhere, so that historical browsing does not leave stale values behind.
6. As a habit user, I want schedule changes to affect only dates on or after their effective date, so that historical compliance remains stable.
7. As a habit user, I want selected-date labels and “today” indicators to use the same date context as the data, so that the UI does not contradict itself around midnight or while browsing history.
8. As a developer, I want one observation seam for homepage and detail projection, so that date-boundary fixes have locality.
9. As a developer, I want Room mapping and UI mapping to remain adapters around domain data, so that persistence and Compose concerns do not leak into the observation module.
10. As a developer, I want the observation interface to be testable with plain data, so that historical edge cases are fast to verify without a device.
11. As a developer, I want existing score, streak, frequency-progress, and analytics semantics preserved, so that this refactor improves locality without changing domain meaning.
12. As a developer, I want a connected smoke path proving homepage/detail agreement, so that a green JVM suite is not mistaken for visual or navigation correctness.

## Implementation Decisions

- The observation module is domain-only and owns date clipping, Habit visibility, selected-date projection, schedule-as-of-date selection, and bounded calendar-day data.
- Its input uses domain types, not Room entities, Compose state, or `LocalDate.now()` calls hidden inside the implementation. Callers provide selected/as-of dates explicitly.
- The existing Habit timeline remains the source of score, streak, gap, and frequency-progress semantics; the observation module composes it rather than duplicating it.
- The bounded calendar-day output is a domain record containing date, Entry state, and numeric value. UI adapters convert it to `DayCell` or homepage presentation types. Its end is the effective selected date, capped by the explicit as-of date; future selected dates are projected at as-of.
- The repository exposes one observation snapshot stream built from the Habit, Entry, and Schedule DAO streams. Both ViewModels consume that snapshot rather than independently combining raw streams; presentation metadata remains in the repository adapter snapshot.
- Detail analytics remains separate and consumes the observation's bounded Entries/as-of context.
- Screen-specific copy, “today” visual treatment, dialogs, and navigation remain in the UI adapters.
- Existing archive, reorder, swipe, Room schema, and persistence semantics do not change.

## Testing Decisions

- Plain-JUnit tests exercise the observation module through its domain interface: pre-creation visibility, future-entry exclusion, selected-date changes, schedule boundaries, bounded history range, and return-to-today inputs.
- Repository instrumentation verifies the real Room observation stream maps Entries and Schedules correctly and remains ordered.
- Existing coroutine/ViewModel contract tests are extended to prove both adapters receive the same selected-date observation.
- Existing Compose semantics and connected smoke tests remain the client-level verification for date selection, detail navigation, and historical agreement.
- Tests assert observable outputs and domain invariants, not private helper structure.

## Completion evidence

- Domain and coroutine verification: `./gradlew test` passed.
- Room and Compose/device verification: `./gradlew :app:connectedDebugAndroidTest` passed all 9 tests on `CPH2691 - 16`; the device was USB-plugged, awake, and stay-on enabled.
- Review follow-up is recorded in commit `3945f7e`: future-date projection, duplicated metadata observation, and Room stream coverage were corrected. The remaining “today” source is intentionally captured once per ViewModel/UI session; a live injected clock is a separate future seam, not part of this projection refactor.

## Out of Scope

- Rewriting the Room schema or adding import/export/sync adapters.
- Moving detail analytics into the observation module.
- Redesigning the detail calendar or day rail visuals.
- Creating a generic read-model framework for unrelated screens.
- Replacing the existing custom gesture implementation.

## Further Notes

The observation seam is justified because homepage and detail are already two production callers with separate test adapters. A generic repository interface without another real adapter would fail the deletion test and is intentionally not part of this work.
