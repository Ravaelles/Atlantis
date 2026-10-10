#!/usr/bin/env bash
# One command: attach the Atlantis bot to real StarCraft under Wine and play.
#
# This is the F5-from-IDE equivalent for Linux. It was verified end to end on
# 2026-10-06 (client log reached HELLO_ATLANTIS - BWAPI attached, Atlantis is
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
# Seeing the bot's output in the IDE console:
#   The bot is a SEPARATE process (a Windows JVM under Wine), so its
#   System.out does not reach the IDE by itself - on Windows it did, because
#   the bot was the IDE process. This script streams the bot's output to its own
#   stdout, which the IDE shows live (UnixChaosGameLauncher uses inheritIO()), so
#   A.println / System.out.println / System.err.println appear in the IntelliJ
#   console AND in out/wine/client.log.
#
#   LIVE_OUTPUT=0   quiet console, log file only
#   BUILD=0         play the jar already on disk instead of rebuilding it
#   JAR_OUT=...     where the jar is built and loaded from
#
# Exit code 0 = HELLO_ATLANTIS seen (bot attached). Non-zero otherwise; on
# failure everything is killed so no stray process survives.
set -u

# =============================================================================
# CONFIGURATION - edit here, nothing below needs touching
# =============================================================================

# Where the built bot jar goes, and where the game loads it from.
#
# Point this at the bot folder the game uses. Two common values:
#   $HOME/.scbw/bots/AtlantisP/AI/Atlantis.jar   the scbw container's bot slot
#                                                (what IntelliJ's artifact used
#                                                to write)
#   /sc-ai/Atlantis/bots/AtlantisP/AI/Atlantis.jar   the in-repo bot folder
#
# This script BUILDS the jar at this path from src/ before every game, so the
# jar can never be older than the sources. That is why the IntelliJ artifact
# should be removed (Build > build-on-make = OFF for it): two builders writing
# two different jars is how the owner spent a session testing code from hours
# before - the IDE wrote one path, the game loaded another, and none of the new
# prints appeared. One builder, one path, one jar.
JAR_OUT="${JAR_OUT:-$HOME/.scbw/bots/AtlantisP/AI/Atlantis.jar}"

# Rebuild the jar before playing. BUILD=0 uses whatever is already there
# (useful to reproduce a report against a known build).
BUILD="${BUILD:-1}"

# The Wine setup.
WINE_ROOT="${WINEPREFIX:-$HOME/.wine}"
GAME_ROOT="$WINE_ROOT/drive_c/sc"
JAVA_EXE="$WINE_ROOT/drive_c/Java/bin/java.exe"

# This repository.
ATLANTIS_DIR="${ATLANTIS_DIR:-/sc-ai/Atlantis}"
LOG_DIR="$ATLANTIS_DIR/out/wine"

# =============================================================================

MAP="${1:-1a2a 3a Micro 2.scx}"
MAP="${MAP//1a2a 3a/1a2a3a}"   # tolerate the extra space from tab completion

JAR="$JAR_OUT"
mkdir -p "$(dirname "$JAR")" "$LOG_DIR"
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

# The jar is what actually plays, and nothing rebuilt it: the IDE launches this
# script, the script starts the jar, so a stale jar means the owner tests code
# that is hours old while the sources say otherwise (measured 2026-10-07: the
# jar was from 17:56 and the sources from 20:25, so none of the prints added in
# between appeared and the whole session looked like "the commander never runs").
# Rebuild it from source here, every time.
#
# BUILD=0 (env) skips it for a deliberately frozen jar, e.g. to reproduce a
# report against a known build.
if [ "${BUILD:-1}" = "1" ]; then
  say "Building the bot jar from source in the BACKGROUND (BUILD=0 to skip)"
  rm -f /tmp/atlantis-build-jar.done /tmp/atlantis-build-jar.failed
  # Started now, in parallel with the game launch: StarCraft takes seconds to
  # come up anyway, so the compile hides inside that instead of adding to it.
  # The bot JVM waits for the build to finish before it starts (see Step 1).
  (
    if timeout 300 bash "$ATLANTIS_DIR/scripts/build-bot-jar.sh" "$JAR" >/tmp/atlantis-build-jar.log 2>&1; then
      : > /tmp/atlantis-build-jar.done
    else
      echo "failed" > /tmp/atlantis-build-jar.failed
    fi
  ) &
  JAR_BUILD_PID=$!
fi

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

say "Step 1/3: jar: $JAR"

# Wait for the background build before launching the client: the bot cannot
# start from a jar that is still being written. This is the only point that has
# to wait, and by now StarCraft has been starting in parallel for the same time.
if [ "${BUILD:-1}" = "1" ]; then
  waited=0
  while [ ! -f /tmp/atlantis-build-jar.done ] && [ ! -f /tmp/atlantis-build-jar.failed ]; do
    sleep 0.2
    waited=$((waited + 1))
    if [ "$waited" -gt 250 ]; then break; fi
  done

  if [ -f /tmp/atlantis-build-jar.failed ]; then
    say "  FAILED to build the jar - see /tmp/atlantis-build-jar.log"
    tail -20 /tmp/atlantis-build-jar.log | sed 's/^/    /'
    exit 1
  fi
  since "jar rebuilt (parallel with game start)"
fi

[ -f "$JAR" ] || { say "Bot jar missing at $JAR"; exit 1; }

# The jar path must reach the Wine JVM as a WINDOWS path. Deriving it from
# $JAR instead of hardcoding it is the whole point: a hardcoded
# Z:\sc-ai\...\bots\AtlantisP\AI\Atlantis.jar here is how the game ended up
# loading a DIFFERENT jar than the one being built, so changes never appeared
# (measured 2026-10-07 - the owner tested hours-old code and the prints added in
# between never showed up).
WIN_JAR="$(winepath -w "$JAR" 2>/dev/null || true)"
if [ -z "$WIN_JAR" ]; then
  # Fallback if winepath is unavailable: map /sc-ai -> Z:\sc-ai by hand, which
  # is what Wine's default Z: drive does for the filesystem root.
  WIN_JAR="Z:$(echo "$JAR" | tr '/' '\\')"
fi
say "  Windows path: $WIN_JAR"

# The bot is a SEPARATE process (a Windows JVM under Wine), so its stdout is
# not the IDE's console - on Windows it was, because the bot WAS the IDE
# process. Without help, every print the owner adds lands only in
# out/wine/client.log, 16k lines deep, and looks like "the code never runs"
# (measured 2026-10-07: the prints were all there, 13k of them).
#
# So the bot's output is TEE'd: the log file still gets everything (scripts and
# greps read it), and the same lines are streamed to this script's stdout,
# which the IDE shows live through ProcessBuilder.inheritIO().
#
# LIVE_OUTPUT=0 turns the streaming off for a quiet console (the log is still
# written). Filtering is deliberately left to the reader: filtering here would
# silently hide the line someone is looking for, which is the exact failure this
# fixes.
LIVE_OUTPUT="${LIVE_OUTPUT:-1}"

if [ "$LIVE_OUTPUT" = "1" ]; then
  setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine "$JAVA_EXE" "-Dos.name=Windows 10" -jar "$WIN_JAR" \
    2>&1 < /dev/null | tee "$CLIENT_LOG" &
  # tee is the foreground of that pipeline; killing it later also closes the
  # bot's stdout. CLIENT_PID stays the pipeline's PID, which is what the
  # liveness checks below need.
  CLIENT_PID=$!
else
  setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine "$JAVA_EXE" "-Dos.name=Windows 10" -jar "$WIN_JAR" \
    > "$CLIENT_LOG" 2>&1 < /dev/null &
  CLIENT_PID=$!
fi
say "  Client PID $CLIENT_PID, log: $CLIENT_LOG"
if [ "$LIVE_OUTPUT" = "1" ]; then
  say "  Bot output is streamed below (LIVE_OUTPUT=0 to silence, log file always written)"
fi

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

say "Step 3/3: watching for HELLO_ATLANTIS (grace 10s)..."
END=$(( $(date +%s) + 10 ))
ATTACHED=0
while [ "$(date +%s)" -lt "$END" ]; do
  if grep -q "HELLO_ATLANTIS" "$CLIENT_LOG" 2>/dev/null; then
    say "SUCCESS: BWAPI attached, Atlantis is playing!"
    #since "client attached (HELLO_ATLANTIS)"
    ATTACHED=1
    break
  fi
  if ! kill -0 "$CLIENT_PID" 2>/dev/null; then
    say "FAILED: client JVM died. Log tail:"
    tail -15 "$CLIENT_LOG" | sed 's/^/    /'
    cleanup
    exit 1
  fi
  sleep 1
done

if [ "$ATTACHED" -eq 0 ]; then
  say "FAILED: no HELLO_ATLANTIS within the grace period. Client log tail:"
  tail -15 "$CLIENT_LOG" | sed 's/^/    /'
  cleanup
  exit 1
fi

# Keep watching while the bot plays. When the game ends - win or lose - the
# BWAPI connection drops and the client JVM exits; that is the signal to tear
# the whole session down. Without this watcher the result screen sat there
# forever with StarCraft and ChaosLauncher still running (owner report,
# 2026-10-06).
#say "OK - The bot is playing..."
say "###############################################"
while kill -0 "$CLIENT_PID" 2>/dev/null; do
  sleep 2
done
say "Client JVM exit, killing SC & ChaosLauncher."
cleanup
exit 0
