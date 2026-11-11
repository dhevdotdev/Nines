#!/usr/bin/env bash
# Boot the nines-verify emulator if no device is attached, build the debug APK, install it
# on a clean data dir, and start it. Prints the serial it will drive.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"
started_emulator=0
if ! $ADB devices | awk 'NR>1 && $2=="device"' | grep -q .; then
  "$ANDROID_HOME/emulator/emulator" -avd nines-verify -no-window -no-audio -no-boot-anim -no-snapshot \
    -gpu swiftshader_indirect >"${TMPDIR:-/tmp}/nines-emulator.log" 2>&1 &
  started_emulator=1
  $ADB wait-for-device
  until [ "$($ADB shell getprop sys.boot_completed | tr -d '\r')" = 1 ]; do sleep 2; done
fi
./gradlew :app:assembleDebug -q
$ADB install -r app/build/outputs/apk/debug/app-debug.apk >/dev/null
$ADB shell pm clear dev.dhev.nines >/dev/null
$ADB shell am start -W -n dev.dhev.nines/.MainActivity >/dev/null
here="$(dirname "$0")"
for _ in $(seq 1 30); do ADB="$ADB" python3 "$here/ui.py" dump 2>/dev/null | grep -q "Skip" && break; sleep 2; done
echo "ready: $($ADB devices | awk 'NR>1 && $2=="device"{print $1; exit}') (emulator started by this run: $started_emulator)"
