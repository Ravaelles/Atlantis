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
#   bash scripts/run-openbw-e2e.sh --self-test              # setup only
#   bash scripts/run-openbw-e2e.sh --self-test-assertions   # test scenario assertions, no game
#
# Runtime limits (top of the file, CONVENTIONS §17): a single run gets
# RUN_BUDGET_SECONDS=20 wall-clock by default - a redirecting or placement test
# is short, and one that needs longer is stuck, not slow. The 120 s ceiling is
# for the mega-test and the full E2E sweep only, selected with MEGA_TEST=1.
# INGAME_TIME=20 game minutes is the in-game self-termination. Widening either is
# refused rather than honoured; the outer command must cap the run at
# TIMEOUT_SECONDS and must never use a blind `sleep` to wait for it (poll with a
# condition - CONVENTIONS §17).
#
# Scenario assertions (optional): game summary counters plus log checks.
#   EXPECT_MIN_INGAME_SECONDS=<n>   fail if the game ended earlier than n
#   EXPECT_MIN_KILLED=<n>           fail if we killed fewer units
#   EXPECT_MAX_KILLED=<n>           fail if we killed more units
#   EXPECT_MIN_PYLONS=<n>           fail if fewer Protoss Pylons exist at end
#   EXPECT_MIN_GATEWAYS=<n>         fail if fewer Protoss Gateways exist at end
#   EXPECT_MIN_RESOURCE_BALANCE=<n> fail if Resource killed/lost < n
#   EXPECT_NO_PLACEMENT_FAILURES=1  fail if "Can't find place" is logged
# When none is set the script only reports the verdict, as before.
#
# Requirements: a JDK, the bot jar, and the StardustDevEnvironment build tree.
# Owner-only tier: a real game takes minutes, so it is not in the fast loop
# (same ruling as scripts/run-full-tests.sh).
#
# CONVENTIONS §13: every command is bounded to 120 seconds.
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
ASSERTION_SELF_TEST=0
[ "${1:-}" = "--self-test" ] && SELF_TEST=1
[ "${1:-}" = "--self-test-assertions" ] && ASSERTION_SELF_TEST=1

say() { echo "[openbw-e2e] $*"; }
fail() { echo "[openbw-e2e] ERROR: $*" >&2; exit 2; }

# === Simulation runtime limits (CONVENTIONS §17) ===========================
# Three knobs live here, at the top, and nothing below may widen them.
#   MEGA_TEST        set to 1 only for the single OpenBW mega-test and the full
#                    E2E sweep - the only runs allowed to use the 120 s ceiling.
#   TIMEOUT_SECONDS  wall-clock cap for the whole simulation. 120 s is the HARD
#                    CEILING and applies only when MEGA_TEST=1; otherwise the
#                    normal per-test budget below is the limit.
#   RUN_BUDGET_SECONDS  the normal budget for a single OpenBW test. A redirecting
#                    or placement test is short; one that needs more than this is
#                    stuck, not slow (CONVENTIONS §17).
#   INGAME_TIME      in-game seconds after which the bot ends the game itself
#                    (20 real minutes of game time).
MEGA_TEST="${MEGA_TEST:-0}"
RUN_BUDGET_SECONDS="${RUN_BUDGET_SECONDS:-20}"
if [ "$MEGA_TEST" = "1" ]; then
  TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-120}"
else
  # A normal run gets the 20 s budget, never the 120 s ceiling. Asking for more
  # without MEGA_TEST=1 is refused rather than silently granted: the ceiling is
  # for the mega-test, not for a single scenario that is having a bad day.
  if [ "${TIMEOUT_SECONDS:-$RUN_BUDGET_SECONDS}" -gt "$RUN_BUDGET_SECONDS" ]; then
    fail "TIMEOUT_SECONDS=${TIMEOUT_SECONDS} exceeds the ${RUN_BUDGET_SECONDS}s single-test budget; set MEGA_TEST=1 only for the mega-test/full sweep (CONVENTIONS §17)"
  fi
  TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-$RUN_BUDGET_SECONDS}"
fi
INGAME_TIME="${INGAME_TIME:-$((60 * 20))}"
if [ "$TIMEOUT_SECONDS" -gt 120 ]; then
  fail "TIMEOUT_SECONDS=$TIMEOUT_SECONDS exceeds the OpenBW 120-second simulation ceiling (CONVENTIONS §17)"
fi
if [ "$TIMEOUT_SECONDS" -le 0 ]; then
  fail "TIMEOUT_SECONDS must be positive, got $TIMEOUT_SECONDS"
fi

if [ "$ASSERTION_SELF_TEST" -eq 1 ]; then
  SELF_LOG="$(mktemp)"
  printf '%s\n' '### Total time: 428 seconds. ###' \
    '### Units killed/lost: 12/3 ###' '### Resource killed/lost: +450 ###' \
    '### Protoss buildings: Pylons=1 Gateways=1 ###' > "$SELF_LOG"
  EXPECT_MIN_INGAME_SECONDS=420 EXPECT_MIN_KILLED=12 EXPECT_MAX_KILLED=40 \
    EXPECT_MIN_PYLONS=1 EXPECT_MIN_GATEWAYS=1 EXPECT_MIN_RESOURCE_BALANCE=-200 \
    bash scripts/assert-openbw-scenario.sh "$SELF_LOG"
  printf '%s\n' '### Total time: 428 seconds. ###' \
    '### Units killed/lost: 0/3 ###' '### Resource killed/lost: -300 ###' \
    '### Protoss buildings: Pylons=0 Gateways=0 ###' "Can't find place for Pylon" > "$SELF_LOG"
  if EXPECT_MIN_INGAME_SECONDS=420 EXPECT_MIN_KILLED=12 EXPECT_MAX_KILLED=40 \
       EXPECT_MIN_PYLONS=1 EXPECT_MIN_GATEWAYS=1 EXPECT_MIN_RESOURCE_BALANCE=-200 \
       EXPECT_NO_PLACEMENT_FAILURES=1 bash scripts/assert-openbw-scenario.sh "$SELF_LOG"; then
    rm -f "$SELF_LOG"
    fail "scenario assertion self-test missed the broken fixture"
  fi
  rm -f "$SELF_LOG"
  say "scenario assertion self-test OK (valid fixture passes; broken fixture fails)"
  exit 0
fi

# Nested exits stay strictly ordered, and the host must OUTLIVE the client:
#   bot ends its own game  <  bot JVM killed  <  host killed
# The host is killed last on purpose (measured 2026-10-09: with the same value
# for both, the host died while the client was still starting), but everything
# must still fit inside TIMEOUT_SECONDS.
HOST_KILL_SECONDS="$TIMEOUT_SECONDS"
BOT_KILL_SECONDS=$(( TIMEOUT_SECONDS - 10 ))
if [ "$BOT_KILL_SECONDS" -lt 5 ]; then
  BOT_KILL_SECONDS=5
fi

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
  # OPENBW_PROBE=1 runs the engine capability survey (OpenBwCapabilityProbe):
  # every map/path/build query the bot uses, answered from a Probe's point of
  # view, printed as OPENBW_PROBE lines. Diagnostic only.
  if [ -n "${OPENBW_PROBE:-}" ]; then
    echo "OPENBW_PROBE=$OPENBW_PROBE" >> "$1"
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

if [ "$MEGA_TEST" = "1" ]; then
  say "runtime budget: ${TIMEOUT_SECONDS}s (MEGA_TEST=1 - the mega-test/full sweep ceiling)"
else
  say "runtime budget: ${TIMEOUT_SECONDS}s (single-test budget; a run that reaches it is stuck, not slow)"
fi
RUN_STARTED_AT=$(date +%s)

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

RUN_ELAPSED=$(( $(date +%s) - RUN_STARTED_AT ))
say "bot exit code: $BOT_EXIT"
say "runtime: ${RUN_ELAPSED}s of ${TIMEOUT_SECONDS}s budget"
say "server log: $SERVER_LOG"
say "bot log:    $BOT_LOG"

# A non-mega run that spends its whole budget did not "need more time": it was
# stuck (CONVENTIONS §17). Say so loudly, because the loudest failure here is a
# run that is quietly slow rather than one that is short and red.
if [ "$MEGA_TEST" != "1" ] && [ "$RUN_ELAPSED" -ge "$TIMEOUT_SECONDS" ]; then
  say "BUDGET EXHAUSTED: a single test used its whole ${TIMEOUT_SECONDS}s budget."
  say "  That is a stuck run (loop / hung host / broken path), not a slow one -"
  say "  inspect the logs above instead of re-running with a larger limit."
fi

if [ "$BOT_EXIT" -ne 0 ]; then
  say "bot did not exit cleanly; log tail:"
  tail -20 "$BOT_LOG" | sed 's/^/    /'
  # A timed-out or crashed bot is never a passing scenario, even when no
  # optional expectations were supplied. Preserve timeout's exit code rather
  # than printing SCENARIO PASSED from a partial GameSummary.
  exit "$BOT_EXIT"
fi
# === Scenario assertions ===================================================
# A run in which the client never attached is a failure, even without optional
# expectations. Otherwise an empty/logless run can look like a pass.
if ! grep -q 'HELLO_ATLANTIS' "$BOT_LOG" 2>/dev/null; then
  say "ASSERT FAILED: the bot never attached - no HELLO_ATLANTIS in $BOT_LOG"
  exit 1
fi
if ! bash "$ATLANTIS_DIR/scripts/assert-openbw-scenario.sh" "$BOT_LOG"; then
  say "SCENARIO FAILED (see $BOT_LOG)"
  exit 1
fi
if [ -n "${EXPECT_MIN_INGAME_SECONDS:-}${EXPECT_MIN_KILLED:-}${EXPECT_MAX_KILLED:-}${EXPECT_MIN_RESOURCE_BALANCE:-}${EXPECT_NO_PLACEMENT_FAILURES:-}" ]; then
  say "SCENARIO PASSED"
fi
exit "$BOT_EXIT"
