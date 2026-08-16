# EWMA recomputation decision

**Status:** done

## Finding

The homepage recomputes each visible habit's score series when its observation is
projected. The work is already dispatched to `Dispatchers.Default`, and the
current product data scale has no measured frame hitch or trace showing this as a
problem.

## Decision

Do not introduce an EWMA cache, stored score, or speculative memoization now.
Score remains a pure projection from Entries and Schedule versions, consistent
with ADR-0002 and the domain glossary. Revisit only with a captured performance
trace demonstrating user-visible jank and a measured invalidation strategy.

## Verification

Existing score and timeline tests cover the EWMA behavior and schedule-aware
historical projection. This item is closed as an explicit deferment, not omitted
from the architecture review.
