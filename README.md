# Nines

A habit tracker for Android whose score works like an uptime SLO: a missed day spends part of an error budget that decays over time, instead of resetting a streak to zero.

Built with Kotlin, Jetpack Compose and Material 3 Expressive. Single `:app` module, Koin, Navigation 3, Room. Requires Android 8.0 (API 26) or newer.

## Build

You need the Android SDK (set `ANDROID_HOME` or `sdk.dir` in `local.properties`) and a full JDK 25. GraalVM builds fail: their `jlink` can't create the Android JDK image.

```sh
./gradlew :app:assembleDebug     # debug APK
./gradlew :app:testDebugUnitTest # unit tests
```

Debug builds on a device or emulator: see `.agents/skills/verify-nines`. Instrumented tests (Room, reminders, UI) run on a device or emulator with `./gradlew :app:connectedDebugAndroidTest`.

## Project

- `GLOSSARY.md` defines the domain terms (Habit, Entry, Schedule, Score, Streak).
- `docs/adr/` records architectural decisions.
- Planned work is tracked in [GitHub Issues](https://github.com/dhevdotdev/Nines/issues).
- `AGENTS.md` holds the conventions for coding agents working in this repo.

Released under the [MIT License](LICENSE).
