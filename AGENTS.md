# Nines

Kotlin/Jetpack Compose Material 3 Expressive habit tracker. Single `:app` module, Koin DI, Navigation 3, Room persistence.

See `docs/architecture.md` for how the code is laid out, `GLOSSARY.md` for domain vocabulary and `docs/adr/` for recorded architectural decisions — read both before making changes that touch the data model, scoring, or gesture handling.

## Commands

- Setup: Android SDK (`ANDROID_HOME` or `sdk.dir` in `local.properties`) and a full JDK 25, not GraalVM (its `jlink` breaks the Android build). No environment variables or secrets are needed. Run `git config core.hooksPath .githooks` once to enable the pre-commit hook.
- Build: `./gradlew :app:assembleDebug`
- Unit tests: `./gradlew :app:testDebugUnitTest`
- Format: `./gradlew spotlessApply` (ktlint); static analysis: `./gradlew :app:detekt`; Android lint: `./gradlew :app:lintDebug`
- Coverage floor for `scoring/`: `./gradlew :app:jacocoDebugUnitTestCoverageVerification`
- Instrumented tests (device or emulator): `./gradlew :app:connectedDebugAndroidTest`
- Run and prove a change in the real app: use the `verify-nines` skill.

CI (`.github/workflows/ci.yml`) runs format, detekt, unit tests with the coverage floor, lint, the debug build and compiles the instrumented tests. New code must pass detekt without growing `app/detekt-baseline.xml`.

## Working conventions

- **Design quality is a functional requirement, not a finishing touch.** Build every screen/component at real visual polish as it's created — motion, transitions, dynamic color, haptics — rather than an MVP pass now and a "polish phase" later. If something has to give, it isn't the look and feel.
- **Consult [m3.material.io](https://m3.material.io) before any Material 3 Expressive decision** (color, motion, shape, component usage) rather than relying on general training knowledge of Material Design. It has repeatedly given concrete, correct mechanisms (e.g. why tonal palettes are derived via HCT, not raw sRGB) that guessing wouldn't have.
- **Reference apps for UX/pattern ideas only, never source.** Existing Material You apps (see the [curated app list](https://github.com/nyas1/Material-You-app-list)) inform features and screen ideas, not code — some are GPL-3.0, see `docs/adr/0005-reference-apps-no-code-copying.md`. Functionality and UX concepts aren't copyrightable; reimplementing from scratch is clean and keeps future licensing/monetization options open.

## Agent skills

### Issue tracker

Issues and specs live in GitHub Issues for `dhevdotdev/Nines`, via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`), applied as GitHub labels. Issues also carry a `priority:` and an `area:` label. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `GLOSSARY.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.

### Verification

Use the `verify-nines` skill (`.agents/skills/verify-nines/`) to run the app on an emulator and prove a change works.
