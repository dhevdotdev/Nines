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

## Classic-theme sources

The classic-theme choices use a canonical accent as the HCT seed, then let
Nines generate accessible Material light and dark role sets. They are therefore
**inspired by**, not literal ports of, the editor themes: a literal port would
override Material role pairings and fight the system light/dark mode.

- Catppuccin Mocha's canonical mauve is `#cba6f7`. [Catppuccin
  palette](https://github.com/catppuccin/palette/blob/main/palette.json)
- Tokyo Night exposes `#7aa2f7` as its ANSI/chart blue. [Tokyo Night
  theme](https://github.com/enkia/tokyo-night-vscode-theme/blob/master/themes/tokyo-night-color-theme.json)
- Dracula's canonical purple is `#bd93f9`. [Dracula
  palette](https://github.com/dracula/dracula-theme)
- Nord's frost blue is `#88c0d0`. [Nord
  palette](https://github.com/nordtheme/nord/blob/develop/src/nord.css)

All four upstream repositories publish MIT licenses. Nines consumes no source
code, assets, or raw role values from them; it records attribution in this
research note and derives Material schemes from the listed accent seeds.
