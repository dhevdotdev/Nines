# 01 — Detail screen Insights section is deeply data-conditioned, not a stable layout

**Status:** needs-triage

## Verified

`InsightsSection` in `HabitDetailInsights.kt:99-140` builds its row list from seven independently-optional sources, each shown only if its data exists:

| Row | Shown only when |
|---|---|
| `RecordProgressRow` | `recordProgressLabel != null` |
| `WeekdayBreakdownRow` | exactly 7 `weekdayRates` |
| `TargetProgressRow` (the bar graph) | numeric habit **and** `targetKind != null` **and** `targetValue > 0` **and** ≥7 days of history |
| `MomentumRow` | `momentumDeltaPoints != null` |
| `TypicalTimeRow` | `typicalTimeOfDay != null` |
| `NumericTotalsRow` | numeric habit with both total and average present |
| `LongestGapRow` | `longestGap != null` |

Line 141: `if (rows.isEmpty()) return` — if none of the seven conditions are met (e.g. a brand-new habit, or a boolean habit with no gaps and no record streak yet), the **entire Insights card and its "Insights" heading disappear**, not just individual rows. The user never sees "no insights yet" — the section just isn't there.

## Why this matters

The detail screen's height, content, and what sections a user learns to expect all shift based on how much history/progress a specific habit happens to have — a boolean habit with a clean streak might show 3 rows, a numeric habit with a target might show a completely different visual anchor (the bar graph), and a fresh habit shows nothing at all. Reported by a colleague being shown the app: what they expected to see on the detail page was conditioned on which habit they happened to look at, not a consistent layout — undermines trust that the page "always shows X."

## Open questions for triage/grilling

- Should rows that don't yet have data show an empty/placeholder state instead of disappearing, so the section's presence and row count is stable across habits?
- Is a completely-empty Insights section (new habit, no data yet) an acceptable case to hide entirely, or should it show a "come back after a few days" placeholder?
- Does this same data-conditioned-visibility pattern exist elsewhere in the detail screen (e.g. `HabitDetailCalendar.kt`) and need the same treatment, or is Insights the only offender?
