#!/bin/bash
# -------------------------------------------------------
# Copyright (c) [2026] MantaBots
# All rights reserved
# -------------------------------------------------------
# Merge config files and push to the Control Hub
# Usage: ./conf.sh <n> <path>
# -------------------------------------------------------

set -e

# ── Arguments ──────────────────────────────────────────
NAME=$1
PATH_DIR=$2

if [ -z "$NAME" ] || [ -z "$PATH_DIR" ]; then
    echo "Usage: $0 <n> <path>"
    echo "  name : configuration name (e.g. mantabots)"
    echo "  path : directory containing the config files"
    exit 1
fi

# ── Check ADB connection ───────────────────────────────
echo "🔌 Connecting to Control Hub..."
adb connect 192.168.43.1:5555 > /dev/null 2>&1

if ! adb devices | grep -q "192.168.43.1:5555"; then
    echo "❌ Control Hub not reachable at 192.168.43.1:5555"
    exit 1
fi

# ── Find all <n>.*.json files ───────────────────────────
FILES=$(find "$PATH_DIR" -maxdepth 1 -name "${NAME}.*.json" | sort)

if [ -z "$FILES" ]; then
    echo "⚠️  No files matching ${NAME}.*.json found in $PATH_DIR"
    exit 0
fi

echo "📂 Found files:"
for F in $FILES; do echo "   $F"; done

# ── Merge into a single JSON ───────────────────────────
MERGED=$(python3 - "$NAME" "$PATH_DIR" $FILES <<'PYEOF'
import json, os, sys

name     = sys.argv[1]
path_dir = sys.argv[2]
files    = sys.argv[3:]
merged   = {}

def deep_set(d, keys, value):
    for key in keys[:-1]:
        d = d.setdefault(key, {})
    d[keys[-1]] = value

for filepath in files:
    basename = os.path.basename(filepath)
    # Remove <n>. prefix and .json suffix → get key path
    key_part = basename[len(name)+1:-5]   # e.g. "robot.arm"
    keys = key_part.split(".")             # e.g. ["robot", "arm"]
    try:
        with open(filepath) as f:
            content = json.load(f)
        deep_set(merged, keys, content)
        print(f"   ✅ Merged: {basename} → {'.'.join(keys)}", file=sys.stderr)
    except Exception as e:
        print(f"   ⚠️  Skipping {basename}: {e}", file=sys.stderr)

print(json.dumps(merged, indent=2))
PYEOF
)

# ── Write temp file and push ───────────────────────────
TMP_FILE="/tmp/${NAME}.json"
echo "$MERGED" > "$TMP_FILE"

echo "📤 Pushing ${NAME}.json to Control Hub..."
adb push "$TMP_FILE" "/sdcard/FIRST/${NAME}.json"
rm "$TMP_FILE"

# ── Push hardware XML if present ───────────────────────
HW_FILE="$PATH_DIR/$NAME.xml"
if [ -f "$HW_FILE" ]; then
    adb push "$HW_FILE" "/sdcard/FIRST/$NAME.xml"
    echo "✅ Pushed: $HW_FILE → /sdcard/FIRST/$NAME.xml"
else
    echo "⚠️  No hardware file found: $HW_FILE"
fi

echo "🏁 Done — configuration '$NAME' deployed to /sdcard/FIRST/"
