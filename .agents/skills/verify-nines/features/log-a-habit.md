# Add a habit and log today

Create a yes/no habit and mark it done for today.

## Sub-features

- New habit sheet: name, type (Yes / no or Measurable), frequency (Daily, Per week, Custom), reminder, color, background, icon.
- Completion button on the habit card, with the streak line and the "All done for today" banner.

## How to get to it (user POV)

From an empty list, tap "Add a habit". With habits present, tap the plus button (`Add habit`). Fill in the sheet and tap `Add habit` at its bottom.

## Driving it with ui.py

1. Fresh install, `ui.py tap Skip`.
2. `ui.py tap "Add a habit"`, wait 2s.
3. `ui.py tap Name`, then `adb shell input text Meditate`, then `adb shell input keyevent 4` to close the keyboard.
4. `adb shell input swipe 540 1900 540 500 300`, then `ui.py tap "Add habit"`.
5. `ui.py dump` shows `Meditate`, `Not logged` and `Mark Meditate complete`.
6. `ui.py tap "Mark Meditate complete"`.

Proof: the dump shows `Done`, `Mark Meditate incomplete` and `All done for today`; the screenshot shows the "1 day streak" line; `scripts/db.sh "select * from entries;"` returns a row with `state` `COMPLETED` for today's `date`.

## Gotchas

- Name the habit one word. Longer typed text picks up keyboard autocomplete (`Read 10 pages` became `Read 10 pages gym`).
- The sheet's save button is `Add habit`, the same label as the plus button on the list.
- A numeric (Measurable) habit opens the numeric editor instead of toggling. That path is not mapped yet.
