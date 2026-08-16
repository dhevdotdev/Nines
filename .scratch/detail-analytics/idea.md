# Detail-screen analytics seam

**Status:** superseded

The detail ViewModel had accumulated independent rules for streak ranking, weekday rates, momentum, typical logging time, gaps, numeric aggregates, and record progress. The approved architecture candidate was to extract those rules behind one pure analytics interface fed by date-bounded timeline entries, while leaving UI copy and history presentation in the detail adapter.
