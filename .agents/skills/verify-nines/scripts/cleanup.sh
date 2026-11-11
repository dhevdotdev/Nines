#!/usr/bin/env bash
# Stop the app and uninstall nothing. If launch.sh started the emulator, pass --emulator to shut it down.
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"
$ADB shell am force-stop dev.dhev.nines
[ "${1:-}" = "--emulator" ] && $ADB emu kill
exit 0
