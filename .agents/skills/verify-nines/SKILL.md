---
name: verify-nines
description: Launch, drive and prove behaviour in Nines, the Android habit tracker, on an emulator. Use to see a change working in the real UI, to reproduce a bug, or before saying a change works.
---

# Verify Nines

Nines is a single-activity Compose app (`dev.dhev.nines/.MainActivity`). Verification runs the debug APK on an Android emulator and drives it through `uiautomator`, reading state from the screen and from the Room database.

## Doctor

Run `.agents/skills/verify-nines/scripts/doctor.sh`. Every line must say `ok`. A GraalVM `java` fails the Android build, so put a full JDK 25 first (the repo pins daemon JVM 25; Amazon Corretto 25 works). Without an SDK, set `ANDROID_HOME` or `sdk.dir` in `local.properties`.

## Launch

`.agents/skills/verify-nines/scripts/launch.sh` boots the `nines-verify` emulator headless if no device is attached, builds the debug APK, installs it, wipes app data and starts it. Ready means the first-run onboarding ("Build a reliable routine", "1 of 4") is on screen. If the AVD is missing, create it with the Android command-line tools: `system-images;android-36;google_apis;arm64-v8a`, device `pixel_8`, name `nines-verify`.

A device already attached is used as is, and its Nines data is wiped. Do not run this against a phone that holds real habits.

## Drive

`scripts/ui.py` is the harness:

- `ui.py dump` lists visible nodes with text, content description and bounds.
- `ui.py tap "<text or content-desc>"` taps a node's centre.
- `ui.py type "<text>"` types into the focused field.

Use content descriptions, which are stable: `Add habit`, `Mark <name> complete`, `Mark <name> incomplete`, `Settings`, `Reorder habits`, `Choose a date`, and the day rail's `Tuesday, November 10` style labels. Wait about two seconds after a tap before dumping, because sheets animate. The soft keyboard covers the lower half of a sheet: send `adb shell input keyevent 4` to dismiss it, then `adb shell input swipe 540 1900 540 500 300` to scroll. Type one word at a time, since the emulator keyboard appends autocomplete suggestions to longer text.

- `scripts/seed.py` replaces the device's habits with six habits and 60 days of history (boolean, measurable, weekly targets, streaks), for screenshots and for driving past dates or numeric editing. Run it after `launch.sh`.

`features/README.md` lists what to drive and what proves each feature.

## Evidence

Write to a scratch folder outside the repository.

- Screenshots: `adb exec-out screencap -p > <scratch>/name.png`, taken before and after the action.
- State: `scripts/db.sh "select * from entries;"` reads the Room database. A claim about an Entry needs its row (`state`, `date`, `numericValue`), not only what the card says. `date` is days since the epoch.
- `./gradlew :app:testDebugUnitTest` is the regression pin for scoring and presentation logic. It does not prove anything a user sees.
- A run that could not complete is inconclusive, not a pass.

## Cleanup

`scripts/cleanup.sh` stops the app. Add `--emulator` only when this run's `launch.sh` started the emulator (it prints `emulator started by this run: 1`). Never kill an emulator you did not start. Evidence stays in your scratch folder.
