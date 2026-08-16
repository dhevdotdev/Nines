# 02 — Expressive shared habit action group

**What to build:** Replace isolated homepage habit controls with one coherent action grammar. Boolean habits expose primary completion plus secondary habit-configuration edit; numeric habits expose primary increment plus secondary exact-value edit. The group should feel like one Material 3 Expressive interaction, not unrelated floating buttons, while keeping the primary logging action visually stronger.

**Blocked by:** 01 — Card anatomy and selected-date status.

**Status:** superseded

**Superseded by:** `.scratch/homepage-entry-actions/issues/01-homepage-entry-action-grammar.md`

- [x] Boolean cards expose a primary completion action and secondary habit-configuration edit action.
- [x] Numeric cards expose a primary increment action and secondary exact-value action labelled "Set value".
- [x] The primary logging action has stronger visual emphasis than the secondary edit action.
- [x] Pressed items use local expressive scale/shape feedback with clear feedback and stable card geometry; width redistribution is avoided in this compact action group.
- [x] Completion updates local action/status treatment and Score feedback without changing card height or list order.
- [x] The same action behavior works for today and historical selected dates.
- [x] Swipe-to-archive and swipe-to-edit remain available outside reorder mode.
- [x] Reorder-mode gating remains unchanged: reorder is available only for today.
- [x] Action semantics and accessibility labels distinguish logging, exact editing, and habit configuration.
- [x] Light/dark on-device motion and interaction behavior are verified before completion.
