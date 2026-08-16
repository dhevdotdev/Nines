# 03 — Use the shared editor on detail hero and history

**What to build:** Replace the numeric detail hero's increment/decrement affordance and the detail-only numeric dialog with the shared numeric Entry editor, while retaining direct boolean check-off.

**Blocked by:** 01 — Extract and reuse the shared numeric Entry editor; 02 — Clear a numeric Entry with undo

**Status:** ready-for-agent

- [ ] Numeric detail hero uses the homepage's 44dp expressive edit action grammar.
- [ ] Numeric detail hero opens the shared editor instead of mutating on tap/long press.
- [ ] Detail history/calendar opens the same editor and supports Clear entry.
- [ ] Boolean detail hero and history behavior remain unchanged.
- [ ] Historical save/clear actions preserve Selected date.
- [ ] Remove the obsolete detail-only numeric editor.
- [ ] Run JVM, Compose semantics, and connected Android verification.
