# 01 — Extract the homepage habit editor module

**Status:** done
**Blocked by:** None

Move `AddHabitSheet`, its frequency state, and its numeric formatting/stepper
helpers into a dedicated presentation file. Preserve the existing callback
interface and all Material 3 Expressive behavior.

- [x] Extract the editor module without changing behavior.
- [x] Keep palette and action callbacks explicit at the interface.
- [x] Run the JVM and connected test suites.
