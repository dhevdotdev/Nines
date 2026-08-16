# 04 — Add coverage diagnostics

**Blocked by:** 01

**Status:** done

- [x] Add a reportable JVM coverage task for the app module.
- [x] Document source/test exclusions and baseline interpretation.
- [x] Publish a useful report without imposing an arbitrary threshold.
- [x] Use the report to identify the next highest-value missing tests.

## Comments

- Added `./gradlew :app:jacocoDebugUnitTestReport`. The HTML report is generated at `app/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html`; XML is generated alongside it for CI/report consumers. The current JVM baseline is recorded in `coverage-baseline.md`; these are diagnostic numbers, not a gate.
- The report covers production JVM classes exercised by `testDebugUnitTest`. Generated Room/KSP/build classes are excluded, along with Android `R`/manifest/build-config classes. The Kotlin class path currently follows AGP 9.3.1's `intermediates/built_in_kotlinc` layout and should be revisited when AGP changes. Compose/device behavior, Android framework execution, and real Room instrumentation are intentionally outside this report and covered by connected tests. The next highest-value gaps are detail analytics and gesture-policy contracts, followed by broader repository/ViewModel cases.
