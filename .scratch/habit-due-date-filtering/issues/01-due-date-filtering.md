# Frequency-aware progress windows

Status: done

Right now every non-archived, already-created habit shows on the list every single day, but a frequency target such as "3×/week" gives the user no useful progress context. The original proposal was to hide habits on arbitrary "not due" days, but flexible numerator/denominator schedules do not define particular weekdays.

Surfaced while grilling the date-aware-homepage redesign (`.scratch/date-aware-homepage/`): a real "is this habit due" filter would need to answer which specific days count for a flexible ratio, but that is undefined. The approved direction is frequency-aware progress in a rolling window ending on the Selected date. The existing EWMA scoring math (`docs/adr/0002-score-and-streaks-are-derived.md`) remains separate and unchanged.

Deliberately deferred out of the date-aware-homepage redesign rather than solved shallowly just for that ticket. It was specified in `.scratch/habit-due-date-filtering/spec.md` and implemented through tickets 01 and 02.

## Comments

- Reframed during the approved uHabits/Loop-inspired grilling pass. The product decision is flexible rolling frequency progress, not arbitrary due weekdays or homepage filtering.
- Implemented and verified on-device: active habits remain visible, rolling progress updates for selected dates, and historical progress uses the effective schedule without future-entry leakage.
