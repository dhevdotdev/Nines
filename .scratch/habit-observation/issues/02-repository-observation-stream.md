# 02 — Expose one Room-backed observation stream

**What to build:** Make the repository expose one domain observation stream that the homepage and detail adapters can consume without independently combining raw Habit, Entry, and Schedule streams.

**Blocked by:** 01 — Build the domain Habit observation

**Status:** ready-for-agent

- [ ] Map the real Room streams into the domain observation input.
- [ ] Route both homepage and detail ViewModels through the shared observation stream.
- [ ] Preserve selected-date actions, historical scores, analytics inputs, and existing UI state shapes.
- [ ] Extend coroutine contract tests and Room instrumentation to prove both callers receive the same observation semantics.

