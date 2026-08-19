# Theme personalization research

## Findings

- Material 3 dynamic color supports a user-generated source from wallpaper and
  a content-based source. Nines is a habit tracker rather than a media/content
  surface, so this feature should use wallpaper personalization plus app-owned
  curated alternatives, not per-screen content extraction. [Material 3 dynamic
  color](https://m3.material.io/styles/color/dynamic-color/overview)
- Compose exposes dynamic light and dark schemes on Android 12 and newer. Apps
  should use a custom light/dark scheme as the fallback on older Android
  versions. [Android: Material 3 in
  Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- A generated ColorScheme is a complete role system, not a primary-color
  shortcut. Primary, secondary, tertiary, surface, container, and their
  paired `on*` roles must remain paired to retain contrast as dynamic color and
  user contrast settings vary. [Material 3 color
  roles](https://m3.material.io/styles/color/roles)

## Implication for Nines

Wallpaper and curated choices must both resolve to full HCT-derived Material
ColorSchemes at the root theme. Palette previews must render the real schemes,
not decorative hand-picked swatches. Habit identity colors remain independent
seeds, harmonized to the active app primary as they are today.
