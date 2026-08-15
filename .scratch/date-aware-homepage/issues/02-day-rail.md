# 02 — Day rail: select a recent date, see and edit that date's entries

**What to build:** The core of the feature. A horizontally-scrollable day rail appears at the top of the homepage, showing a fixed window of recent days ending at today. Tapping a day selects it, and every row updates to show and allow editing that date's actual state — completion, numeric value, score ring, and streak, all as-of the selected date. Habits that didn't exist yet as of that date are excluded. Editing a past date behaves exactly like editing today: instant, no extra confirmation. A clear but unobtrusive indicator shows whenever the selected date isn't today.

**Blocked by:** 01 — date-scoped state and pure visibility logic.

**Status:** ready-for-agent

- [ ] A horizontally-scrollable day rail appears at the top of the homepage, showing a fixed window of recent days ending at today
- [ ] Tapping a day in the rail selects it and the list updates to that date's state for every visible habit
- [ ] Habits not yet created as of the selected date are excluded from the list
- [ ] Toggling a boolean habit or editing a numeric habit's value while a past date is selected writes to that date, with the same instant behavior as editing today — no added confirmation step
- [ ] The score ring and streak count on each row reflect the selected date, not always today
- [ ] A clear but unobtrusive indicator is visible whenever the selected date isn't today, and disappears when it is
- [ ] Verified on-device via screenshots: selecting a past date, editing an entry there, and confirming the change persists and the indicator/ring/streak all update correctly
