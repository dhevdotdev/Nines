# First-run onboarding

Four pages shown on a fresh install, replayable from Settings.

## How to get to it (user POV)

Launch on a clean data dir (`launch.sh` does this). Replay later from Settings, "How Nines works".

## Driving it with ui.py

`ui.py dump` on first launch shows `Build a reliable routine`, `1 of 4`, `Next` and `Skip`. `ui.py tap Skip` lands on the homepage.

Proof: after Skip the homepage dump shows `Nothing tracked yet` and `Add a habit`.

## Gotchas

- Driven: page 1 and Skip. Pages 2 to 4 and the Settings replay are not driven.
