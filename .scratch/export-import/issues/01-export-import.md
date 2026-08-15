# Export / import habit data

Status: needs-triage

No way currently to get data out of or into the app. Never scoped.

No design decided yet: file format (a raw DB export vs. a portable format like JSON/CSV), what's included (archived habits? entry history? just current state?), whether import is meant for restore-to-same-device or migrating from another tracker, and how this interacts with `Schedule` versioning (see `CONTEXT.md`) if imported data implies a different schedule history than what's reconstructable.

## Comments
