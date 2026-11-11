#!/usr/bin/env python3
"""Seed the connected device's Nines database with six habits and about 60 days of history.

Run after launch.sh (the app must have created its database). Used for screenshots and for
driving past dates and numeric habits. Replaces whatever habits are on the device.

  seed.py
"""
import os, random, sqlite3, subprocess, tempfile, time

ADB = os.environ.get("ADB") or os.path.expanduser("~/Library/Android/sdk/platform-tools/adb")
PKG = "dev.dhev.nines"
DAYS = 60

def adb(*a, **k):
    return subprocess.run([ADB, *a], check=True, **k)

def argb(v):
    return v - (1 << 32) if v >= 1 << 31 else v

adb("shell", "am", "force-stop", PKG)
tmp = tempfile.mkdtemp()
db_path = os.path.join(tmp, "nines.db")
for name in ("nines.db", "nines.db-wal", "nines.db-shm"):
    with open(os.path.join(tmp, name), "wb") as f:
        adb("exec-out", "run-as", PKG, "cat", f"databases/{name}", stdout=f)

today = int(adb("shell", "date", "+%s", capture_output=True, text=True).stdout) // 86400
now_ms = int(time.time() * 1000)
start = today - DAYS
con = sqlite3.connect(db_path)
con.execute("PRAGMA foreign_keys=ON")
for t in ("entries", "schedule_versions", "reminders", "habits"):
    con.execute(f"DELETE FROM {t}")

# name, type, unit, color, icon, background, schedule (num, den, value, kind), daily completion chance
habits = [
    ("Meditate", "BOOLEAN", None, 0xFF00897B, "meditate", "waves", (1, 1, None, None), 0.88),
    ("Read", "NUMERICAL", "pages", 0xFFFFB300, "book", "dots", (1, 1, 20.0, "AT_LEAST"), 0.85),
    ("Run", "BOOLEAN", None, 0xFFF4511E, "run", "peaks", (3, 7, None, None), 0.45),
    ("Drink water", "NUMERICAL", "glasses", 0xFF00ACC1, "water", "rings", (1, 1, 8.0, "AT_LEAST"), 0.8),
    ("Lights out by 11", "BOOLEAN", None, 0xFF5E35B1, "sleep", "stars", (1, 1, None, None), 0.7),
    ("Practice piano", "BOOLEAN", None, 0xFFE53935, "piano", "sparkle", (4, 7, None, None), 0.55),
]
rng = random.Random(9)
for pos, (name, typ, unit, color, icon, bg, sched, chance) in enumerate(habits):
    created = start * 86400 * 1000
    cur = con.execute(
        "INSERT INTO habits (uuid,name,type,unit,color,iconKey,backgroundStyleKey,position,archivedAt,createdAt)"
        " VALUES (?,?,?,?,?,?,?,?,NULL,?)",
        (f"seed-{pos}", name, typ, unit, argb(color), icon, bg, pos, created),
    )
    hid = cur.lastrowid
    num, den, value, kind = sched
    con.execute(
        "INSERT INTO schedule_versions (habitId,effectiveFromDate,targetNumerator,targetDenominator,targetValue,targetKind)"
        " VALUES (?,?,?,?,?,?)",
        (hid, start, num, den, value, kind),
    )
    for d in range(start, today):  # leave today open so the list shows what is left to do
        if rng.random() > chance:
            continue
        v = None
        if typ == "NUMERICAL":
            v = round(value * rng.uniform(1.0, 1.4)) if rng.random() < 0.8 else round(value * rng.uniform(0.4, 0.9))
        con.execute(
            "INSERT INTO entries (habitId,date,state,numericValue,notes,tzOffsetMinutes,createdAt,updatedAt)"
            " VALUES (?,?,?,?,NULL,330,?,?)",
            (hid, d, "COMPLETED", v, now_ms, now_ms),
        )
    # today: Meditate done, Read in progress, the rest untouched
    if name == "Meditate":
        con.execute("INSERT INTO entries VALUES (?,?,?,?,NULL,330,?,?)", (hid, today, "COMPLETED", None, now_ms, now_ms))
    if name == "Read":
        con.execute("INSERT INTO entries VALUES (?,?,?,?,NULL,330,?,?)", (hid, today, "IN_PROGRESS", 12.0, now_ms, now_ms))
con.commit()
con.execute("PRAGMA wal_checkpoint(TRUNCATE)")
con.close()

adb("push", db_path, "/data/local/tmp/nines-seed.db")
adb("shell", "run-as", PKG, "rm", "-f", "databases/nines.db-wal", "databases/nines.db-shm")
adb("shell", f"run-as {PKG} cp /data/local/tmp/nines-seed.db databases/nines.db")
adb("shell", "rm", "/data/local/tmp/nines-seed.db")
adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
print(f"seeded {len(habits)} habits over {DAYS} days")
