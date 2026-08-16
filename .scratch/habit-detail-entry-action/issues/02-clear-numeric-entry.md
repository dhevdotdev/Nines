# 02 — Clear a numeric Entry with undo

**What to build:** Let a user explicitly clear an existing numeric Entry from the shared editor, returning the Selected date to `Not logged` with an undo path.

**Blocked by:** 01 — Extract and reuse the shared numeric Entry editor

**Status:** ready-for-agent

- [ ] Add an explicit Clear entry action only for existing numeric Entries.
- [ ] Delete the selected date's Entry row rather than writing zero or skipped state.
- [ ] Restore the deleted Entry through undo.
- [ ] Refresh the current Selected date without returning to today.
- [ ] Cover clear/undo behavior at repository/ViewModel and Compose semantics seams.
