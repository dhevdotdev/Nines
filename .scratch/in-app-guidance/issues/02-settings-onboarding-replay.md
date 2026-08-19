# 02 — Replay “How Nines works” from Settings

**What to build:** Let a returning user replay the same onboarding flow from Settings without changing the first-run completion state.

**Blocked by:** 01 — First-run onboarding flow

**Status:** done

- [x] Settings exposes a clearly labeled “How Nines works” entry.
- [x] Selecting it opens the exact shared onboarding flow used on first launch.
- [x] Completing or leaving the replay does not cause onboarding to appear automatically on the next normal launch.
- [x] Back navigation returns to Settings or the expected parent surface without corrupting the main navigation stack.
- [x] Compose semantics and connected navigation coverage verify replay behavior.
