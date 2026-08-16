# Measurable detail-page Entry action

**Status:** superseded
**Superseded by:** `.scratch/habit-detail-entry-action/spec.md` and tickets 01–03

The detail page's numeric hero previously incremented/decremented the selected
date directly, while the homepage and detail history had separate numeric editing
surfaces. The approved direction is one shared numeric Entry editor everywhere:
empty for an unlogged Entry, prefilled for an existing Entry, explicit Target
context, valid non-negative numeric input, and an explicit clear action with undo.
Boolean habits retain direct check-off behavior.
