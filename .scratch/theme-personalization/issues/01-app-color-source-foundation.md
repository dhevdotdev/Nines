# 01 — App color source foundation

**What to build:** Let Nines resolve Wallpaper and each curated app color
source into one complete, app-wide Material color scheme that persists across
launches and safely falls back when wallpaper dynamic color is unavailable.

**Blocked by:** None — can start immediately.

**Status:** done

- [ ] Wallpaper is the default persisted source; Android 12+ uses the system
  wallpaper scheme and earlier Android safely resolves Wallpaper to Nines.
- [ ] Nines, Forest, Ocean, Sunset, Catppuccin, Tokyo Night, Dracula, and Nord
  each resolve to complete light/dark HCT Material color schemes using the
  app's existing color-science configuration.
- [ ] Changing source updates the root theme and system bars immediately;
  existing per-Habit color harmonization follows the new app primary.
- [ ] Persistence and resolution behavior have focused automated coverage.

## Comments

Implemented and verified with focused JVM/connected tests, full regression
tests, and device selection/relaunch checks.
