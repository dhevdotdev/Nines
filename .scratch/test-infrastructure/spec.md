# Layered test infrastructure and coverage diagnostics

**Status:** ready-for-agent

## Problem

Nines currently has fast JVM tests for a few pure scoring functions, but important boundaries are untested: coroutine Flow/ViewModel wiring, real Room persistence contracts, Compose semantics, and repeatable device smoke. Coverage reporting is also absent, making gaps hard to see.

## Solution

Add a small, maintainable test stack in layers: coroutine/ViewModel contract tests over a deterministic repository seam; real Room in-memory contract tests; focused Compose semantics tests for stable user-facing behavior; a repeatable device smoke checklist; and coverage reporting as a diagnostic, not a vanity threshold.

The layers must preserve the existing domain seams and avoid broad UI snapshot tests, repository mocks that duplicate Room, or a brittle percentage gate before the suite has useful signal.

## Decisions

- JVM tests use `kotlinx-coroutines-test` and test observable StateFlow/action behavior through interfaces with deterministic fakes only where the seam is already defined.
- Persistence contracts use the real Room DAOs and an in-memory `NinesDatabase`; no mock DAO substitutes for Room behavior.
- Compose tests target stable semantics and critical interaction contracts, not pixel snapshots.
- Device smoke remains a small documented checklist using the connected test device and UIAutomator bounds when precise taps are needed.
- Coverage is reported for the app's JVM tests first; thresholds are deferred until the baseline and exclusions are understood.
- Test infrastructure is implementation support, not a reason to rewrite production architecture or add speculative abstractions.

## Out of scope

- Full end-to-end automation of every screen.
- Pixel-perfect screenshot goldens.
- A mandatory arbitrary coverage percentage.
- Replacing the existing Room schema or production repository.
