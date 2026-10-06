#!/usr/bin/env bash
# One command: attach the Atlantis bot to real StarCraft under Wine and play.
#
# This is the F5-from-IDE equivalent for Linux. It was verified end to end on
# 2026-10-06 (client log reached HELLO_WORLD - BWAPI attached, Atlantis is
# playing) and encodes three facts that cost the whole session to find:
#
#   1. The bot JVM must run UNDER WINE with a WINDOWS JRE and
#      -Dos.name=Windows 10, so JBWAPI picks its ClientConnectionW32 backend
#      (Kernel32 OpenFileMapping through wineserver). Any other JVM/flag
#      combination selects the POSIX backend, which searches /dev/shm - a
#      namespace the Wine-side BWAPI server never populates - and loops on
#      "Game table mapping not found" forever.
#   2. The CLIENT starts BEFORE the game. The BWAPI server registers a client
#      slot while the game starts; a client started after the match is
#      running attaches to a match that is already over
#      (Connection successful -> failed, disconnecting).
#   3. The game must run with W-MODE doublesize for a usable window (see
#      _AI/LOCAL-STARCRAFT.md); run-wine-game.sh already sets that up.
#
# Usage:
#   scripts/run-wine-full.sh [map-name]        # default: 1a2a3a Micro 2.scx
#
# Exit code 0 = HELLO_WORLD seen (bot attached). Non-zero otherwise; on
# failure everything is killed so no stray process survives.
set -u

MAP="${1:-1a2a 3a Micro 2.scx}"
MAP="${MAP//1a2a 3a/1a2a3a}"   # tolerate the extra space from tab completion

WINE_ROOT="${WINEPREFIX:-$HOME/.wine}"
GAME_ROOT="$WINE_ROOT/drive_c/sc"
JAVA_EXE="$WINE_ROOT/drive_c/Java/bin/java.exe"
JAR="/sc-ai/Atlantis/bots/AtlantisP/AI/Atlantis.jar"
LOG_DIR="/sc-ai/Atlantis/out/wine"
mkdir -p "$LOG_DIR"
CLIENT_LOG="$LOG_DIR/client.log"

say() { echo "[wine-full] $*"; }

cleanup() {
  say "Cleaning up"
  pkill -9 -x StarCraft.exe 2>/dev/null
  pkill -9 -x Chaoslauncher.exe 2>/dev/null
  pkill -9 -f java.exe 2>/dev/null
  wineserver -k 2>/dev/null
}

say "Killing leftovers"
pkill -9 -x StarCraft.exe 2>/dev/null
pkill -9 -x Chaoslauncher.exe 2>/dev/null
pkill -9 -f java.exe 2>/dev/null
wineserver -k 2>/dev/null
sleep 3

if [ ! -f "$JAVA_EXE" ]; then
  say "Windows Java not found at $JAVA_EXE - install Temurin 8 under Wine once"
  exit 1
fi
if [ ! -f "$JAR" ]; then
  say "Bot jar not found at $JAR - build it with scripts/build-bot-jar.sh"
  exit 1
fi

say "Step 1/3: jar: $JAR)"
setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
  wine "$JAVA_EXE" "-Dos.name=Windows 10" -jar "Z:\\sc-ai\\Atlantis\\bots\\AtlantisP\\AI\\Atlantis.jar" \
  > "$CLIENT_LOG" 2>&1 < /dev/null &
CLIENT_PID=$!
say "  Client PID $CLIENT_PID, log: $CLIENT_LOG"

# The client-first order needs no wait: the client only reaches the BWAPI game
# table once the GAME is up (before that it loops on "Game table mapping not
# found", which is its normal idle state). Just make sure it is alive.
sleep 3
if kill -0 "$CLIENT_PID" 2>/dev/null; then
  say "  Client is running (it will reach the table once the game starts)"
else
  say "  FAILED: client JVM died immediately. Log tail:"
  tail -15 "$CLIENT_LOG" | sed 's/^/    /'
  exit 1
fi

say "Step 2/3: GAME (map: $MAP)"

# The map the launcher was given (from Main.defineMapToUse or --map=) is what
# StarCraft must load; it lives in bwapi.ini, read once at game start. The
# Wine-side bwapi.ini is the live file (the /sc-ai/BWAPI one is only the dist
# template), and the client JVM patched nothing, so this script owns the map
# line. Resolve the exact path first - BWAPI needs it, a bare name that lives
# in a subfolder is not loadable (measured 2026-10-06).
INI="$GAME_ROOT/bwapi-data/bwapi.ini"
# -L: maps/ is a symlink to the repo's map tree - without it find never
# descends into it and the resolution silently fails (measured
# 2026-10-06: the launcher passed 4Drag_v_4Drag.scm, the game kept
# loading the previous map).
RESOLVED_MAP=$(cd "$GAME_ROOT" && find -L maps -type f -iname "$(basename "$MAP")" 2>/dev/null | head -1)
if [ -n "$RESOLVED_MAP" ]; then
  say "  Setting map in bwapi.ini to $RESOLVED_MAP"
  # Match both spellings: "map = X" (script-generated ini) and "map=X"
# (written by AtlantisIgniter, which normalizes the separator). Measured
# 2026-10-06: the sed silently missed the "map=X" form and the game kept
# loading the previous map regardless of what the launcher passed.
sed -i -E "s|^map *=.*|map=$RESOLVED_MAP|" "$INI"
else
  say "  WARNING: map '$MAP' not found under $GAME_ROOT/maps - bwapi.ini left unchanged"
fi
cd "$GAME_ROOT"
setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
  wine chaoslauncher/Chaoslauncher.exe \
  > "$LOG_DIR/chaoslauncher.log" 2>&1 < /dev/null &
sleep 40
pgrep -x StarCraft.exe >/dev/null && say "  StarCraft is running" || say "  WARNING: StarCraft not running (see $LOG_DIR/chaoslauncher.log)"

say "Step 3/3: watching for HELLO_WORLD (grace 120s)..."
END=$(( $(date +%s) + 120 ))
while [ "$(date +%s)" -lt "$END" ]; do
  if grep -q "HELLO_WORLD" "$CLIENT_LOG" 2>/dev/null; then
    say "SUCCESS: HELLO_WORLD - BWAPI attached, Atlantis is playing!"
    say "Game stays up. Stop it with: pkill -x StarCraft.exe; pkill -x Chaoslauncher.exe; wineserver -k"
    # Leave the game running; only this supervisor exits.
    trap - EXIT
    exit 0
  fi
  if ! kill -0 "$CLIENT_PID" 2>/dev/null; then
    say "FAILED: client JVM died. Log tail:"
    tail -15 "$CLIENT_LOG" | sed 's/^/    /'
    cleanup
    exit 1
  fi
  sleep 2
done

say "FAILED: no HELLO_WORLD within the grace period. Client log tail:"
tail -15 "$CLIENT_LOG" | sed 's/^/    /'
cleanup
exit 1
