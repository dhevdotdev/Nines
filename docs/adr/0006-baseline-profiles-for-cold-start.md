# Baseline Profiles fix release-build cold-start jank; debug builds are unaffected by design

A separate `:benchmark` Gradle module (`com.android.test` plugin, `androidx.benchmark:benchmark-macro-junit4`) generates a Baseline Profile committed at `app/src/release/generated/baselineProfiles/baseline-prof.txt`. It exists because the first-ever navigation to any screen showed 700ms+ single-frame stalls from ART JIT cold start (verified via `dumpsys gfxinfo`/Choreographer "Skipped N frames" logs, not guessed), independent of data size — proven by controlled A/B testing using different habits as the first navigation.

**Why it's a whole separate module**: demo-data seeding (used to drive the benchmark's UI through list → detail → back) is debug-only and unavailable on the `nonMinifiedRelease`/`benchmarkRelease` build variants the profile generator runs against, so the generator test creates its own throwaway habit instead of reusing the app's normal debug-seeding path.

**Consequences — read before reporting "still janky"**: Baseline Profiles only benefit `release`-type builds (and pre-compiled/installed-from-store builds in general). The `debug` variant used for day-to-day on-device testing sees zero benefit from this profile — that's expected, not a regression, and isn't a signal the fix didn't work.

## Operational verification

This distinction is easy to miss because the debug APK is the normal handoff
build. Installing `app-debug.apk` and seeing a janky first navigation is not a
valid regression check for this ADR; it is the expected unprofiled behavior.

When investigating cold-start performance, record the installed variant first.
For a meaningful Baseline Profile check, build and install a release-type APK
that contains the committed profile, then compare the first navigation against
the same controlled flow without changing the data set. Use
`adb shell dumpsys gfxinfo` and Choreographer skipped-frame logs for evidence.
Do not use a different habit or a second navigation as the only comparison:
the original diagnosis established that the important variable is first-ever
screen composition in the process, not habit history or data size.

The normal debug-device workflow remains useful for UI verification, but any
debug-only first-open jank should be attributed to the missing profile before
opening a new performance issue.
