#!/usr/bin/env bash
# Read-only: is there a device, a usable JDK, and a Nines build installed that matches HEAD's APK?
set -u
cd "$(git rev-parse --show-toplevel)"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"
ok(){ echo "ok   $1"; }; bad(){ echo "FAIL $1"; fail=1; }; fail=0
[ -x "$ADB" ] && ok "adb present" || bad "adb missing under $ANDROID_HOME"
$ADB devices | awk 'NR>1 && $2=="device"' | grep -q . && ok "device attached" || bad "no device: run scripts/launch.sh"
java -XshowSettings:properties -version 2>&1 | grep -q "java.vendor.*GraalVM" && bad "java is GraalVM: its jlink breaks the Android build; use a full JDK 25" || ok "java is not GraalVM"
$ADB shell pm list packages dev.dhev.nines 2>/dev/null | grep -q dev.dhev.nines && ok "dev.dhev.nines installed" || bad "app not installed"
exit $fail
