# 02 — Clear a numeric Entry with undo

**What to build:** Let a user explicitly clear an existing numeric Entry from the shared editor, returning the Selected date to `Not logged` with an undo path.

**Blocked by:** 01 — Extract and reuse the shared numeric Entry editor

**Status:** done

- [x] Add an explicit Clear entry action only for existing numeric Entries.
- [x] Delete the selected date's Entry row rather than writing zero or skipped state.
- [x] Restore the deleted Entry through undo.
- [x] Refresh the current Selected date without returning to today.
- [x] Cover clear/undo behavior at repository/ViewModel and Compose semantics seams.
