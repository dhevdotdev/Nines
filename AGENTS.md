# Nines

Kotlin/Jetpack Compose Material 3 Expressive habit tracker. Single `:app` module, Koin DI, Navigation 3, Room persistence.

See `CONTEXT.md` for domain vocabulary and `docs/adr/` for recorded architectural decisions — read both before making changes that touch the data model, scoring, or gesture handling.

## Working conventions

- **Design quality is a functional requirement, not a finishing touch.** Build every screen/component at real visual polish as it's created — motion, transitions, dynamic color, haptics — rather than an MVP pass now and a "polish phase" later. If something has to give, it isn't the look and feel.
- **Consult [m3.material.io](https://m3.material.io) before any Material 3 Expressive decision** (color, motion, shape, component usage) rather than relying on general training knowledge of Material Design. It has repeatedly given concrete, correct mechanisms (e.g. why tonal palettes are derived via HCT, not raw sRGB) that guessing wouldn't have.
- **Reference apps for UX/pattern ideas only, never source.** Existing Material You apps (see the [curated app list](https://github.com/nyas1/Material-You-app-list)) inform features and screen ideas, not code — some are GPL-3.0, see `docs/adr/0005-reference-apps-no-code-copying.md`. Functionality and UX concepts aren't copyrightable; reimplementing from scratch is clean and keeps future licensing/monetization options open.

## Agent skills

### Issue tracker

Issues and specs live as local markdown files under `.scratch/<feature-slug>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`), recorded as a `Status:` line per issue file. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
