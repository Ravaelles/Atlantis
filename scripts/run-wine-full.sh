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
#   scripts/run-wine-full.sh [map-name] --headless    # Xvfb :99, invisible
#
# Visible by default (owner 2026-10-21: IDE runs must show the game).
# One Wine game per box, enforced by a lock:
# a second simultaneous game is refused instead of mutually killing both
# (measured 2026-10-21: a verification duel's cleanup pkill'd the JVM
# and wineserver of a running macro mid-game - truncated log, unflushed
# twins, no verdict; the trap cleanup is global by design).
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

say() { echo "[wine-full] $*"; }

close_lock_fd() { eval "exec ${WINE_GAME_LOCK_FD:-9}>&-" 2>/dev/null || true; }
bgs() { ( close_lock_fd; exec setsid "$@" ) & }

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
# Per-run archive: out/wine/ is the live copy every tool reads, but it is
# overwritten by the next game - analysis after the fact needs the run's
# own directory (measured 2026-10-20: a short game overwrote the long
# game's EVENTS before the overlap twins were read). Never cleaned here.
WINE_RUNS_DIR="$ATLANTIS_DIR/out/wine_runs"

# =============================================================================

MAP=""
ENV_FILE_ARG=""
for arg in "$@"; do
  case "$arg" in
    --env=*) ENV_FILE_ARG="${arg#--env=}" ;;
    --visible) HIDDEN=0 ;;
    --hidden) HIDDEN=1 ;;
    --headless) HIDDEN=1 ;;
    -h|--help)
      sed -n '1,45p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
      exit 0 ;;
    *) [ -z "$MAP" ] && MAP="$arg" || { echo "[wine-full] ignoring extra arg: $arg" >&2; } ;;
  esac
done

# Xvfb starts BEFORE the box lock on purpose: background children inherit
# every open fd, including the lock - a persisting Xvfb started after flock
# would hold the lock forever and refuse every later game (measured
# 2026-10-21: REFUSED with the previous Xvfb as the sole holder). Started here
# it never sees the lock fd. Two scripts racing the start is harmless (the
# loser fails to bind :99 and exits; the stale-socket guard covers crashes).
# Xvfb holds no game state, so it is never killed at teardown - the next
# run reuses it instead of paying its boot again.
XVFB_DISPLAY=":99"
XVFB_PID=""
if [ "${HIDDEN:-0}" = "1" ]; then
  if [ -e /tmp/.X11-unix/X99 ] && ! pgrep -f "[X]vfb :99" >/dev/null; then
    rm -f /tmp/.X11-unix/X99 /tmp/.X99-lock
  fi
  if pgrep -f "[X]vfb :99" >/dev/null; then
    say "  Hidden Wine desktop on :99 (Xvfb, invisible - reused, not restarted)"
  else
    say "  Hidden Wine desktop on :99 (Xvfb, invisible)"
    bgs Xvfb :99 -screen 0 1280x1024x24 >"$LOG_DIR/xvfb.log" 2>&1 < /dev/null
    XVFB_PID=$!
  fi
  for _ in $(seq 1 100); do
    [ -e /tmp/.X11-unix/X99 ] && break
    sleep 0.1
  done
  [ -e /tmp/.X11-unix/X99 ] || { echo "[wine-full] Xvfb :99 did not come up" >&2; exit 1; }
  export DISPLAY=:99
fi

# One Wine game per box, enforced (not documented): the trap cleanup below
# kills by process NAME (StarCraft.exe, ChaosLauncher, java.exe), so a second
# game started alongside a running one does not just fail - its cleanup
# murders the first game mid-play (and vice versa).
# flock is kernel-held: no stale lockfiles, released on any death.
WINE_GAME_LOCK="/tmp/atlantis-wine-game.lock"
exec {WINE_GAME_LOCK_FD}>"$WINE_GAME_LOCK" || { echo "[wine-full] cannot open lock $WINE_GAME_LOCK" >&2; exit 2; }
# Background children inherit every open fd, including the box lock above:
# a surviving child (Xvfb, wineserver, a stuck tee) would hold the lock
# forever and refuse every later game (measured 2026-10-21: REFUSED with
# the previous Xvfb as the sole holder). Every background child therefore
# starts via bgs(), which closes the lock copy first - the script keeps its
# own, so the lock still guards the run. Foreground `wine` calls are short
# and cannot hold anything, except the wineserver itself: it is pre-started
# via bgs() below so it never inherits the fd. NOTE: call bgs WITHOUT a
# trailing `&` (it backgrounds internally); an extra `&` spawns a throwaway
# wrapper whose instant exit makes CLIENT_PID look dead (measured).
if ! flock -n "$WINE_GAME_LOCK_FD" 2>/dev/null; then
  # fuser names the actual lock holders (our own fd is filtered out);
  # pgrep on the script name would also match this very check.
  holder_pids="$(fuser "$WINE_GAME_LOCK" 2>/dev/null | tr ' ' '\n' | grep -E '^[0-9]+$' | grep -v "^$$\$" | head -3 | tr '\n' ' ')"
  holder_cmds=""
  for _pid in $holder_pids; do
    holder_cmds="$holder_cmds$(ps -o comm= -p "$_pid" 2>/dev/null | head -1)[${_pid}] "
  done
  echo "[wine-full] REFUSED: another Wine game holds the box lock (${holder_cmds:-unknown holder}); one game at a time" >&2
  exit 2
fi
# Warm server, taken while the lock is ours: the first `wine` call of a
# cold box pays ~0.5 s forking wineserver; a spawned -p server answers every
# later call, and (via bgs) never sees the lock fd. Redundant when one
# already runs (it just exits).
bgs wineserver -p >/dev/null 2>&1 || true
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

# Visible by default (owner 2026-10-21: IDE runs must show the game;
# hidden-by-default hijacked them). Pass --headless for unattended runs:
# HIDDEN=1 starts Xvfb on :99 and points the whole session (client,
# game, ChaosLauncher) at it - nothing ever maps on :0, no resolution
# switch, no window to minimize. The supervisor key relay is
# display-independent (file-based), so nothing else changes.


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
  # No wineserver -k and no Xvfb kill: both are reused by the next run (see
  # above for why that is safe). If a stale table ever attaches, the missing
  # -k is the first suspect.
}

# Belt and braces: the paths below call cleanup() explicitly, but a signal
# or an early exit must not leave test flags in the shared ENV file either.
# cleanup() is idempotent (pkill no-ops, the backup is consumed on restore).
trap cleanup EXIT

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
# What dies here, and why the warm wineserver stays safe:
#   - StarCraft/ChaosLauncher: a second instance breaks the BWAPI injection.
#   - java.exe (8 chars, -x matches): a stray CLIENT JVM from a killed run
#     would hold the previous game's BWAPI sections open, and the next game
#     would attach to a stale table instead of failing loudly. The client for
#     THIS run starts a few lines below, so killing a leftover costs nothing.
#   - wineserver is deliberately NOT killed (measured 2026-10-21: a cold
#     server start costs the first `wine` call of every run ~0.5 s). Keeping
#     it warm is safe because every section holder is dead before anything
#     attaches: the trap below kills them all, and this block re-kills them.
# The one thing that MUST go is a leftover StarCraft/ChaosLauncher, because a
# second instance breaks the BWAPI injection.
pkill -9 -x StarCraft.exe 2>/dev/null
# Never -x on Chaoslauncher.exe: the 17-char name exceeds the 15-char
# comm limit, so -x never matches and a leftover launcher makes every
# new game silently attach to the old map instead (measured:
# an 8v8 request played the previous 1v1). The bracket keeps pgrep
# from matching this shell's own command line (GameExecution.md #2).
pkill -9 -f "[C]haoslauncher.exe" 2>/dev/null
pkill -9 -x java.exe 2>/dev/null
# Poll instead of a fixed sleep: pkill is async, but a dead box returns in
# one tick while a dying process still gets up to 3 s (measured 2026-10-21:
# the fixed sleep cost every fast start 1.0 s).
for _ in $(seq 1 30); do
  if ! pgrep -x StarCraft.exe >/dev/null 2>&1 \
      && ! pgrep -f "[C]haoslauncher.exe" >/dev/null 2>&1; then break; fi
  sleep 0.1
done
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
# The unix->windows jar translation is constant per jar path, but winepath
# spawns a whole Wine process to print it (~0.4 s each run, measured
# 2026-10-21). Cache it in /tmp; a changed jar path simply misses.
WINEPATH_CACHE="/tmp/atlantis-winepath-cache"
WIN_JAR=""
if [ -f "$WINEPATH_CACHE" ]; then
  if [ "$(head -1 "$WINEPATH_CACHE" 2>/dev/null)" = "$JAR" ]; then
    WIN_JAR="$(tail -1 "$WINEPATH_CACHE" 2>/dev/null)"
  fi
fi
if [ -z "$WIN_JAR" ]; then
  WIN_JAR="$(winepath -w "$JAR" 2>/dev/null || true)"
  if [ -n "$WIN_JAR" ]; then
    { echo "$JAR"; echo "$WIN_JAR"; } > "$WINEPATH_CACHE" 2>/dev/null || true
  fi
fi
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
rm -rf "$CLIENT_CWD/bwapi-data/write/cherryvis_dir.rep.cvis"
# The live out/wine/ copies are per-game outputs too, not an archive:
# a game that warps nothing must report "missing", never the previous
# game's ledger (measured 2026-10-20: a UMS duel archived the macro
# game's BUILDINGS.json). The per-run archive below copies what's here.
rm -f "$LOG_DIR/WINE_LOG" "$LOG_DIR/WINE_DEBUG" "$LOG_DIR/WINE_DECISIONS" \
    "$LOG_DIR/EVENTS.jsonl" "$LOG_DIR/BUILDINGS.json"
rm -rf "$LOG_DIR/CVIS"

TEE_OUT=("$CLIENT_LOG")
TEE_ERR=("$LOG_DIR/WINE_DEBUG")
# out/wine/ is the only destination - the old per-ENV live copy is retired.

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
# Array, not a string: an --env= path with spaces (e.g. under "E2E OpenBW
# LOGS") must survive as ONE argv element. The unquoted $CLIENT_ENV_ARG
# split it at the first space and the client saw a truncated path
# (measured 2026-10-21: "ENV file doesn't exist (Z:\sc-ai\E2)").
CLIENT_ARGS=("$CLIENT_MAP_ARG")
[ -n "$CLIENT_ENV_ARG" ] && CLIENT_ARGS+=("$CLIENT_ENV_ARG")
if [ "$LIVE_OUTPUT" = "1" ]; then
  bgs env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine "$JAVA_EXE" "-Dos.name=Windows 10" -jar "$WIN_JAR" "${CLIENT_ARGS[@]}" \
    > >(close_lock_fd; exec tee "${TEE_OUT[@]}") 2> >(close_lock_fd; exec tee "${TEE_ERR[@]}" >&2) < /dev/null
  # CLIENT_PID is now wine itself, not tee (better liveness: it dies when
  # the game ends, while tee may linger flushing). Tees inherit the
  # redirection, so no output is lost when the client exits first.
  CLIENT_PID=$!
else
  bgs env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine "$JAVA_EXE" "-Dos.name=Windows 10" -jar "$WIN_JAR" "${CLIENT_ARGS[@]}" \
    > >(close_lock_fd; exec tee "${TEE_OUT[@]}" > /dev/null) 2> >(close_lock_fd; exec tee "${TEE_ERR[@]}" > /dev/null) < /dev/null
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

# Replay name carries opponent + engine (owner: bare MAP_DATE.rep names are
# unattributable). MEASURED on real BWAPI 4.4.0, macro path: any template
# beyond %MAP% + strftime ($Y...) silently saves NOTHING (B64 ENEMYNAMES,
# B65/B66 BOTRACE/ENEMYRACES, B69 static PvsZ - zero reps; old template
# saved 7/9 incl. B67/B68/B72, duel path unaffected). So the engine keeps the
# proven template and the RICH name is composed here, where both races are
# known (client race above, enemy_race below): the per-run archive and the
# shared dir get MAP_PvsZ_wine_DATE.rep next to the engine's file.
REPLAYS_DIR="${REPLAYS_DIR:-/sc-ai/REPLAYS}"
ENEMY_RACE_INI="$(grep -oE '^enemy_race *= *[A-Za-z]+' "$INI" | grep -oE '[A-Za-z]+$' || true)"
BOT_LETTER="$(printf '%s' "${CLIENT_RACE:-Protoss}" | cut -c1)"
FOE_LETTER="$(printf '%s' "${ENEMY_RACE_INI:-Zerg}" | cut -c1)"
REPLAY_TEMPLATE="%MAP%_\$Y\$m\$d_\$H\$M\$S.rep"
if grep -qE "^save_replay *=" "$INI"; then
  sed -i -E "s|^save_replay *=.*|save_replay=$REPLAYS_DIR/$REPLAY_TEMPLATE|" "$INI"
else
  sed -i -E "s|^(enemy_race *=.*)$|\1\nsave_replay=$REPLAYS_DIR/$REPLAY_TEMPLATE|" "$INI"
fi
say "  Replay template: $REPLAYS_DIR/$REPLAY_TEMPLATE"
PRE_REP="$(ls -t "$REPLAYS_DIR"/*.rep 2>/dev/null | head -1 || true)"


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
  bgs env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine explorer /desktop=atlantis,"$WINE_VIRTUAL_DESKTOP" chaoslauncher/Chaoslauncher.exe \
    > "$LOG_DIR/chaoslauncher.log" 2>&1 < /dev/null
else
  bgs env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
    wine chaoslauncher/Chaoslauncher.exe \
    > "$LOG_DIR/chaoslauncher.log" 2>&1 < /dev/null
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
# Teardown-race guard (replay save flakiness, measured B64-B71: the engine
# sometimes writes no .rep at all on the macro path): give the engine up
# to 20 s after client exit to flush the replay before the -9 below.
# Breaks early the moment a new rep lands; a no-op when it was already
# saved (or when nothing ever comes - bounded cost, no behavior change).
for _ in $(seq 1 20); do
  CUR_REP="$(ls -t "$REPLAYS_DIR"/*.rep 2>/dev/null | head -1 || true)"
  if [ -n "${CUR_REP:-}" ] && [ "$CUR_REP" != "${PRE_REP:-}" ]; then break; fi
  [ "$_" = "1" ] && say "  waiting for the engine replay file..." || true
  sleep 1
done
ls -lt "$REPLAYS_DIR"/ 2>/dev/null | head -3
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
# log. A clean game end already prints it (OnGameEnd, stderr) - the
# marker guard keeps this append for killed games only, never duplicating.
if [ -f "$LOG_DIR/WINE_DEBUG" ] \
  && [ -f "$LOG_DIR/BUILDINGS.json" ] \
  && ! grep -q "ledger at game end" "$LOG_DIR/WINE_DEBUG" 2>/dev/null; then
  {
    echo ""
    echo "===== BUILDINGS.json (ledger at game end) ====="
    cat "$LOG_DIR/BUILDINGS.json"
  } >> "$LOG_DIR/WINE_DEBUG" 2>/dev/null || true
  say "ledger appended to debug log (killed game, no OnGameEnd tail)"
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
else
  say "decisions log missing: $CLIENT_CWD/bwapi-data/write/DECISIONS.log was never written"
fi
# CherryVis trace (bot-side writer, non-UMS games only): same 4 artifacts
# the OpenBW runner collects. A miss is said out loud, never fatal - a
# killed game never runs onGameEnd, so no trace exists by design.
CVIS_SRC="$CLIENT_CWD/bwapi-data/write/cherryvis_dir.rep.cvis"
if [ -d "$CVIS_SRC" ]; then
  mkdir -p "$LOG_DIR/CVIS"
  cp -f "$CVIS_SRC"/trace.json "$CVIS_SRC"/trace.json.zst "$CVIS_SRC"/trace_raw.json "$CVIS_SRC"/game_summary.json "$LOG_DIR/CVIS/" 2>/dev/null || true
  say "cherryvis trace collected: $LOG_DIR/CVIS"
else
  say "cherryvis trace missing: $CVIS_SRC was never written (was CHERRYVIS_ENABLED seen by the bot?)"
fi
# Per-run archive (MAP is final here): the live copy above stays where the
# tooling reads it; the run's own directory keeps the evidence past the
# next game. Missing files are skipped, never fatal.
{
  RUN_STAMP="$(date +%Y%m%d_%H%M%S)"
  MAP_SLUG="$(basename "${MAP:-game}" | tr -c 'A-Za-z0-9._-' '_')"
  RUN_DIR="$WINE_RUNS_DIR/${RUN_STAMP}_${MAP_SLUG}"
  mkdir -p "$RUN_DIR" || exit 0
  for f in WINE_LOG WINE_DEBUG WINE_DECISIONS EVENTS.jsonl BUILDINGS.json chaoslauncher.log xvfb.log; do
    [ -f "$LOG_DIR/$f" ] && cp -f "$LOG_DIR/$f" "$RUN_DIR/$f" 2>/dev/null || true
  done
  if [ -d "$LOG_DIR/CVIS" ]; then
    cp -rf "$LOG_DIR/CVIS" "$RUN_DIR/CVIS" 2>/dev/null || true
    say "cherryvis trace archived: $RUN_DIR/CVIS"
  fi
  # The replay carries opponent + engine in its name (see template above);
  # archive this game's file next to its logs. The newest-first comparison
  # attributes correctly: a game that saved nothing leaves POST==PRE.
  # Rich copy: the engine's file keeps its proven name; the archive and
  # the shared dir also get MAP_PvsZ_wine_DATE.rep (opponent + engine),
  # composed here from the known races - never via engine placeholders.
  POST_REP="$(ls -t "$REPLAYS_DIR"/*.rep 2>/dev/null | head -1 || true)"
  if [ -n "${POST_REP:-}" ] && [ "$POST_REP" != "${PRE_REP:-}" ]; then
    RICH_NAME="$(basename "$POST_REP" .rep | sed -E "s/_([0-9]{8}_[0-9]{6})\$/_${BOT_LETTER:-P}vs${FOE_LETTER:-Z}_wine_\1/").rep"
    cp -f "$POST_REP" "$RUN_DIR/$RICH_NAME" 2>/dev/null \
      && say "replay archived: $RUN_DIR/$RICH_NAME" || true
    [ "$RICH_NAME" != "$(basename "$POST_REP")" ] \
      && cp -f "$POST_REP" "$REPLAYS_DIR/$RICH_NAME" 2>/dev/null || true
  fi
  ln -sfn "$RUN_DIR" "$WINE_RUNS_DIR/latest"
  say "run archived: $RUN_DIR"
} || true
exit 0
