#!/usr/bin/env bash

ATLANTIS_DIR="/sc-ai/Atlantis"
HARNESS_DIR="/sc-ai/StardustDevEnvironment"
GAME_DIR="$HARNESS_DIR/build/test"
SERVER_SCRIPT="$HARNESS_DIR/scripts/run-openbw-server.sh"
WINE_BOT_DIR="$ATLANTIS_DIR/bots/AtlantisP/AI"

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
# Hard limits (top of the file, CONVENTIONS §17): TIMEOUT_SECONDS=120 wall-clock,
# INGAME_TIME=20 game minutes. Both are refused when widened, never silently
# extended; the outer command must also cap the whole run at TIMEOUT_SECONDS.
#
# Requirements: a JDK, the bot jar, and the StardustDevEnvironment build tree.
# Owner-only tier: a real game takes minutes, so it is not in the fast loop
# (same ruling as scripts/run-full-tests.sh).
#
# CONVENTIONS §13: every command is bounded to 6 minutes. Past that the run is
# hung or misconfigured, not slow - kill it and change the approach.
set -euo pipefail


# The bot jar to run. `/sc-ai/BOTS` is the real deployed bot tree (the owner's
# shortcut), so an OpenBW run uses the *same* artifact the owner runs - a stale
# copy in the repo's own bot folder is how a run silently tested old code
# (measured 2026-10-08: the game ran a jar whose default map was
# `ums/rav/7th_rav.scx`, from a build predating the fix, while the log said
# everything was fine).
BOTS_DIR="/sc-ai/BOTS"
if [ -f "$BOTS_DIR/AtlantisP/AI/Atlantis.jar" ]; then
  JAR="$BOTS_DIR/AtlantisP/AI/Atlantis.jar"
else
  JAR="$WINE_BOT_DIR/Atlantis.jar"
fi

# The harness serves maps from StardustDevEnvironment/build/test/maps/; the
# owner's map is the COG TauCross (measured path 2026-10-07 - the sscai copy
# has a different name, and a wrong path is what made the first server start
# exit immediately).
MAP_DEFAULT="maps/cog/(3)TauCross1.1.scx"
MAP="${1:-$MAP_DEFAULT}"
RACE="${2:-Protoss}"
ENEMY_RACE="${3:-Zerg}"

# ENEMY_COUNT=0 plays with NO opponent (every enemy slot closed - confirmed in
# AutoMenuManager.cpp: enemyCount < autoMenuEnemyCount, else closeSlot()). That is
# the undisturbed-economy run: our bot alone on a real melee map, so an economy,
# production or placement bug shows up as itself instead of being masked by a
# fight. Use ENEMY_COUNT=1 (the default) for a normal game.
ENEMY_COUNT="${ENEMY_COUNT:-1}"

SELF_TEST=0
[ "${1:-}" = "--self-test" ] && SELF_TEST=1

say() { echo "[openbw-e2e] $*"; }
fail() { echo "[openbw-e2e] ERROR: $*" >&2; exit 2; }

# === Hard simulation limits (CONVENTIONS §17) ==============================
# The two knobs live here, at the top, and nothing below may widen them.
#   TIMEOUT_SECONDS  wall-clock cap for the whole simulation.
#   INGAME_TIME      in-game seconds after which the bot ends the game itself
#                    (20 real minutes of game time).
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-120}"
INGAME_TIME="${INGAME_TIME:-$((60 * 20))}"
if [ "$TIMEOUT_SECONDS" -gt 120 ]; then
  fail "TIMEOUT_SECONDS=$TIMEOUT_SECONDS exceeds the OpenBW 120-second simulation cap (CONVENTIONS §17)"
fi
if [ "$TIMEOUT_SECONDS" -le 0 ]; then
  fail "TIMEOUT_SECONDS must be positive, got $TIMEOUT_SECONDS"
fi

# Nested exits stay ordered: the bot ends its own game first, the bot JVM is
# killed next, the host last - all within TIMEOUT_SECONDS.
BOT_KILL_SECONDS="$TIMEOUT_SECONDS"
HOST_KILL_SECONDS="$TIMEOUT_SECONDS"

# Teardown. A leftover host holds the game table, so the next run's client
# adopts a dead PID and loops on "Unable to open communications socket"
# (measured 2026-10-07). It runs on EXIT/INT/TERM and on the normal path.
#
# `pkill -9 -x BWAPILauncher` and never -f: a -f pattern also matches this
# script's own command line and the shell kills itself before printing
# anything (_AI/CHALLENGES/GameExecution.md #2).
cleanup() {
  pkill -9 -x BWAPILauncher 2>/dev/null || true
  rm -f /tmp/bwapi_socket_* 2>/dev/null || true
  return 0
}
trap cleanup EXIT INT TERM

# --- 1. Preconditions -----------------------------------------------------
[ -x "$SERVER_SCRIPT" ] || fail "OpenBW server script not found: $SERVER_SCRIPT"
[ -f "$JAR" ] || fail "bot jar not found: $JAR (build with scripts/build-bot-jar.sh)"

# The jar must match the working tree. A play that runs old code is the one
# failure this script cannot afford - the bug being hunted is often already fixed,
# and the search goes somewhere it no longer lives (owner's report, 2026-10-08).
# The check compares a hash of the whole source tree against the tag the jar
# carries, so it catches EVERY source change, not the files someone listed.
if ! bash scripts/check-jar-freshness.sh "$JAR" --quiet; then
  say "the bot jar is stale (or untagged). Details:"
  bash scripts/check-jar-freshness.sh "$JAR" 2>&1 | sed 's/^/    /'
  say "Rebuilding it now, so this run uses your current code."
  timeout 300 bash scripts/build-bot-jar.sh "$JAR" >/tmp/atlantis-e2e-build.log 2>&1 \
    || { tail -20 /tmp/atlantis-e2e-build.log | sed 's/^/    /'; fail "rebuild failed"; }
  bash scripts/check-jar-freshness.sh "$JAR" --quiet \
    || fail "the rebuild did not produce a matching jar - check /tmp/atlantis-e2e-build.log"
fi

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

# The bot resolves build orders against BWAPI_DATA_PATH + AI/build_orders/, so
# this must point at a bwapi-data root that actually has them. Prefer the
# repository's own tree; fall back to a bot folder that has one.
BWAPI_DATA_ROOT="$ATLANTIS_DIR/bwapi-data"
if [ ! -d "$BWAPI_DATA_ROOT/AI/build_orders" ]; then
  for candidate in "$WINE_BOT_DIR/.." "$ATLANTIS_DIR/bots/AtlantisP"; do
    if [ -d "$candidate/bwapi-data/AI/build_orders" ]; then BWAPI_DATA_ROOT="$candidate/bwapi-data"; break; fi
  done
fi
[ -d "$BWAPI_DATA_ROOT/AI/build_orders" ] || fail "no build_orders under $BWAPI_DATA_ROOT"
say "build orders root: $BWAPI_DATA_ROOT"

# ENV must sit in the WORKING DIRECTORY (Env.envFilePath() tries
# `bwapi-data/AI/ENV`, then `../bwapi-data/AI/ENV`, then `ENV`), so it has to
# live in the directory the jar is started from. Missing it is not fatal but it
# silently selects the DEFAULT backend - which is Chaos, and on Linux that ends
# in `IOException: Cannot run program "taskkill"` (measured 2026-10-07).
#
# Written to both the bot root and its AI/ subdir: the jar may legitimately be
# started from either (scbw starts it from the bot folder, a by-hand run often
# from AI/), and a missing ENV is a silent wrong-backend, not a clear error.
write_env() {
  cat > "$1" <<EOF
# Atlantis on the headless OpenBW engine (E2E tier).
# Deliberately NOT the Wine ENV: GAME_LAUNCHER=OPENBW means the bot attaches
# to an already-running BWAPILauncher and never starts a game itself.
# See Atlantis/_AI/CONVENTIONS.md §14 and scripts/run-openbw-e2e.sh.
LOCAL=true
GAME_LAUNCHER=OPENBW
# With LOCAL=true the bot resolves build orders from BWAPI_DATA_PATH; without it
# every lookup misses and the game plays with no production at all (the run then
# looks like a stalled bot, with no error line) - measured 2026-10-08.
BWAPI_DATA_PATH=$BWAPI_DATA_ROOT
FORCE_GG_FOR_ENEMY=false
POSTGAME_COPY_CHERRYVIS_TO=
# The bot ends its own game before the HOST's timeout kills the host (measured
# 2026-10-08: without this the host died first, the client was orphaned and
# looped on "No server proc ID" until the script SIGTERM'd it, so a played game
# still reported failure). ForceExitLocallyAfterRealSeconds calls
# Atlantis.onEnd -> System.exit(0), which is the only exit that leaves the host
# alive to be torn down cleanly. TIMEOUT_SECONDS (120, see the top of this file)
# is the wall-clock limit; INGAME_TIME is the in-game limit.
FORCE_END_GAME_AFTER_REAL_SECONDS=$TIMEOUT_SECONDS
FORCE_END_GAME_AFTER_INGAME_SECONDS=$INGAME_TIME
# Pass-through flags, so a run can be pointed at the new subsystems without
# editing files: PLACEMENT=catalogue selects the rewritten planner
# (03_PLACEMENT.md), PRODUCTION_V2=DRY_RUN/LIVE selects the v2 production policy.
# Both are absent by default, which keeps the legacy path in charge.
EOF

  # Only written when the caller actually set them, so an unset variable does not
  # become an empty one the bot has to interpret.
  if [ -n "${PLACEMENT:-}" ]; then
    echo "PLACEMENT=$PLACEMENT" >> "$1"
  fi
  if [ -n "${PRODUCTION_V2:-}" ]; then
    echo "PRODUCTION_V2=$PRODUCTION_V2" >> "$1"
  fi
}
write_env "$BOT_RUN_DIR/ENV"

# The bot reads its build orders from BUILD_ORDERS_PATH = "AI/build_orders/",
# resolved against its WORKING DIRECTORY (ABuildOrderLoader.buildOrdersDir()).
# That makes the working directory part of the contract:
#
#   cwd = <botRoot>  ->  needs <botRoot>/AI/build_orders   (how scbw launches)
#
# Getting this wrong is fatal and looks like a connection problem: the attach
# SUCCEEDS, then the client thread dies with
#     RuntimeException: Current BUILD ORDER is NULL
# while processing the first frame (measured 2026-10-07).
#
# So the launcher runs the jar from the bot ROOT (not from its AI/ subdir) and
# this links the orders + the maps tree that the loader also expects.
BOT_ROOT="$(dirname "$BOT_RUN_DIR")"
mkdir -p "$BOT_ROOT/AI"
write_env "$BOT_RUN_DIR/ENV"
write_env "$BOT_ROOT/ENV"
write_env "$BOT_ROOT/AI/ENV"

for candidate in "$ATLANTIS_DIR/bwapi-data/AI/build_orders" \
                 "$WINE_BOT_DIR/build_orders"; do
  if [ -d "$candidate" ] && [ ! -e "$BOT_ROOT/AI/build_orders" ]; then
    ln -s "$candidate" "$BOT_ROOT/AI/build_orders"
    say "linked build orders: $BOT_ROOT/AI/build_orders -> $candidate"
    break
  fi
done

if [ ! -d "$BOT_ROOT/AI/build_orders" ]; then
  fail "no build_orders: link $ATLANTIS_DIR/bwapi-data/AI/build_orders or $WINE_BOT_DIR/build_orders into $BOT_ROOT/AI"
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
# The client attaches to the SERVER's shared segment, and that segment exists
# only while the server process is alive. So the order is: host the game, wait
# for the segment (not a guessed sleep), then start the client, and only tear
# the host down after the client is gone.
SERVER_LOG="$ATLANTIS_DIR/out/openbw/server.log"
BOT_LOG="$ATLANTIS_DIR/out/openbw/bot.log"
mkdir -p "$(dirname "$SERVER_LOG")"

# Clear stale transport first: a leftover host or segment poisons this run.
# Both namespaces are cleared because the server publishes a /dev/shm segment
# AND a /tmp socket, both named after the hosting PID.
pkill -9 -x BWAPILauncher 2>/dev/null || true
rm -f /tmp/bwapi_socket_* /dev/shm/bwapi_shared_memory_* 2>/dev/null || true

say "hosting OpenBW game: map=$MAP race=$RACE enemy=$ENEMY_RACE enemy_count=$ENEMY_COUNT"

# setsid + nohup: the host must survive this script's process group (the
# harness script ends with `exec BWAPILauncher`, so its PID is the host's PID).
# `timeout` bounds it (CONVENTIONS §13) so a hung game cannot outlive the run.
#
# BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON is THE FIX that makes the client attach
# (measured 2026-10-08). The harness's BWAPI creates its shared-memory game
# registry (`/dev/shm/bwapi_shared_memory_game_list`, the table the Java client
# reads the server PID from) only when `Server::serverEnabled` is true, and that
# is `LoadConfigStringUCase("config", "shared_memory", "ON") == "ON"`. With no
# bwapi.ini present the setting has to come from the environment - Config.cpp
# reads `BWAPI_CONFIG_<SECTION>__<KEY>` before the file - so without this the
# host served a socket but published no registry, and the client failed with
# "No server proc ID" (the blocker recorded in _AI/CHALLENGES/OpenBW.md as the
# "sixth blocker", whose isConnected=1 reading was a stale entry from a dead
# host, not what a live host writes: with this flag the slot is published with
# isConnected=0, which is exactly what the client's free-slot search wants).
setsid nohup env BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON \
  BWAPI_CONFIG_AUTO_MENU__ENEMY_COUNT="$ENEMY_COUNT" \
  timeout "$HOST_KILL_SECONDS" bash "$SERVER_SCRIPT" "$MAP" "$RACE" "$ENEMY_RACE" \
  >"$SERVER_LOG" 2>&1 </dev/null &
SERVER_WRAPPER_PID=$!

# Do NOT wait for the socket. The host publishes the shared segment and the
# registry up front, but creates the socket only once a client is knocking -
# its own log says so:
#     Start the Java client now (it polls until the server is up).
# Waiting for the socket before starting the client deadlocks: the host waits
# for the client, and the client is not started yet (measured 2026-10-07 - the
# run that failed here timed out at exactly this line, and the transcript that
# showed why came from a manual run of the same two commands).
#
# What the host DOES publish immediately is the registry entry carrying its
# PID, which is the first thing the client reads. That is what to wait for.
HOSTED=0
for _ in $(seq 1 60); do
  if [ -f /dev/shm/bwapi_shared_memory_game_list ] \
     && [ -n "$(xxd -p -l 8 /dev/shm/bwapi_shared_memory_game_list 2>/dev/null | tr -d '0')" ]; then
    HOSTED=1
    break
  fi
  if ! kill -0 "$SERVER_WRAPPER_PID" 2>/dev/null && ! pgrep -x BWAPILauncher >/dev/null 2>&1; then
    say "server exited before hosting; log tail:"
    tail -20 "$SERVER_LOG" | sed 's/^/    /'
    exit 1
  fi
  sleep 0.5
done

if [ "$HOSTED" -ne 1 ]; then
  say "server published no game registry entry within 30 s; log tail:"
  tail -20 "$SERVER_LOG" | sed 's/^/    /'
  exit 1
fi

say "game hosted (registry: $(xxd -p -l 8 /dev/shm/bwapi_shared_memory_game_list 2>/dev/null))"
say "starting bot: $BOT_RUN_DIR (headless, GAME_LAUNCHER=OPENBW)"
# cwd = the bot ROOT, because the build-order path is relative to the working
# directory and is "AI/build_orders/" (see the link section above).
cd "$BOT_ROOT"
BOT_EXIT=0
# `--map=` wins over Main's hard-coded map choices
# (ActiveMap.readMapFromCliArgument), so the map this script hosts and the map
# the bot analyses are the same one. Without it the bot's own default was used
# while the harness hosted something else (measured 2026-10-08: the bot analysed
# a UMS map and could place nothing on the real one).
timeout "$BOT_KILL_SECONDS" java -jar "$BOT_RUN_DIR/Atlantis.jar" "--map=$MAP" >"$BOT_LOG" 2>&1 &
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
