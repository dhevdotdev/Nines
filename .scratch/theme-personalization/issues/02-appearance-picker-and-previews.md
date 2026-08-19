# 02 — Appearance picker and truthful palette previews

**What to build:** Give Settings an Appearance destination where people can
preview and choose Wallpaper or a curated Nines palette, with the chosen
appearance applying immediately and remaining clear on Android versions that
cannot supply wallpaper dynamic color.

**Blocked by:** 01 — App color source foundation.

**Status:** done

- [ ] Settings exposes Appearance and back navigation preserves the existing
  Settings flow.
- [ ] Every source has a labeled, accessible selected state and a compact
  preview generated from its actual resolved Material color scheme.
- [ ] Wallpaper accurately communicates platform availability while curated
  choices remain usable everywhere.
- [ ] Compose semantics and connected-device verification cover selection,
  previews, persistence, and readable live application.

## Comments

Implemented with actual ColorScheme role-pair previews. Device verification
confirmed immediate recoloring and persistence after a process restart.
