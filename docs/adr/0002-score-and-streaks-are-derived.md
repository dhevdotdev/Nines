# Score and streaks are always recomputed, never stored as truth

A habit's Score is computed via an exponentially-weighted moving average over completion entries (`m = 0.5^(√f/13)`), not a flat rolling-window percentage. Streaks and gaps (`computeStreakRuns`/`computeGapRuns` in the scoring package) are likewise always derived on demand from raw entries and the schedule versions in force at each date — none of these are stored as fields anywhere.

**Why**: EWMA was chosen over a flat window specifically because a flat window lets old misses "fall off the edge" and silently restore the budget the instant they scroll out of the window — the opposite of the reliability-budget framing the product is built around (see `CONTEXT.md`). Storing Score/streak as mutable fields would also require invalidating them on every retroactive schedule change, which versioned schedules (see the `Schedule` entry in `CONTEXT.md`) already make unnecessary as long as they stay pure functions of entries.

**Considered and rejected**: a flat rolling window (simpler, but has the silent-return-of-budget failure mode above); persisting Score as a cached column with manual invalidation (adds a whole class of staleness bugs for no real performance win at this data scale).
