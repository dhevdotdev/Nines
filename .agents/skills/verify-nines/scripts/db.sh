#!/usr/bin/env bash
# Copy the app's Room database off the device and run a sqlite3 query against it.
#   db.sh "select * from entries;"
set -euo pipefail
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"
d=$(mktemp -d)
for f in nines.db nines.db-wal nines.db-shm; do $ADB exec-out run-as dev.dhev.nines cat "databases/$f" >"$d/$f"; done
sqlite3 "$d/nines.db" "$1"
rm -rf "$d"
