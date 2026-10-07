#!/usr/bin/env bash
# E2E for Atlantis on the headless OpenBW engine - no Wine, no StarCraft, no
# ChaosLauncher, ever (CONVENTIONS §14).
#
# There are two entirely separate game setups in this workspace and they must
# not be mixed:
#
#   WINE setup  (owner-only)   scripts/run-wine-*.sh, bots/AtlantisP/AI/,
#                              ENV with GAME_LAUNCHER=WINE: real StarCraft 1.16.1
#                              under Wine + ChaosLauncher, vs the Blizzard AI.
#   OPENBW setup (this file)   StardustDevEnvironment/, ENV with
#                              GAME_LAUNCHER=OPENBW: headless OpenBW server, the
#                              bot attaches as a BWAPI client. No game window,
#                              no licensed game, no Wine.
#
# The failure this script is shaped to avoid: a bot process started with the
# WINE ENV brings StarCraft up by itself (the Wine launcher starts the game),
# so pointing an "OpenBW" run at bots/AtlantisP/AI/ silently launched a real
# game. Therefore this script never uses bots/AtlantisP/AI and always runs the
# bot from a directory whose ENV says GAME_LAUNCHER=OPENBW.
#
# Usage:
#   bash scripts/run-openbw-e2e.sh [map] [race] [enemy-race]
#   bash scripts/run-openbw-e2e.sh --self-test     # no game, checks setup only
#
# Requirements: a JDK, the bot jar, and the StardustDevEnvironment build tree.
# Owner-only tier: a real game takes minutes, so it is not in the fast loop
# (same ruling as scripts/run-full-tests.sh).
#
# CONVENTIONS §13: every command is bounded to 6 minutes. Past that the run is
# hung or misconfigured, not slow - kill it and change the approach.
set -euo pipefail

ATLANTIS_DIR="/sc-ai/Atlantis"
HARNESS_DIR="/sc-ai/StardustDevEnvironment"
GAME_DIR="$HARNESS_DIR/build/test"
SERVER_SCRIPT="$HARNESS_DIR/scripts/run-openbw-server.sh"
WINE_BOT_DIR="$ATLANTIS_DIR/bots/AtlantisP/AI"
JAR="$WINE_BOT_DIR/Atlantis.jar"

MAP_DEFAULT="maps/sscai/(4)Python.scx"
MAP="${1:-$MAP_DEFAULT}"
RACE="${2:-Protoss}"
ENEMY_RACE="${3:-Zerg}"

SELF_TEST=0
[ "${1:-}" = "--self-test" ] && SELF_TEST=1

say() { echo "[openbw-e2e] $*"; }
fail() { echo "[openbw-e2e] ERROR: $*" >&2; exit 2; }

# Nothing may outlive the run: a leftover BWAPILauncher holds the shared-memory
# game table, so the next run's client would attach to a dead game and the
# failure would look random. "java -jar Atlantis.jar" is matched by the jar
# path, not the bare word (which would also match the shell itself).
cleanup() {
  [ -n "${SERVER_PID:-}" ] && kill "$SERVER_PID" 2>/dev/null || true
  [ -n "${BOT_PID:-}" ] && kill "$BOT_PID" 2>/dev/null || true
  # -x (exact process name), never -f: a -f pattern matches this script's own
  # command line and the shell kills itself before it can print anything
  # (measured 2026-10-07, _AI/CHALLENGES/GameExecution.md #2).
  pkill -9 -x BWAPILauncher 2>/dev/null || true
  return 0
}
trap cleanup EXIT INT TERM

# --- 1. Preconditions -----------------------------------------------------
[ -x "$SERVER_SCRIPT" ] || fail "OpenBW server script not found: $SERVER_SCRIPT"
[ -f "$JAR" ] || fail "bot jar not found: $JAR (build with scripts/build-bot-jar.sh)"
[ -d "$GAME_DIR" ] || fail "harness game dir not found: $GAME_DIR"
for mpq in StarDat.mpq BrooDat.mpq Patch_rt.mpq; do
  [ -f "$GAME_DIR/$mpq" ] || fail "missing $mpq in $GAME_DIR (harness not built?)"
done

LAUNCHER="$HARNESS_DIR/build/bin/BWAPILauncher"
[ -x "$LAUNCHER" ] || fail "BWAPILauncher not built (cmake --build $HARNESS_DIR/build)"

# --- 2. The one thing this script must never do: start StarCraft ----------
# A leftover Wine game would mean an earlier run leaked into this one, and a
# real game must never be part of an OpenBW run.
if pgrep -x StarCraft.exe >/dev/null 2>&1 || pgrep -f 'Chaoslauncher' >/dev/null 2>&1; then
  fail "StarCraft/ChaosLauncher is running. This script never starts it; kill it and retry."
fi

# --- 3. An isolated OpenBW environment for the bot ------------------------
# The bot reads its ENV from its working directory. Running it from
# bots/AtlantisP/AI (the WINE folder, GAME_LAUNCHER=WINE) is exactly what used
# to launch a real game, so the OpenBW run gets its own directory with its own
# ENV, GAME_LAUNCHER=OPENBW.
BOT_RUN_DIR="$ATLANTIS_DIR/bots/AtlantisOpenBW/AI"
mkdir -p "$BOT_RUN_DIR"
cp "$JAR" "$BOT_RUN_DIR/Atlantis.jar"

cat > "$BOT_RUN_DIR/ENV" <<'EOF'
# Atlantis on the headless OpenBW engine (E2E tier).
# Deliberately NOT the Wine ENV: GAME_LAUNCHER=OPENBW means the bot attaches
# to an already-running BWAPILauncher and never starts a game itself.
# See Atlantis/_AI/CONVENTIONS.md §14 and scripts/run-openbw-e2e.sh.
LOCAL=true
GAME_LAUNCHER=OPENBW
FORCE_GG_FOR_ENEMY=false
POSTGAME_COPY_CHERRYVIS_TO=
EOF

# Build orders are read relative to the bot directory.
if [ -d "$WINE_BOT_DIR/build_orders" ] && [ ! -e "$BOT_RUN_DIR/build_orders" ]; then
  ln -s "$WINE_BOT_DIR/build_orders" "$BOT_RUN_DIR/build_orders"
fi

if [ "$SELF_TEST" -eq 1 ]; then
  say "self-test OK:"
  say "  server script : $SERVER_SCRIPT"
  say "  game dir      : $GAME_DIR"
  say "  bot jar       : $JAR"
  say "  bot run dir   : $BOT_RUN_DIR (GAME_LAUNCHER=OPENBW)"
  say "  no StarCraft/ChaosLauncher running"
  exit 0
fi

# --- 4. Run: server first, bot second ------------------------------------
# The server must be up before the client attaches (LOCAL-STARDRAFT.md); the
# client itself polls, so a short head start is enough.
SERVER_LOG="$ATLANTIS_DIR/out/openbw/server.log"
BOT_LOG="$ATLANTIS_DIR/out/openbw/bot.log"
mkdir -p "$(dirname "$SERVER_LOG")"

# The client maps a socket named after the SERVER's PID, so no stale server or
# socket may exist before hosting (the server script documents the same hazard
# from its own side). Clearing here rather than relying on it keeps the run
# self-contained.
pkill -9 -x BWAPILauncher 2>/dev/null || true
rm -f /tmp/bwapi_socket_* 2>/dev/null || true

say "hosting OpenBW game: map=$MAP race=$RACE enemy=$ENEMY_RACE"

# Bounded to 6 minutes (CONVENTIONS §13). `timeout` also covers the server,
# so a hung game cannot outlive the run.
timeout 360 bash "$SERVER_SCRIPT" "$MAP" "$RACE" "$ENEMY_RACE" >"$SERVER_LOG" 2>&1 &
SERVER_PID=$!
sleep 2

if ! kill -0 "$SERVER_PID" 2>/dev/null; then
  say "server exited immediately; log tail:"
  tail -20 "$SERVER_LOG" | sed 's/^/    /'
  exit 1
fi

say "starting bot: $BOT_RUN_DIR (headless, GAME_LAUNCHER=OPENBW)"
cd "$BOT_RUN_DIR"
BOT_EXIT=0
timeout 360 java -jar Atlantis.jar >"$BOT_LOG" 2>&1 &
BOT_PID=$!
wait "$BOT_PID" || BOT_EXIT=$?

say "bot exit code: $BOT_EXIT"
say "server log: $SERVER_LOG"
say "bot log:    $BOT_LOG"

if [ "$BOT_EXIT" -ne 0 ]; then
  say "bot did not exit cleanly; log tail:"
  tail -20 "$BOT_LOG" | sed 's/^/    /'
fi

exit "$BOT_EXIT"
