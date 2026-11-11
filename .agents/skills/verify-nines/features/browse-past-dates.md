# Browse past dates

Select an earlier day on the rail to see and edit that day's entries.

## Sub-features

- Day rail with a week of dates, a "Viewing <date>" banner and "Jump to today".
- Full calendar behind `Choose a date`.
- Reorder is disabled while a past date is selected.

## How to get to it (user POV)

The rail sits under the title on the homepage. Tap a day.

## Driving it with ui.py

`ui.py tap "Tuesday, November 10"`, wait 2s, `ui.py dump`. Return with `ui.py tap "Wednesday, November 11"` (today) or the `Jump to today` button. Day labels carry the real date, so compute them from the device date (`adb shell date`).

Proof: the dump shows `Viewing Tue, Nov 10` and the `Reorder habits` node's description becomes `Reorder habits unavailable for past dates`.

## Gotchas

- A habit created today does not appear on earlier dates: they show the empty state. Entry editing on a past date needs a habit created before that date, which the stock emulator image gives no way to seed. Create the habit first, then select only dates on or after its creation, or leave that path unproven.
- Driven so far: selecting a past date and the banner. Editing an entry on a past date and the full calendar are not driven.
