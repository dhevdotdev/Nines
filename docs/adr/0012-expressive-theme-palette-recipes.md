# Expressive theme palette recipes preserve Material roles

## Context

Named Nines palettes currently resolve from one accent seed through the same
HCT generator. Although this produces a complete `ColorScheme`, the rendered
app can feel monochromatic because one dominant hue is doing most of the visual
work. The user wants named themes to feel recognizably rich and coordinated,
with meaningful primary, secondary, tertiary, container, and surface
relationships.

## Decision

Represent named themes as palette recipes rather than single-seed tints. A
recipe may provide coordinated accent characteristics inspired by a named
theme, while Nines still derives accessible Material light/dark roles through
the shared color-resolution contract. Theme names remain “inspired by” labels,
not literal ports of external theme implementations or raw role tables.

Use the same resolution contract for Wallpaper, named themes, and Custom. The
Appearance preview renders a small composition of actual role pairings, and the
live app receives a targeted role-usage audit so expressive roles appear where
they improve hierarchy without turning every surface into a loud color field.

## Alternatives considered

- Keep one seed per theme and only change the preview. Rejected because the
  live application would remain visually monochromatic.
- Hardcode screen-specific decorative colors. Rejected because it would bypass
  Material semantics, fragment the root theme, and weaken accessibility.
- Copy literal palettes or role tables from external themes. Rejected because
  Nines uses references for inspiration only and must preserve its own Material
  role contract.

## Consequences

- Named themes need richer source metadata than one accent seed.
- Custom remains one user-selected seed for now, with supporting roles derived
  algorithmically; multi-color editing is a separate feature.
- Contrast validation remains authoritative over exact theme fidelity.
- Wallpaper dynamic color and per-Habit identity harmonization remain intact.
