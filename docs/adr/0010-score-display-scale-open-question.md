---
status: proposed
---

# Open: what scale should Score actually display at?

Nines' name and framing are built on literal SRE "nines" of uptime (99%, 99.9%, 99.99%). But at roughly one loggable event per day, a target in the 99.9%+ range allows effectively zero misses over any realistic window — statistically meaningless at this event rate, unlike a service handling thousands of requests per second. Displaying Score on that literal scale would make the product's own core metric read as either permanently perfect or catastrophically failing, with no meaningful middle.

**Not yet decided**: whether the user-facing Score scale should compress into a more usable range (e.g. 90/95/99/99.5%) while keeping the "nines" *name* and conceptual framing (EWMA decay, error-budget-style misses — see `docs/adr/0002-score-and-streaks-are-derived.md`), or whether the literal uptime-percentage framing needs to be dropped or reworked for the stats/scoring UI specifically.

**When this needs resolving**: before building or redesigning any screen that surfaces Score as a number or percentage to the user (a stats screen, a detail-screen summary, etc.) — raise it explicitly during that feature's `/grill-with-docs` pass rather than picking a default silently.
