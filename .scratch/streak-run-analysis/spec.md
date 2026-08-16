# Shared Habit run analysis

**Status:** done

## Problem Statement

Habit timeline and detail analytics both derive Streak and Gap runs from the same completed dates
and date range, but they call the underlying run functions independently. The duplicate derivation
is a correctness risk: a future change to one caller can make current streak, best streak, or
longest gap disagree without a compiler error.

## Solution

Deepen the existing scoring module with one pure `HabitRunAnalysis` projection. It computes the
streak runs, gap runs, current streak, and current gap for a bounded date range. The Habit timeline
owns the production projection and carries it to detail analytics; analytics remains independently
callable by falling back to the same projector when no upstream analysis is supplied.

## User Stories

1. As a habit user, I want the homepage streak and detail streak to describe the same Entries, so
   that navigating between screens never changes the meaning of my progress.
2. As a habit user, I want the detail's best streak and longest Gap to use the same date range as
   the selected-date timeline, so that its insights are trustworthy.
3. As a habit user, I want retroactive Entry changes to update all streak and Gap displays from one
   derivation, so that stale or contradictory metrics cannot appear.
4. As a developer, I want one authoritative pure run-analysis module, so that streak/gap semantics
   have locality and future fixes apply to every caller.
5. As a developer, I want the timeline-to-analytics seam to be explicit, so that tests can assert
   the two adapters consume the same run analysis rather than merely duplicating fixtures.
6. As a developer, I want standalone analytics calls to remain valid, so that the deepened module
   does not force every caller to construct a timeline first.

## Implementation Decisions

- The new module remains domain-only and pure, using completed dates plus an inclusive start/end
  date. It returns both run lists and their current-length projections.
- Existing `computeStreakRuns`, `computeGapRuns`, and current-length semantics remain the source
  behavior; the new projection composes them rather than changing their meaning.
- `HabitTimeline` carries the computed `HabitRunAnalysis` alongside its existing scalar fields.
- `HabitAnalyticsInput` requires a `HabitRunAnalysis`; its default derives that analysis from the
  same bounded Entries for standalone callers. Production detail wiring supplies the timeline's
  analysis, eliminating duplicate work and giving the two adapters one shared result. The input
  validates that the supplied analysis covers the same date range.
- No score caching, persistence changes, UI redesign, generic read-model framework, or clock work
  is included. ADR-0002 remains unchanged: Score, Streak, and Gap are always derived.

## Testing Decisions

- Plain JUnit tests cover the run-analysis interface: contiguous and broken completions, empty
  ranges, current-date and day-before semantics, and exact input range clipping.
- Timeline tests prove its scalar current streak/gap values and exposed analysis agree.
- Analytics tests prove upstream analysis is honored and standalone fallback preserves existing
  behavior.
- Existing ViewModel contracts and connected device smoke remain regression verification; no new
  UI mechanism is needed for this pure scoring refactor.
- The run-analysis tests cover bounded input clipping, empty completion ranges, current-date and
  day-before semantics, and timeline-to-analytics reuse.

## Out of Scope

- Splitting the two monolithic Compose screens.
- Narrowing or deleting `HabitObservation`.
- Reducing the homepage's architectural fan-out.
- Optimizing EWMA score-series recomputation without measured jank.
- Refreshing captured real-today state across midnight.

## Further Notes

The deletion test says the run-analysis module earns its seam: deleting it would push identical
streak/gap semantics back into multiple production adapters. The interface is the test surface,
and the existing pure scoring tests are the highest useful seam.

## Completion evidence

- `./gradlew test` passed.
- `./gradlew :app:connectedDebugAndroidTest` passed all 10 tests on the USB-powered awake device.
- Final two-axis review found and corrected the optional/mismatched run-analysis interface and
  duplicated timeline scalar state. No ADR violation remains.
