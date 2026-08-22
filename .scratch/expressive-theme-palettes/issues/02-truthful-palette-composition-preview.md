# 02 — Show a truthful expressive palette composition

**What to build:** Replace the sparse Appearance source preview with a compact
mini composition that makes the selected source’s actual Material hierarchy
visible before selection.

**Blocked by:** 01 — Resolve coordinated theme palette recipes.

**Status:** done

- [x] Each source preview is generated from the same resolved scheme used by
      the root app theme.
- [x] The composition visibly demonstrates surface/container, primary,
      secondary, tertiary, and paired foreground roles together.
- [x] Preview content remains accessible, compact, and clearly associated with
      its source selection semantics in both light and dark mode.
- [x] Appearance semantics tests cover source labels, selected state, and the
      role composition without asserting private layout details.
