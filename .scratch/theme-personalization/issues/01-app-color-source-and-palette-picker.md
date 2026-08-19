# 01 — App color source and palette picker

**Status:** needs-triage

**Reported problem:** Nines already follows the wallpaper-derived Android dynamic
color scheme when the platform provides one, but the user cannot see which
palette is active, opt out of wallpaper colors, or choose a curated Nines
palette.

## Current state

- `NinesTheme` defaults to Android's wallpaper-derived light/dark color scheme
  on Android 12 and newer.
- Older Android versions and dynamic-color-disabled previews/tests fall back to
  an HCT-derived Material Kolor scheme using the Nines seed, Expressive palette
  style, and the 2025 color spec.
- App surfaces predominantly consume semantic `MaterialTheme.colorScheme`
  roles rather than fixed presentation colors, so switching the app scheme is
  structurally viable.
- The fixed `HabitPalette` is a separate per-Habit identity choice. Each Habit
  seed is harmonized toward the active app primary and passed through HCT tone
  generation; it should not be confused with the app-wide theme source.
- Settings currently has no appearance or palette controls, and the selected
  app-wide source is not persisted because there is no user choice yet.

## Desired direction to grill/spec later

- Add an app-wide color-source choice with at least **Wallpaper** and a small
  set of curated Nines palettes.
- Show a truthful palette preview for the wallpaper-derived scheme and every
  curated option, including enough primary/secondary/tertiary and container
  roles to communicate the result in the current light or dark mode.
- Generate curated schemes through Material's color utilities/HCT tone curves;
  do not hand-pick isolated UI colors or bypass semantic color roles.
- Persist the chosen source and apply it at the root theme so onboarding,
  homepage, detail, dialogs, and Settings all update together.
- Define fallback behavior for Android versions without wallpaper dynamic
  color and for a wallpaper source that changes after selection.
- Verify contrast, light/dark behavior, system bars, Habit color harmonization,
  and live theme transitions before considering the feature complete.

## Product questions

- Whether theme mode itself remains system-only or gains light/dark/system
  controls alongside color source.
- Which curated palettes belong in the initial set and whether users may choose
  an arbitrary seed later.
- Whether previews are compact swatches, miniature semantic surfaces, or both.

## Reference

Material 3 distinguishes wallpaper-based user-generated dynamic color from
content-based color. This issue concerns user-generated wallpaper color plus
curated app-owned alternatives, not content-based per-screen theming:
https://m3.material.io/styles/color/dynamic-color/overview
