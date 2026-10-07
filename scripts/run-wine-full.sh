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
ATLANTIS_DIR="/sc-ai/Atlantis"
JAR="$ATLANTIS_DIR/bots/AtlantisP/AI/Atlantis.jar"
LOG_DIR="$ATLANTIS_DIR/out/wine"
mkdir -p "$LOG_DIR"
CLIENT_LOG="$LOG_DIR/client.log"

say() { echo "[wine-full] $*"; }

# Wall-clock markers. The owner asked why a start takes so long, and guessing is
# what made this slow before: print one line per phase so the next run shows
# where the time actually goes (setup, SC up, client attached).
T0=$(date +%s)
since() { echo "[wine-full]   +$(( $(date +%s) - T0 ))s  $1"; }

cleanup() {
  say "Cleaning up"
  pkill -9 -x StarCraft.exe 2>/dev/null
  pkill -9 -x Chaoslauncher.exe 2>/dev/null
  pkill -9 -f java.exe 2>/dev/null
  wineserver -k 2>/dev/null
}

say "Killing leftovers"
# Only what must die before a game can start, and only by exact name (-x): a
# -f pattern also matches this script's own command line.
#
# What is deliberately NOT killed:
#   - java.exe: the CLIENT JVM is started a few lines below, and killing it here
#     only makes this script wait for a fresh JVM under Wine (seconds).
#   - wineserver: killing it makes every subsequent `wine` call pay for a cold
#     server start, which is the single biggest fixed cost of a run.
# The one thing that MUST go is a leftover StarCraft/ChaosLauncher, because a
# second instance breaks the BWAPI injection.
pkill -9 -x StarCraft.exe 2>/dev/null
pkill -9 -x Chaoslauncher.exe 2>/dev/null
pkill -9 -x ChaosLauncher.exe 2>/dev/null

# A leftover BWAPILauncher holds the game table for the OpenBW path; harmless
# here but cheap to check, and it is never wanted during a Wine game.
pkill -9 -x BWAPILauncher 2>/dev/null

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
# found", which is its normal idle state). Just make sure it is alive. Poll
# fast instead of sleeping 3 s: a JVM under Wine is up in well under a second,
# and if it died we want to know at once (measured 2026-10-06).
CLIENT_ALIVE=0
for _ in $(seq 1 15); do
  if kill -0 "$CLIENT_PID" 2>/dev/null; then CLIENT_ALIVE=1; break; fi
  sleep 0.2
done
if [ "$CLIENT_ALIVE" -eq 1 ]; then
  say "  Client is running (it will reach the table once the game starts)"
else
  say "  FAILED: client JVM died immediately. Log tail:"
  tail -15 "$CLIENT_LOG" | sed 's/^/    /'
  exit 1
fi

say "Step 2/3: GAME (map: $MAP)"
since "client started, launching the game"

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
  # The game type follows the map FAMILY, and getting it wrong stops the game at
  # the map-selection screen (owner report, 2026-10-07):
  #   sscai/...  -> MELEE               a real melee map; USE_MAP_SETTINGS makes
  #                                      StarCraft wait for the player instead
  #                                      of starting
  #   ums/...    -> USE_MAP_SETTINGS    a UMS scenario carries its own rules
  # The resolved path already contains the family, so key off it rather than off
  # the name the caller passed.
  case "$RESOLVED_MAP" in
    *sscai*) GAME_TYPE="MELEE" ;;
    *)       GAME_TYPE="USE_MAP_SETTINGS" ;;
  esac
  if grep -qE "^game_type *=" "$INI"; then
    sed -i -E "s|^game_type *=.*|game_type=$GAME_TYPE|" "$INI"
  else
    sed -i -E "s|^(map=.*)$|\1\ngame_type=$GAME_TYPE|" "$INI"
  fi
  say "  Setting game_type to $GAME_TYPE (map family)"
else
  say "  WARNING: map '$MAP' not found under $GAME_ROOT/maps - bwapi.ini left unchanged"
fi
# Our race is owned by the CLIENT (Main.ourRace()) but is READ by StarCraft
# from bwapi.ini at game start, and this script is the only thing that rewrites
# the live ini. Leaving it alone is why changing Main.ourRace() to Protoss still
# produced a Terran game (owner report, 2026-10-07): the ini said race=Terran
# and nothing ever changed it.
#
# The race is read straight from Main.java's ourRace() rather than duplicated
# here, so the two cannot drift. That method commits its answer as
#     if (true) return "Protoss";
# so the FIRST returned literal after the method header is the active one.
CLIENT_RACE=""
if [ -f "$ATLANTIS_DIR/src/main/Main.java" ]; then
  # The commented-out alternatives above the active one are the commit-a-race
  # idiom, so a commented `return` must be skipped - matching it picked the
  # WRONG race ("Terran" while the active line said "Protoss", measured).
  CLIENT_RACE=$(sed -n '/public static String ourRace/,/^    }/p' "$ATLANTIS_DIR/src/main/Main.java" \
    | grep -vE '^\s*//' \
    | grep -oE 'return "(Protoss|Terran|Zerg)"' | head -1 | sed -E 's/.*"(.*)".*/\1/')
fi

if [ -z "$CLIENT_RACE" ]; then
  say "  WARNING: could not read the client's race from Main.java; race= left unchanged"
else
  if grep -qE "^race *=" "$INI"; then
    sed -i -E "s|^race *=.*|race=$CLIENT_RACE|" "$INI"
  else
    sed -i -E "s|^(enemy_race=.*)$|\1\nrace=$CLIENT_RACE|" "$INI"
  fi
  say "  Setting race to $CLIENT_RACE (from the client's Main.ourRace())"
fi

cd "$GAME_ROOT"
setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
  wine chaoslauncher/Chaoslauncher.exe \
  > "$LOG_DIR/chaoslauncher.log" 2>&1 < /dev/null &

# Poll for StarCraft instead of a fixed 40 s sleep: the game usually comes up
# within seconds of ChaosLauncher starting, and the old constant wait made
# every launch pay the full 40 s no matter how fast the game was (measured
# 2026-10-06). 45 s cap is only the failure path.
SC_UP=0
for _ in $(seq 1 90); do
  if pgrep -x StarCraft.exe >/dev/null; then SC_UP=1; break; fi
  sleep 0.5
done
if [ "$SC_UP" -eq 1 ]; then
  say "  StarCraft is running"
  since "StarCraft is up"
else
  say "  WARNING: StarCraft not running within 45 s (see $LOG_DIR/chaoslauncher.log)"
fi

say "Step 3/3: watching for HELLO_WORLD (grace 120s)..."
END=$(( $(date +%s) + 120 ))
ATTACHED=0
while [ "$(date +%s)" -lt "$END" ]; do
  if grep -q "HELLO_WORLD" "$CLIENT_LOG" 2>/dev/null; then
    say "SUCCESS: HELLO_WORLD - BWAPI attached, Atlantis is playing!"
    since "client attached (HELLO_WORLD)"
    ATTACHED=1
    break
  fi
  if ! kill -0 "$CLIENT_PID" 2>/dev/null; then
    say "FAILED: client JVM died. Log tail:"
    tail -15 "$CLIENT_LOG" | sed 's/^/    /'
    cleanup
    exit 1
  fi
  sleep 2
done

if [ "$ATTACHED" -eq 0 ]; then
  say "FAILED: no HELLO_WORLD within the grace period. Client log tail:"
  tail -15 "$CLIENT_LOG" | sed 's/^/    /'
  cleanup
  exit 1
fi

# Keep watching while the bot plays. When the game ends - win or lose - the
# BWAPI connection drops and the client JVM exits; that is the signal to tear
# the whole session down. Without this watcher the result screen sat there
# forever with StarCraft and ChaosLauncher still running (owner report,
# 2026-10-06).
say "Bot is playing - watching until the game ends..."
while kill -0 "$CLIENT_PID" 2>/dev/null; do
  sleep 2
done
say "Client JVM exit - game ended - killing SC & ChaosLauncher."
cleanup
exit 0
