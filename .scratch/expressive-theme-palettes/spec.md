# Expressive, coordinated theme palettes

**Status:** done

## Problem Statement

Named Appearance sources currently feel like single-color tints in the live
app. Although Nines derives a complete Material `ColorScheme` from each seed,
the visible result does not communicate the coordinated identity of themes
such as Catppuccin or Tokyo Night. Supporting roles are either too close to
the dominant hue or are not used in enough expressive surfaces, leaving the
interface visually flat and lifeless.

## Solution

Give named themes richer palette recipes with an anchor, companion, and
highlight character. Resolve those recipes into accessible light and dark
Material roles through one shared root color contract. Improve the Appearance
preview into a miniature composition using the actual resolved scheme, then
audit live role usage so primary, secondary, and tertiary roles appear in
appropriate expressive surfaces without turning all reading surfaces into
loud color fields.

Wallpaper and Custom continue to use the same resolver contract. Custom stays
a one-seed editor for this effort. All theme sources preserve system light/dark
mode, persistence, per-Habit identity colors, and the “inspired by” boundary
for external themes.

## User Stories

1. As a Nines user, I want Catppuccin to feel like a coordinated palette rather
   than a purple tint, so selecting it feels worthwhile.
2. As a Nines user, I want Tokyo Night, Dracula, Nord, and the other named
   sources to retain recognizable color character, so I can tell them apart.
3. As a Nines user, I want primary, secondary, and tertiary accents to have
   distinct emphasis, so the interface has visual hierarchy.
4. As a Nines user, I want large reading surfaces to stay calm, so added color
   does not make the app noisy or reduce readability.
5. As a Nines user, I want selected controls, progress visuals, onboarding
   artwork, and supporting containers to express the chosen palette, so the
   theme feels alive throughout the app.
6. As a Nines user, I want the Appearance preview to show the real palette
   relationships before I select a source, so previews are trustworthy.
7. As a Nines user, I want the preview to show primary, secondary, tertiary,
   surface, and container roles together, so I can understand the hierarchy.
8. As a Nines user, I want text and icons to remain readable on every generated
   role, so expressiveness never compromises accessibility.
9. As a Nines user, I want a theme to remain recognizable in both light and
   dark mode, so changing system mode does not erase its identity.
10. As a Nines user, I want Custom to remain simple to use with one seed color,
    while still receiving richer supporting roles automatically.
11. As a Nines user, I want Wallpaper mode to keep following my system source,
    so named-theme improvements do not weaken dynamic color.
12. As a Nines user, I want my selected source to persist after relaunch, so I
    do not have to configure Appearance repeatedly.
13. As a Nines user, I want Habit identity colors to remain distinct from the
    app palette, so I can still recognize individual Habits.
14. As a Nines user, I want named themes to be inspired by their references
    without pretending to be literal ports, so Material behavior remains
    coherent.
15. As a maintainer, I want the root resolver to remain the single source of
    truth, so dialogs, previews, system bars, and screens cannot drift apart.
16. As a maintainer, I want automated checks for role distinction and pair
    readability, so a future palette change cannot silently collapse into one
    tint.
17. As a maintainer, I want connected-device review of representative themes,
    so color math is checked against the rendered app rather than trusted on
    paper.

## Implementation Decisions

- Add a richer palette-recipe model for named sources. Each recipe expresses
  anchor, companion, and highlight characteristics; it must not become a raw
  table of copied external theme roles.
- Preserve one root color-resolution seam that returns the complete Material
  light/dark `ColorScheme` for Wallpaper, named recipes, and Custom.
- Keep Wallpaper system dynamic color and Custom one-seed editing semantics.
  Supporting roles may be derived more expressively, but Custom does not gain
  a multi-color editor in this effort.
- Use Material semantic roles according to their intended emphasis: primary
  for high-emphasis actions, secondary for lower-emphasis controls, tertiary
  for smaller special emphasis, surfaces for large calm areas, and containers
  as fills with their paired `on*` content roles.
- Replace the current sparse Appearance preview with a mini composition built
  from the resolved scheme itself. It must show actual role pairings rather
  than decorative swatches.
- Audit current live role consumption and make targeted changes in previews,
  onboarding/illustration elements, selected controls, progress visuals, and
  supporting containers. Do not recolor every reading surface or introduce
  screen-local theme constants.
- Adapt tonal values and chroma per light/dark mode when needed to preserve
  both theme identity and readable role pairings.
- Keep named themes explicitly “inspired by” external palettes. Use documented
  accent inspiration only; do not copy code, assets, or literal role tables.
- Preserve the root precedence decision in ADR-0011 and the richer recipe
  trade-off in ADR-0012.

## Testing Decisions

- Test the highest public color-resolution seam with deterministic source,
  mode, and availability inputs. Assert source identity, role distinction,
  and valid paired foreground contrast rather than private generator details.
- Cover representative named themes—Catppuccin, Tokyo Night, and Dracula—plus
  Custom and Wallpaper in both light and dark mode.
- Test Appearance semantics for the mini composition and selected source state.
- Run connected-device smoke checks that select representative named themes,
  inspect the live app and preview, relaunch, and confirm the source persists.
- Use the existing Appearance DataStore and Compose semantics testing patterns;
  add no UI test framework or domain persistence layer.

## Out of Scope

- A full screen-by-screen visual redesign.
- A multi-color Custom editor.
- Manual light/dark mode selection.
- Changing per-Habit palette ownership or persistence.
- Literal copying of external theme implementations, assets, or raw role
  tables.
- Wallpaper image/content extraction beyond Android system dynamic color.
- Replacing Material semantic roles with arbitrary decorative colors.

## Further Notes

Material guidance treats primary, secondary, and tertiary as distinct accent
roles, surface as a calm large-area role, containers as fills, and `on*` roles
as paired foregrounds. The implementation should use that system to create
hierarchy, not bypass it for visual noise.
