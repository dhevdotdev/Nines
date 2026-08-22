# Themed palettes feel monochromatic in the live app

Category: enhancement
Status: needs-triage

## Reported problem

After selecting one of the named palettes in Appearance, the app feels like it
has been tinted with one color rather than given a complete palette. The
screens lack a convincing splash of coordinated colors, so the result feels
lifeless despite the theme being described as a Material/HCT-derived scheme.

## Desired direction

Each named theme should produce a visually intentional, harmonious palette:
primary, secondary, tertiary, container, surface, and supporting roles should
have enough tonal and chromatic distinction to create hierarchy while still
reading as one theme. The result must remain accessible and use proper Material
role pairings rather than raw decorative colors.

## Scope to clarify during triage

- Verify whether the issue is in palette generation, role consumption by the
  app surfaces, or both.
- Compare the resolved `ColorScheme` roles and the rendered app for several
  named themes, including Catppuccin and Tokyo Night.
- Decide whether the fix belongs in the shared HCT palette configuration, the
  app's Material role usage, or both.

## Constraints

- Preserve Wallpaper dynamic color and Custom seed behavior unless verification
  shows they share the same defect.
- Preserve readable light/dark contrast and Material paired roles.
- Do not replace the shared root theme with per-screen hardcoded colors.
- Keep per-Habit identity colors distinct from the app palette.
