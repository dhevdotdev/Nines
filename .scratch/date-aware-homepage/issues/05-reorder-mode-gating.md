# 05 — Gate reorder mode on the selected date

**What to build:** The reorder-mode toggle becomes unavailable while a past date is selected — reordering has nothing to do with which date is showing. If a past date gets selected while reorder mode is already active, reorder mode exits automatically. The toggle becomes available again once the selected date returns to today. Swipe-to-archive and swipe-to-edit are unaffected by any of this, since they're habit-level actions — this ticket confirms that explicitly rather than assuming it.

**Blocked by:** 02 — day rail.

**Status:** ready-for-agent

- [ ] The reorder-mode toggle is unavailable (hidden or disabled) whenever a past date is selected
- [ ] If a past date is selected while reorder mode is already active, reorder mode exits automatically
- [ ] The reorder-mode toggle becomes available again once the selected date returns to today
- [ ] Swipe-to-archive and swipe-to-edit are confirmed still available and working regardless of which date is selected
- [ ] Verified on-device via screenshots: attempting to enter reorder mode on a past date, confirming it's blocked/hidden, then confirming it works normally after returning to today
