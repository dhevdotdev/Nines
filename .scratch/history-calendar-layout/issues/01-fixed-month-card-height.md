# 01 — Keep history month cards a stable height

**What to build:** Keep the history calendar's month dot-matrix cards at a stable height even when a month uses five rather than six calendar weeks.

**Status:** done

- [x] Reserve space for the maximum six-week month grid.
- [x] Prevent the content below the history rail from shifting when the visible month changes.
- [x] Verify the layout still compiles, passes lint, and is installed on the test device.

## Comments

The month cards previously sized themselves to their actual number of rows. Because the cards live in a horizontally scrolling `LazyRow`, the rail's measured height could change while scrolling, moving the section below it. A fixed height based on the six-week maximum resolves the layout instability without changing the dot matrix itself.
