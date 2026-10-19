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
#   console AND in out/wine/WINE_LOG.
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

MAP=""
ENV_FILE_ARG=""
for arg in "$@"; do
  case "$arg" in
    --env=*) ENV_FILE_ARG="${arg#--env=}" ;;
    *) [ -z "$MAP" ] && MAP="$arg" || { echo "[wine-full] ignoring extra arg: $arg" >&2; } ;;
  esac
done
[ -z "$MAP" ] && MAP="1a2a 3a Micro 2.scx"
MAP="${MAP//1a2a 3a/1a2a3a}"   # tolerate the extra space from tab completion
# Explicit per-run env file (owner: scripts never edit shared ENV files).
# Relative paths resolve against the repo root, so --env=ENV-WINE-FAST means
# bwapi-data/AI/ENV-WINE-FAST.
if [ -n "$ENV_FILE_ARG" ]; then
  case "$ENV_FILE_ARG" in
    /*) ENV_FILE_ABS="$ENV_FILE_ARG" ;;
    *) ENV_FILE_ABS="$ATLANTIS_DIR/$ENV_FILE_ARG" ;;
  esac
  [ -f "$ENV_FILE_ABS" ] || { echo "[wine-full] no such env file: $ENV_FILE_ABS" >&2; exit 1; }
  CLIENT_ENV_ARG="--env=$ENV_FILE_ABS"
  say "  env file: $ENV_FILE_ABS (the bot reads this INSTEAD of the shared ENV)"
else
  CLIENT_ENV_ARG=""
fi

JAR="$JAR_OUT"
mkdir -p "$(dirname "$JAR")" "$LOG_DIR"
CLIENT_LOG="$LOG_DIR/WINE_LOG"

say() { echo "[wine-full] $*"; }

# Wall-clock markers. The owner asked why a start takes so long, and guessing is
# what made this slow before: print one line per phase so the next run shows
# where the time actually goes (setup, SC up, client attached).
T0=$(date +%s)
since() { echo "[wine-full]   +$(( $(date +%s) - T0 ))s  $1"; }

cleanup() {
  # No "Cleaning up" echo and no bash "Killed <job>" notice (owner:
  # local games stay quiet): disown first so the shell forgets the
  # background jobs, then kill them by name. The kills stay - without
  # them a dead game leaves Wine processes behind.
  disown -a 2>/dev/null
  pkill -9 -x StarCraft.exe 2>/dev/null
  pkill -9 -f "[C]haoslauncher.exe" 2>/dev/null
  pkill -9 -f java.exe 2>/dev/null
  wineserver -k 2>/dev/null
}

# Belt and braces: the paths below call cleanup() explicitly, but a signal
# or an early exit must not leave test flags in the shared ENV file either.
# cleanup() is idempotent (pkill no-ops, the backup is consumed on restore).
trap cleanup EXIT

# Where else the client log goes, live. WINE_LOG_COPY in the bot's ENV file
# (machine-local, git-ignored): when non-empty AND the same file says
# GAME_LAUNCHER=WINE, tee writes every bot line there as it arrives - so the
# copy exists mid-game, after a crash and after an IDE kill, not only on a
# clean exit (an end-of-run cp was tried first and missed all three - the
# owner's IDE run left no file behind it). Prints the path or nothing.
# Wine-script only (local, non-OpenBW by construction) plus the launcher
# guard, so an OpenBW ENV can never trigger it.
resolve_wine_log_copy() {
  local env_file=""
  local candidate
  # Same order the bot itself resolves ENV in (Env.java): the first file that
  # exists is the one the game played with, so its GAME_LAUNCHER and
  # WINE_LOG_COPY are authoritative.
  for candidate in "bwapi-data/AI/ENV" "bots/AtlantisP/AI/ENV" "ENV"; do
    if [ -f "$candidate" ]; then env_file="$candidate"; break; fi
  done
  [ -z "$env_file" ] && return 0
  grep -q "^GAME_LAUNCHER=WINE" "$env_file" || return 0

  local dest
  dest=$(grep "^WINE_LOG_COPY=" "$env_file" | tail -1 | cut -d= -f2-)
  [ -z "$dest" ] && return 0
  mkdir -p "$(dirname "$dest")" 2>/dev/null
  printf '%s' "$dest"
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
# Never -x on Chaoslauncher.exe: the 17-char name exceeds the 15-char
# comm limit, so -x never matches and a leftover launcher makes every
# new game silently attach to the old map instead (measured:
# an 8v8 request played the previous 1v1). The bracket keeps pgrep
# from matching this shell's own command line (GameExecution.md #2).
pkill -9 -f "[C]haoslauncher.exe" 2>/dev/null
sleep 1
if pgrep -x StarCraft.exe >/dev/null 2>&1 || pgrep -f "[C]haoslauncher.exe" >/dev/null 2>&1; then
  say "FATAL: a leftover game survived the kill - refusing to attach to a stale map."
  exit 1
fi

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

# Speed and horizons come ONLY from an explicit --env= file (or the
# shared ENV the bot reads by default). This script never writes test
# flags into any ENV file (owner: per-run flags must not leak into
# watched IDE games) - pass --env=ENV-WINE-FAST for unattended speed.


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
# out/wine/WINE_LOG, 16k lines deep, and looks like "the code never runs"
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

# Resolve the live copy destination once, before the client starts (see
# resolve_wine_log_copy): empty in every case that must stay quiet.
WINE_LOG_COPY_RESOLVED="$(resolve_wine_log_copy)"

# The client inherits this cwd, so the bot's cwd-relative files
# (bwapi-data/write/...) land here. Captured now: the script cd's into
# GAME_ROOT further down, so pwd at teardown would lie.
CLIENT_CWD="$(pwd)"

# Fresh diagnostics per game: the bot appends to these files and never
# truncates them, so without clearing every run collects all previous
# games (measured 2026-10-17: 7 MB EVENTS.jsonl spanning 6 games,
# per-run attribution impossible). Same fix as run-openbw-e2e.sh.
rm -f "$CLIENT_CWD/bwapi-data/write/BUILDINGS.json" \
    "$CLIENT_CWD/bwapi-data/write/EVENTS.jsonl" \
    "$CLIENT_CWD/bwapi-data/write/DECISIONS.log"

TEE_OUT=("$CLIENT_LOG")
TEE_ERR=("$LOG_DIR/WINE_DEBUG")
WINE_LIVE_LOG=""
WINE_LIVE_DEBUG=""
WINE_LIVE_DECISIONS=""
if [ -n "$WINE_LOG_COPY_RESOLVED" ]; then
  # Owner 2026-10-18: fixed role names, no extensions. The ENV value
  # anchors the directory; basenames are fixed so all three streams
  # always land together (the old last-wine*.log pair is retired).
  WINE_LIVE_DIR="$(dirname "$WINE_LOG_COPY_RESOLVED")"
  WINE_LIVE_LOG="$WINE_LIVE_DIR/WINE_LOG"
  WINE_LIVE_DEBUG="$WINE_LIVE_DIR/WINE_DEBUG"
  WINE_LIVE_DECISIONS="$WINE_LIVE_DIR/WINE_DECISIONS"
  # Owner 2026-10-16: the main copy is the GAME log (stdout only), the
  # -DEBUG copy the diagnostics (stderr only). Merging both streams into
  # both files made last-wine.log identical to last-wine-DEBUG.log.
  TEE_OUT+=("$WINE_LIVE_LOG")
  say "  Live log copy: $WINE_LIVE_LOG (game log only)"
fi
if [ -n "$WINE_LIVE_DEBUG" ]; then
  TEE_ERR+=("$WINE_LIVE_DEBUG")
  say "  Live debug copy: $WINE_LIVE_DEBUG (diagnostics stream only)"
fi

# Streams stay split on purpose: WINE_LOG is the game log (HELLO,
# GameSummary, build orders), WINE_DEBUG the diagnostics (worker gates,
# stuck workers, refusals, stack traces). The console still shows both
# merged, exactly as before.
# The bot must analyse the map the game actually hosts: without --map it
# falls back to its hard-coded default while the game plays something else
# (measured: an 8v8 request played with 1Drag analysis). Same contract as
# the OpenBW path: --map= wins over Main's choices; bare names resolve
# under maps/ums and maps/sscai first.
CLIENT_MAP_ARG="--map=${RESOLVED_MAP:-$MAP}"
if [ "$LIVE_OUTPUT" = "1" ]; then
  setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine "$JAVA_EXE" "-Dos.name=Windows 10" -jar "$WIN_JAR" "$CLIENT_MAP_ARG" $CLIENT_ENV_ARG \
    > >(tee "${TEE_OUT[@]}") 2> >(tee "${TEE_ERR[@]}" >&2) < /dev/null &
  # CLIENT_PID is now wine itself, not tee (better liveness: it dies when
  # the game ends, while tee may linger flushing). Tees inherit the
  # redirection, so no output is lost when the client exits first.
  CLIENT_PID=$!
else
  setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine "$JAVA_EXE" "-Dos.name=Windows 10" -jar "$WIN_JAR" "$CLIENT_MAP_ARG" $CLIENT_ENV_ARG \
    > >(tee "${TEE_OUT[@]}" > /dev/null) 2> >(tee "${TEE_ERR[@]}" > /dev/null) < /dev/null &
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


# Mute the game (owner 2026-10-19): unattended runs need no sound, and a
# missing audio device must not become a hang. Disabling the backends is
# a run-level mute - StarCraft continues silently (sound options exist
# in-game for watched runs; this only affects script launches).
export WINEDLLOVERRIDES="${WINEDLLOVERRIDES:-}winealsa.drv=d;winecoreaudio.drv=d;winepulse.drv=d"

cd "$GAME_ROOT"
# Contained window (owner 2026-10-19): WINE_VIRTUAL_DESKTOP=WxH (e.g.
# 1280x1024) runs the game inside a Wine desktop window - no fullscreen
# grab, no resolution switch, no focus theft. Empty = current behavior;
# the desktop wrapper is proven per-run, not defaulted, until a game
# proves ChaosLauncher injection works through it.
if [ -n "${WINE_VIRTUAL_DESKTOP:-}" ]; then
  say "  Virtual desktop $WINE_VIRTUAL_DESKTOP (contained window)"
  setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine explorer /desktop=atlantis,"$WINE_VIRTUAL_DESKTOP" chaoslauncher/Chaoslauncher.exe \
    > "$LOG_DIR/chaoslauncher.log" 2>&1 < /dev/null &
else
  setsid env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine chaoslauncher/Chaoslauncher.exe \
    > "$LOG_DIR/chaoslauncher.log" 2>&1 < /dev/null &
fi

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
# Buildings ledger: the bot rewrites bwapi-data/write/BUILDINGS.json on
# every warp (cwd-relative, like the build orders the game played with),
# so an interrupted game still leaves its rows. Copy next to the client
# log; a miss is said out loud, never silent.
if [ -f "$CLIENT_CWD/bwapi-data/write/BUILDINGS.json" ]; then
  cp -f "$CLIENT_CWD/bwapi-data/write/BUILDINGS.json" "$LOG_DIR/BUILDINGS.json" 2>/dev/null || true
  say "buildings ledger collected: $LOG_DIR/BUILDINGS.json"
else
  say "buildings ledger missing: $CLIENT_CWD/bwapi-data/write/BUILDINGS.json was never written"
fi
# Owner 2026-10-16: the warp ledger also belongs at the tail of the debug
# copy. A clean game end already prints it (OnGameEnd, stderr) - the
# marker guard keeps this append for killed games only, never duplicating.
if [ -n "${WINE_LIVE_DEBUG:-}" ] && [ -f "$WINE_LIVE_DEBUG" ] \
  && [ -f "$LOG_DIR/BUILDINGS.json" ] \
  && ! grep -q "ledger at game end" "$WINE_LIVE_DEBUG" 2>/dev/null; then
  {
    echo ""
    echo "===== BUILDINGS.json (ledger at game end) ====="
    cat "$LOG_DIR/BUILDINGS.json"
  } >> "$WINE_LIVE_DEBUG" 2>/dev/null || true
  say "ledger appended to debug copy (killed game, no OnGameEnd tail)"
fi
if [ -f "$CLIENT_CWD/bwapi-data/write/EVENTS.jsonl" ]; then
  cp -f "$CLIENT_CWD/bwapi-data/write/EVENTS.jsonl" "$LOG_DIR/EVENTS.jsonl" 2>/dev/null || true
  say "event log collected: $LOG_DIR/EVENTS.jsonl"
else
  say "event log missing: $CLIENT_CWD/bwapi-data/write/EVENTS.jsonl was never written"
fi
if [ -f "$CLIENT_CWD/bwapi-data/write/DECISIONS.log" ]; then
  cp -f "$CLIENT_CWD/bwapi-data/write/DECISIONS.log" "$LOG_DIR/WINE_DECISIONS" 2>/dev/null || true
  say "decisions log collected: $LOG_DIR/WINE_DECISIONS"
  if [ -n "${WINE_LIVE_DECISIONS:-}" ]; then
    cp -f "$LOG_DIR/WINE_DECISIONS" "$WINE_LIVE_DECISIONS" 2>/dev/null || true
    say "decisions live copy: $WINE_LIVE_DECISIONS"
  fi
else
  say "decisions log missing: $CLIENT_CWD/bwapi-data/write/DECISIONS.log was never written"
fi
exit 0
