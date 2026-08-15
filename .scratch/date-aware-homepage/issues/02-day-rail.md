# 02 — Day rail: select a recent date, see and edit that date's entries

**What to build:** The core of the feature. A horizontally-scrollable day rail appears at the top of the homepage, showing a fixed window of recent days ending at today. Tapping a day selects it, and every row updates to show and allow editing that date's actual state — completion, numeric value, score ring, and streak, all as-of the selected date. Habits that didn't exist yet as of that date are excluded. Editing a past date behaves exactly like editing today: instant, no extra confirmation. A clear but unobtrusive indicator shows whenever the selected date isn't today.

**Blocked by:** 01 — date-scoped state and pure visibility logic.

**Status:** ready-for-agent

- [x] A horizontally-scrollable day rail appears at the top of the homepage, showing a fixed window of recent days ending at today
- [x] Tapping a day in the rail selects it and the list updates to that date's state for every visible habit
- [x] Habits not yet created as of the selected date are excluded from the list
- [x] Toggling a boolean habit or editing a numeric habit's value while a past date is selected writes to that date, with the same instant behavior as editing today — no added confirmation step
- [x] The score ring and streak count on each row reflect the selected date, not always today
- [x] A clear but unobtrusive indicator is visible whenever the selected date isn't today, and disappears when it is
- [x] Verified on-device via screenshots: selecting a past date, editing an entry there, and confirming the change persists and the indicator/ring/streak all update correctly

## Comments

Done. Two deviations from the original description, both from live user feedback during implementation, not scope creep:

- **Calendar-week anchor, not a rolling "last 7 days ending today" window.** A rolling window always pins today to the trailing edge with no breathing room; matches most calendar/fitness apps' weekly strips instead. Days after today within the week are shown but muted and non-interactive - future is still never actually reachable.
- **Full-width `Row` with `weight(1f)` per chip, not a `LazyRow`.** A `LazyRow` sized to each chip's natural width overflowed on-device, clipping the 7th day right at the screen edge. Since exactly 7 days always exist, dividing the full available width evenly guarantees every chip renders whole with no scrolling at all.

Motion/shape choices for the selected-day chip (spring pop, animated Circle→Cookie9Sided morph via `androidx.graphics.shapes.Morph`) were verified against m3.material.io before implementing, not guessed - see commit message for citations.

One more unticketed but deliberate call: the "All done for today" banner is now also gated on `selectedDate == today`, so it can't fire while browsing a past date where remaining habits may just not be visible yet.
