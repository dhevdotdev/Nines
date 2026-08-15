# MotionScheme.expressive() is wired app-wide; spec choice follows a fixed spatial-vs-effects rule

`Theme.kt` sets `motionScheme = MotionScheme.expressive()` once, at the theme root — not scoped to isolated hero moments. Everywhere in the app that picks an animation spec follows one rule: **spatial** specs (`MaterialTheme.motionScheme.defaultSpatialSpec()`/`fastSpatialSpec()`/etc.) for anything that moves, resizes, or reshapes; **effects** specs (`defaultEffectsSpec()`/etc.) for color/alpha-only changes. Effects specs never overshoot; spatial specs can and often should.

**Why**: applying the expressive scheme app-wide (rather than only to a few hero interactions) keeps motion consistent everywhere instead of some elements feeling "expressive" and others feeling like a different, more conservative app. The spatial-vs-effects split is the mechanism `MotionScheme` itself is built around, not a house convention layered on top — following it rather than picking specs ad hoc keeps every animation's overshoot behavior intentional (a color fade should never bounce; a card sliding into place plausibly should).

**Consequences**: when adding a new animation, first ask "does this move/resize/reshape, or does it only change color/alpha" — that answer picks the spec category before anything else about duration or easing.
