# 04 — Verify expressive palettes across modes and relaunch

**What to build:** Finish the palette effort with representative light/dark,
source-switching, persistence, accessibility, and on-device verification so a
theme cannot regress to a monochromatic or unreadable result.

**Blocked by:** 01, 02, 03 — palette resolution, preview, and live role usage.

**Status:** done

- [x] Automated checks cover Catppuccin, Tokyo Night, Dracula, Custom, and
      Wallpaper in light and dark mode.
- [x] Role distinction and paired foreground readability have explicit test
      assertions rather than only screenshot-based judgment.
- [x] Connected verification selects representative sources, checks the live
      app and Appearance preview, relaunches, and confirms persistence.
- [x] Final review confirms no raw external role tables, per-screen theme
      constants, or regressions to Habit identity colors were introduced.
