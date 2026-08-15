# Reference apps inform features and UX only — never read or copy their source

Several existing habit trackers and Material You apps have been cited as UX/scoring/pattern references during Nines' design research; at least one referenced habit tracker is GPL-3.0. Nines' own source has never been read from or copied out of any reference app — only published docs, file trees, build configs, and restated algorithms (e.g. a habit-scoring formula, reimplemented independently in `scoring/HabitScore.kt`) were used.

**Why**: copying GPL-3.0 code, even a small snippet, would obligate Nines to also be GPL-3.0 with published source. Functionality and UX concepts aren't copyrightable, so reimplementing from scratch keeps that option open. This matters because Nines isn't currently monetized but keeping the option open was an explicit requirement.

**How to apply going forward**: when researching a reference app (including future ones), read published documentation, screenshots, and describe observed behavior — never open or paste in a reference app's source file. If an agent or research pass ever surfaces literal source from a GPL-licensed reference project, strip it before it lands in this repo.
