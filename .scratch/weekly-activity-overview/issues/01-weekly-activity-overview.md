# 01 — Always-visible past-7-days activity overview

**Status:** needs-triage

## Problem

A colleague shown the app went looking for a glanceable "how did the last week go" view first, before anything else, and it isn't there. What exists today doesn't cover this:

- The homepage day rail (`DayRail.kt`) is a calendar-week-anchored *date selector*, not an activity overview — it shows one selected date at a time across all habits, not a per-habit or aggregate view of the past 7 days at a glance.
- The old per-row 5-dot history strip on the homepage (`HabitListItem.recentDays`) was removed in the date-aware-homepage work (ticket 04) in favor of the day rail — so there's currently no per-habit recent-activity glance on the homepage at all.
- The detail screen has a real history/calendar view, but it's per-habit and requires navigating in — not something shown "consistently, all the time" the way the colleague expected.

## Open questions for triage/grilling

- Where does this belong: a card/section on the homepage (visible without navigating anywhere), a restored-but-redesigned per-row strip, or something on the detail screen made more prominent?
- Per-habit (grid of dots per habit) vs. aggregate (one view summarizing all habits' completion for the week)?
- How does this coexist with the day rail, which already occupies the top-of-homepage, calendar-week-anchored real estate?
