# Streak/gap derivation consistency

**Status:** superseded

The timeline and detail analytics independently derive streak and gap runs from the same bounded
completed dates. Centralize that pure derivation and let the timeline hand its result to analytics,
so the two projections cannot quietly disagree as their callers evolve.
