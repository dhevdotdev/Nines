# 04 — Expand theme collection and add Custom

**What to build:** Grow Appearance beyond its initial small set of developer
theme-inspired palettes and let a person create one entirely personal Material
palette from a chosen seed color.

**Blocked by:** 01, 02 — existing source resolution and Appearance picker.

**Status:** done

- [ ] Rename the classic-theme grouping to neutral, user-facing language and
  expand it to the documented twelve theme-derived sources.
- [ ] Store Custom's selected seed color alongside its source selection; it
  applies, previews, and survives a relaunch like every other source.
- [ ] Custom uses a touch color picker inside Material UI chrome and generates
  one full HCT ColorScheme. It never asks users for individual role colors.
- [ ] Focused persistence/resolution and Compose semantics coverage verify the
  expanded collection and custom selection.

## Comments

This is an approved extension of the original Appearance work. It deliberately
keeps named themes as inspired seed sources, not code or visual ports.

Implemented with twelve theme-derived HCT sources and a persisted Custom seed
editor. Full JVM and connected-device regression tests passed.
