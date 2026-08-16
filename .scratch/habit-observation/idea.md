# Habit observation/read-model seam

**Status:** superseded

The selected-date timeline, detail analytics, and test layers are now deepened, but the homepage and detail adapters still assemble parts of the same Habit observation independently. The next architecture candidate is one date-bounded observation module used by both callers, including the selected-date projection and bounded calendar-day data, while leaving UI copy and presentation in the screen adapters.
