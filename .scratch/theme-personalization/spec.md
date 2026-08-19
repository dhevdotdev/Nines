# App color source and palette picker

**Status:** done

## Problem Statement

Nines adapts to the system wallpaper on Android versions that provide dynamic
color, but people cannot understand or control that choice. They cannot select
an app-owned palette when wallpaper colors do not fit their taste, and Settings
does not show a truthful preview of the active scheme.

## Solution

Give Settings an Appearance destination where a person chooses an app color
source. Wallpaper remains the default when the platform can provide it. Four
curated Nines palettes provide intentional alternatives. Each choice displays
a compact preview derived from the same complete Material color scheme that the
root app theme applies immediately and persists for future launches.

## User Stories

1. As a Nines user, I want Wallpaper to be the default color source, so the
   app feels personal to my Android system.
2. As a Nines user, I want to see that Wallpaper is active, so the app's
   changing colors are understandable rather than mysterious.
3. As a Nines user, I want to choose a curated Nines palette, so I can keep an
   app appearance I like even when my wallpaper changes.
4. As a Nines user, I want to preview every choice before selecting it, so I
   understand its primary, secondary, tertiary, and surface expression.
5. As a Nines user, I want the preview to match the real app, so the choice is
   trustworthy.
6. As a Nines user, I want a chosen source to survive relaunch, so I do not
   repeatedly configure appearance.
7. As a Nines user with a new wallpaper, I want Wallpaper mode to adapt on its
   own, so the setting remains genuinely system-personalized.
8. As a Nines user on an Android version without wallpaper dynamic color, I
   want a safe Nines fallback and curated choices, so Appearance remains
   useful without misleading controls.
9. As a Nines user in dark mode, I want the same source choice to resolve to a
   readable dark scheme, so appearance does not compromise accessibility.
10. As a Nines user with colorful Habit identities, I want those accents to
    remain harmonious after changing the app source, so habit recognition does
    not become visual noise.
11. As a Nines user, I want the choice to update every screen and system bar
    immediately, so the app remains one coherent surface.

## Implementation Decisions

- Introduce **app color source** as a persisted UI preference with these
  values: `Wallpaper`, `Nines`, `Forest`, `Ocean`, and `Sunset`.
- Wallpaper is the default. On Android 12+, it resolves through the system
  dynamic light/dark schemes. On earlier Android versions it resolves to the
  Nines curated scheme; the Appearance UI explains that wallpaper color is
  unavailable rather than pretending to extract it.
- Curated choices are fixed named source colors, each expanded into a complete
  light/dark HCT ColorScheme using the existing Material Kolor color utilities
  and the current expressive/2025 color configuration.
- Alongside Nines, Forest, Ocean, and Sunset, offer four recognizable
  classic-theme-derived choices: Catppuccin, Tokyo Night, Dracula, and Nord.
  Each uses a documented canonical accent as a seed and is labelled
  “inspired” in the UI; it is not a literal editor-theme port.
- System light/dark mode remains the only theme-mode choice in this feature.
- Resolve the selected source once at the root Nines theme. All screens,
  dialogs, onboarding, and system bars consume that one scheme.
- Appearance previews receive the exact resolved ColorScheme and display role
  pairings, not raw hexadecimal swatches. At minimum they expose primary/on
  primary, secondary/on secondary, tertiary/on tertiary, and a surface/
  container relationship.
- Add an Appearance destination from Settings. Selecting a source applies it
  immediately, supplies a clear selected state, and persists it.
- Existing per-Habit colors remain distinct from app color source. Their HCT
  harmonization continues to use the active app primary.
- Record the root color-source precedence in an ADR. No new business-domain
  term belongs in `CONTEXT.md`; this is a UI preference, not a Habit/Entry/
  Schedule concept.

## Testing Decisions

- Test `AppearancePreferencesStore` through its public Flow and mutation API,
  using an isolated real Preferences DataStore like onboarding persistence.
- Test source resolution through a public, deterministic policy seam with a
  known source, dark/light mode, and dynamic-color availability; assertions
  verify source/fallback behavior rather than internal Compose calls.
- Test Appearance through Compose semantics: Settings reaches the destination,
  source names and preview labels render, and selection exposes selected state.
- Run connected tests and an on-device smoke path that selects Wallpaper and a
  curated source, verifies immediate theme application, relaunch persistence,
  and readable dark/light behavior.

## Out of Scope

- Manual light/dark/system theme-mode selection.
- Arbitrary user-picked seed colors, image/content extraction, or custom
  palette editing.
- Changing per-Habit palette choices or persistence.
- Importing another theme project's code, raw color-role table, assets, or
  manual theme-mode variants.
- Recoloring individual screens outside the shared root theme.

## Further Notes

Material color roles must be consumed in their intended pairs. The palette
picker is an appearance-control feature, not an invitation to apply raw colors
directly to components.
