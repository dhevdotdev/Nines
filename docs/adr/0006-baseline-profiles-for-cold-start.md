# Baseline Profiles fix release-build cold-start jank; debug builds are unaffected by design

A separate `:benchmark` Gradle module (`com.android.test` plugin, `androidx.benchmark:benchmark-macro-junit4`) generates a Baseline Profile committed at `app/src/release/generated/baselineProfiles/baseline-prof.txt`. It exists because the first-ever navigation to any screen showed 700ms+ single-frame stalls from ART JIT cold start (verified via `dumpsys gfxinfo`/Choreographer "Skipped N frames" logs, not guessed), independent of data size — proven by controlled A/B testing using different habits as the first navigation.

**Why it's a whole separate module**: demo-data seeding (used to drive the benchmark's UI through list → detail → back) is debug-only and unavailable on the `nonMinifiedRelease`/`benchmarkRelease` build variants the profile generator runs against, so the generator test creates its own throwaway habit instead of reusing the app's normal debug-seeding path.

**Consequences — read before reporting "still janky"**: Baseline Profiles only benefit `release`-type builds (and pre-compiled/installed-from-store builds in general). The `debug` variant used for day-to-day on-device testing sees zero benefit from this profile — that's expected, not a regression, and isn't a signal the fix didn't work.
