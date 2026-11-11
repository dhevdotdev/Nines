# Nines

A habit tracker for Android whose score works like an uptime SLO: a missed day spends part of an error budget that decays over time, instead of resetting a streak to zero.

Built with Kotlin, Jetpack Compose and Material 3 Expressive. Single `:app` module, Koin, Navigation 3, Room. Requires Android 8.0 (API 26) or newer.

## Build

```sh
./gradlew :app:assembleDebug     # debug APK
./gradlew :app:testDebugUnitTest # unit tests
```

Instrumented tests (Room, reminders, UI) run on a device or emulator with `./gradlew :app:connectedDebugAndroidTest`.

## Project

- `GLOSSARY.md` defines the domain terms (Habit, Entry, Schedule, Score, Streak).
- `docs/adr/` records architectural decisions.
- Planned work is tracked in [GitHub Issues](https://github.com/dhevdotdev/Nines/issues).
- `AGENTS.md` holds the conventions for coding agents working in this repo.

Nines is a personal project and is not yet licensed for reuse.
