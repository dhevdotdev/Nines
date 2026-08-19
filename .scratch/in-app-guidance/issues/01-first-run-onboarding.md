# 01 — First-run onboarding flow

**What to build:** Show a polished four-page introduction on a fresh installation so a new user understands Nines' core mental model before creating a Habit.

**Blocked by:** None — can start immediately.

**Status:** done

- [x] A fresh installation opens four pages covering welcome, Entries and Targets, Score/Streaks/Insights, and getting started.
- [x] Each page has a focused animated Compose preview using deterministic illustrative data, not interactive fake controls or literal screenshots.
- [x] Next, horizontal swipe, Back, Skip, and Get started follow the approved behavior, with a subtle progress indicator.
- [x] Skip and Get started land on the real homepage and prevent automatic onboarding on later launches.
- [x] Onboarding uses local DataStore Preferences state, dynamic color, accessibility semantics, and reduced-motion behavior.
- [x] JVM/Compose semantics coverage and a connected fresh-install smoke flow verify the user-visible behavior.

## Visual direction follow-up

The onboarding artwork uses the selected Handmade Signal direction from the
`prototype/onboarding-art` comparison. The production motion uses the Signal
choreography selected from `prototype/onboarding-motion` (`f43ca6c`): the
geometry moves independently with the expressive motion scheme's spatial
spring tokens while the page itself stays stable. The reduced-motion setting
renders the settled state directly.

The final implementation uses a distinct composition per onboarding page:
routine path, target rings, insight trend, and a centered nine-node starting
pattern. The artwork is intentionally unframed; an enclosing squircle made the
composition feel like an app icon rather than page-level illustration.

Signal playback is keyed to the pager's current page, not composition. Compose
precomposes neighboring pages, so launching from each page's first composition
silently completed page-one/page-two motion before those pages were visible.
Waiting for the fully settled page plus a fixed delay was also rejected: it
left entry geometry visibly frozen after the swipe. Inactive pages stay reset,
then animate as they become current; reduced motion renders the final state.
An outgoing page retains its completed geometry while inactive. Resetting it
on either `currentPage` or `settledPage` changes made page three's insight
marker jump to its off-canvas entry position while the pager still retained an
outgoing frame in the 3-to-4 swipe seam. A revisited page resets and relaunches
only when it becomes current again.
