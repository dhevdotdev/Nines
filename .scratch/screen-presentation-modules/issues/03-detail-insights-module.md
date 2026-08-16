# 03 — Extract the detail insights module

**Status:** done
**Blocked by:** None

Move the detail analytics/insights section and its row helpers into a dedicated
presentation module. The module consumes `HabitDetailUiState` and renders only;
it must not derive analytics or access persistence.

- [x] Extract insights presentation and formatting helpers.
- [x] Preserve all current rows, labels, and conditional visibility.
- [x] Run the JVM and connected test suites.
