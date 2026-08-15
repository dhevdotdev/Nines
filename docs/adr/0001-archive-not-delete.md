# Archiving replaces deletion for habits

`HabitRepository.archiveHabit` has never been a hard delete — it sets `archivedAt` on the entity and leaves all history intact. `restoreHabit` clears `archivedAt` and re-appends the habit to the end of the active list; there's a dedicated Settings → Archived Habits screen for this. UI icons/labels for this action say "Archive," but the underlying action type is still named `HabitsAction.DeleteHabit` — a rename to match, not a behavior change.

**Why**: a habit's entry history has real value even after the user stops tracking it (streaks, scoring, past data) — a real delete would throw that away irrecoverably on a single accidental swipe, which is an unacceptably easy way to lose data on a gesture-driven list.

**Consequences**: there is currently no way to permanently, irrecoverably delete a habit from the UI at all. If that's ever needed, it's a new, deliberately separate action — don't repurpose the existing archive swipe/action for it.
