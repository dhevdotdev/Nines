#!/usr/bin/env python3
"""Inspect and drive the Nines UI on the connected device through uiautomator.

  ui.py dump                 list visible nodes: text | content-desc | clickable | bounds
  ui.py tap "<text-or-desc>" tap the first node whose text or content-desc equals the argument
  ui.py type "<text>"        type into the focused field
"""
import re, subprocess, sys, xml.etree.ElementTree as ET

ADB = [__import__("os").environ.get("ADB") or __import__("os").path.expanduser("~/Library/Android/sdk/platform-tools/adb")]

def adb(*a, out=False):
    r = subprocess.run(ADB + list(a), capture_output=True, text=True, check=True)
    return r.stdout if out else None

def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    root = ET.fromstring(adb("exec-out", "cat", "/sdcard/ui.xml", out=True))
    for n in root.iter("node"):
        t, d = n.get("text", ""), n.get("content-desc", "")
        if t or d:
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
            yield t, d, n.get("clickable"), (x1 + x2) // 2, (y1 + y2) // 2, n.get("bounds")

cmd = sys.argv[1] if len(sys.argv) > 1 else "dump"
if cmd == "dump":
    for t, d, c, _, _, b in nodes():
        print(f"{t!r} | {d!r} | clickable={c} | {b}")
elif cmd == "tap":
    for t, d, _, x, y, _ in nodes():
        if sys.argv[2] in (t, d):
            adb("shell", "input", "tap", str(x), str(y)); break
    else:
        sys.exit(f"no node with text or content-desc {sys.argv[2]!r}")
elif cmd == "type":
    adb("shell", "input", "text", sys.argv[2].replace(" ", "%s"))
else:
    sys.exit(__doc__)
