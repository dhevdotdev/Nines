# 01 — Resolve coordinated theme palette recipes

**What to build:** Replace the single-seed named-theme behavior with coordinated
palette recipes that resolve to complete, accessible Material light and dark
schemes while preserving Wallpaper, Custom, persistence, and per-Habit color
semantics.

**Blocked by:** None — can start immediately.

**Status:** ready-for-agent

- [ ] Named sources expose anchor, companion, and highlight palette
      characteristics without copying external role tables.
- [ ] Catppuccin, Tokyo Night, Dracula, and representative remaining sources
      resolve to visibly distinct primary, secondary, and tertiary roles in
      both light and dark mode.
- [ ] Wallpaper and Custom continue through the same root resolution contract,
      with Custom remaining one-seed and persisted.
- [ ] Paired `on*` roles remain readable and source selection semantics remain
      unchanged.
- [ ] Deterministic automated tests cover representative sources, both modes,
      Wallpaper fallback, Custom, role distinction, and paired contrast.
