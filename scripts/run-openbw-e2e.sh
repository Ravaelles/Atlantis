#!/usr/bin/env bash

ATLANTIS_DIR="/sc-ai/Atlantis"
HARNESS_DIR="/sc-ai/StardustDevEnvironment"
GAME_DIR="$HARNESS_DIR/build/test"
SERVER_SCRIPT="$HARNESS_DIR/scripts/run-openbw-server.sh"
# The deployed bot tree. **Outside the repository on purpose** (owner's ruling, 2026-10-11):
# `Atlantis/bots/` held generated ENVs, copies of the jar and game-result files - all ignored
# by git - and duplicating the two things that must not drift (the jar and the ENV). The
# deployed bots live in /sc-ai/BOTS, which is the owner's shortcut and already contained
# AtlantisP; AtlantisOpenBW joined it in the same change.
WINE_BOT_DIR="/sc-ai/BOTS/AtlantisP/AI"

# Where per-run E2E logs go. Deliberately NOT under `_AI/`: that directory is the readable
# knowledge base (owner's ruling, 2026-10-11) and a growing pile of raw run logs makes it
# unreadable. The deployed bot tree already holds machine-local, git-ignored bulk, so the
# logs live next to the bot that produced them, with a stable `latest` symlink inside.
E2E_LOGS_DIR="/sc-ai/BOTS/AtlantisOpenBW/E2E_LOGS"

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
#   bash scripts/run-openbw-e2e.sh --short [map] [race] [enemy-race]   # opening gate
#   bash scripts/run-openbw-e2e.sh --long  [map] [race] [enemy-race]   # full economy
#   bash scripts/run-openbw-e2e.sh --short --cvis [map] [race] [enemy-race]
#       # opening gate + CherryVis trace (see --cvis below)
#   bash scripts/run-openbw-e2e.sh --self-test-cvis   # test cvis artifact checks, no game
#
# Hard limits (top of the file, CONVENTIONS §17): TIMEOUT_SECONDS=150 wall-clock,
# INGAME_TIME=20 game minutes. Both are refused when widened, never silently
# extended; the outer command must also cap the whole run at TIMEOUT_SECONDS.
#
# Wall-clock reporting (owner's request, 2026-10-11): the run prints
#   bot started playing at HH:MM:SS
#   bot finished playing at HH:MM:SS, took Ns
# and copies the finished logs to stable paths, $E2E_LOGS_DIR/E2E_LOG,
# E2E_DEBUG and E2E_DECISIONS, so "the last run" is one path triple that
# does not change. No last-server.log: the per-run server.log stays in its
# run directory, nobody reads a stable server copy.
# The copy is a snapshot with a one-line header, not a symlink: a symlink changes
# target under a reader who opened it mid-run.
#
# Scenario assertions (optional): game summary counters plus log checks.
#   EXPECT_MIN_INGAME_SECONDS=<n>   fail if the game ended earlier than n
#   EXPECT_MIN_KILLED=<n>           fail if we killed fewer units
#   EXPECT_MAX_KILLED=<n>           fail if we killed more units
#   EXPECT_MIN_PYLONS=<n>           fail if fewer Protoss Pylons exist at end
#   EXPECT_MIN_GATEWAYS=<n>         fail if fewer Protoss Gateways exist at end
#   EXPECT_MIN_ZEALOTS=<n>          fail if fewer Protoss Zealots exist at end
#   EXPECT_MIN_DRAGOONS=<n>         fail if fewer Protoss Dragoons exist at end
#   EXPECT_MIN_ASSIMILATORS=<n>     fail if fewer Protoss Assimilators exist at end
#   EXPECT_MIN_NEXUS=<n>            fail if fewer Protoss Nexus exist at end
#   EXPECT_MIN_RESOURCE_BALANCE=<n> fail if Resource killed/lost < n
#   EXPECT_NO_PLACEMENT_FAILURES=1  fail if "Can't find place" is logged
# When none is set the script only reports the verdict, as before.
#
# Two named build tiers, because "does the bot build" has two honest questions:
#
#   --short   the opening works: Cybernetics Core and the first Dragoon exist. The
#             bot exits ITSELF the moment that is true (CORE_AND_DRAGOON_GATE), so
#             the run is as long as the opening needs and no longer.
#   --long    the economy runs: >=5 Pylons, >=3 Gateways, >=1 Zealot, >=5 Dragoons,
#             >=2 Assimilators, >=3 Nexus at the end of a full-length game.
#
#   --cvis    also record a CherryVis trace for the run (GENERATE_CHERRYVIS_REPLAY
#             in the bot's ENV). The bot writes bwapi-data/write/cherryvis_dir.rep.cvis/
#             next to where it ran; this script copies it into the run directory
#             (and next to the .rep when combined with --save-replay, as
#             <name>.rep.cvis/, which is the layout the viewer opens) and FAILS the
#             run when the trace is missing - a silent no-trace would otherwise look
#             like a pass. Replays default to /sc-ai/REPLAYS.
#
# The numbers are scenario expectations (CONVENTIONS §10), not a game difficulty:
# they are the smallest structure set that proves production, tech and expansion all
# happened, and they are asserted at the END of the run against the GameSummary line.
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

# --save-replay is handled BEFORE the positional arguments are read: a flag left in
# `$1` would otherwise be taken as the map name ("Map not found: --save-replay").
#
# The engine already supports recording - AutoMenuManager reads the path from
# BWAPI_CONFIG_AUTO_MENU__SAVE_REPLAY (AutoMenuManager.cpp:141) and GameEvents writes
# the replay when the game ends (GameEvents.cpp:330) - so this needs no C++ change.
# The flag exists so a normal run does not litter the tree and so the output location
# has a stable name ("where is my replay?" should not be a question).
SAVE_REPLAY=0
for arg in "$@"; do
  [ "$arg" = "--save-replay" ] && SAVE_REPLAY=1
done
if [ "${1:-}" = "--save-replay" ]; then
  shift
fi

# Read the scenario flags BEFORE the positional arguments are stripped, then strip them,
# then read map/race from what is left. Two passes on purpose: the strip must not be the
# thing that decides the scenario (that inversion is what made `--short` a no-op on the
# first run, 2026-10-11 - `set --` removed the flag before the scan saw it).
SCENARIO=0
SHORT_GATE=0
CVIS=0
for arg in "$@"; do
  case "$arg" in
    --short) SCENARIO=1; SHORT_GATE=1 ;;
    --long)  SCENARIO=1 ;;
    --cvis)  CVIS=1 ;;
  esac
done

# Keep the original argv for the flags that are read AFTER the strip (--self-test*).
ORIGINAL_ARGS=("$@")

# A flag left in `$1` would be taken as the map name ("Map not found: --short") - the same
# trap the --save-replay handling above documents.
POSITIONAL=()
for arg in "$@"; do
  case "$arg" in
    --short|--long|--cvis|--save-replay) ;;
    *) POSITIONAL+=("$arg") ;;
  esac
done
set -- "${POSITIONAL[@]:-}"
REPLAY_DIR="/sc-ai/REPLAYS"

# Set here, not near the host command: write_env (which produces the ENV the bot reads)
# runs earlier in the script than the host launch, so a variable assigned next to the
# `setsid env ...` line never reaches the bot's ENV. Measured 2026-10-10 - the first
# attempt wrote the ENV without this key and the bot still killed its own JVM.
END_GAME_VIA_ENGINE=0
if [ "$SAVE_REPLAY" -eq 1 ]; then
  END_GAME_VIA_ENGINE=1
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

# Read from the ORIGINAL argv, before the positional strip removed the flags: the strip at
# the top of the file is what made `--self-test-assertions` a silent no-op for a while
# (measured 2026-10-11 - `$1` was already empty by the time this line read it).
SELF_TEST=0
ASSERTION_SELF_TEST=0
SELF_TEST_CVIS=0
for arg in "${ORIGINAL_ARGS[@]:-}"; do
  [ "$arg" = "--self-test" ] && SELF_TEST=1
  [ "$arg" = "--self-test-assertions" ] && ASSERTION_SELF_TEST=1
  [ "$arg" = "--self-test-cvis" ] && SELF_TEST_CVIS=1
done

# === Named build tiers (owner's ruling, 2026-10-11) ========================
# Two scenarios, because "does the bot build" has two honest answers. Both assert
# the end-of-game structure set; --short additionally stops the bot as soon as the
# opening is proven, so iterating on production does not cost a 20-minute game.
# (SCENARIO/SHORT_GATE are set above, before the positional strip.)
if [ "$SHORT_GATE" -eq 1 ]; then
  # The gate the bot exits on: a finished Cybernetics Core and at least one Dragoon.
  # Written into the bot's ENV (see write_env) as CORE_AND_DRAGOON_GATE=1.
  #
  # Fail-FAST clock (owner's ruling, 2026-10-11): the short tier gets 30 wall-clock
  # seconds, not 120. A working opening proves itself in well under that (the gate ends
  # the run itself), so 30 s is the SLOWEST this tier is allowed to be - and a run that
  # hits it has not proven the opening, which is a failure to report, not a run to wait
  # out. Overridable for a one-off debug run, never widened silently.
  TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-30}"
  # The gate's own condition, asserted at the end: a Core and a Dragoon. Without asserting
  # the Core, a run could reach the gate, lose the Core, and still pass on the Dragoon alone.
  EXPECT_MIN_DRAGOONS="${EXPECT_MIN_DRAGOONS:-1}"
  EXPECT_MIN_ZEALOTS="${EXPECT_MIN_ZEALOTS:-1}"
  EXPECT_MIN_CORES="${EXPECT_MIN_CORES:-1}"
  # A Dragoon costs gas, so an opening that proved Dragoons without proving the
  # refinery is broken by definition (measured: a run reporting Dragoon=3 with no
  # Assimilator ever built). Without this the tier passes gas-less phantoms.
  EXPECT_MIN_ASSIMILATORS="${EXPECT_MIN_ASSIMILATORS:-1}"
else
  # Full expectations for the long tier; overridable, never silently lowered.
  EXPECT_MIN_PYLONS="${EXPECT_MIN_PYLONS:-5}"
  EXPECT_MIN_GATEWAYS="${EXPECT_MIN_GATEWAYS:-3}"
  EXPECT_MIN_ZEALOTS="${EXPECT_MIN_ZEALOTS:-1}"
  EXPECT_MIN_DRAGOONS="${EXPECT_MIN_DRAGOONS:-5}"
  EXPECT_MIN_ASSIMILATORS="${EXPECT_MIN_ASSIMILATORS:-2}"
  EXPECT_MIN_NEXUS="${EXPECT_MIN_NEXUS:-3}"
fi

say() { echo "[openbw-e2e] $*"; }
fail() { echo "[openbw-e2e] ERROR: $*" >&2; exit 2; }

# === Hard simulation limits (CONVENTIONS §17) ==============================
# The two knobs live here, at the top, and nothing below may widen them.
#   TIMEOUT_SECONDS  wall-clock cap for the whole simulation.
#   INGAME_TIME      in-game seconds after which the bot ends the game itself
#                    (20 real minutes of game time).
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-150}"
INGAME_TIME="${INGAME_TIME:-$((60 * 20))}"
if [ "$TIMEOUT_SECONDS" -gt 150 ]; then
  fail "TIMEOUT_SECONDS=$TIMEOUT_SECONDS exceeds the OpenBW 150-second simulation cap (CONVENTIONS §17)"
fi
if [ "$TIMEOUT_SECONDS" -le 0 ]; then
  fail "TIMEOUT_SECONDS must be positive, got $TIMEOUT_SECONDS"
fi

if [ "$ASSERTION_SELF_TEST" -eq 1 ]; then
  SELF_LOG="$(mktemp)"
  # The fixture is in GameSummary's REAL format (`### Protoss: Pylon=1 Gateway=1 ###`),
  # not the old `Protoss buildings: Pylons=N` - the patterns must be exercised against
  # the text the bot actually prints, or this self-test would pass while the verdict
  # printed `?` (which is exactly what happened, 2026-10-11).
  printf '%s\n' '### Total time: 428 seconds. ###' \
    '### Units killed/lost: 12/3 ###' '### Resource killed/lost: +450 ###' \
    '### Protoss: Nexus=3 Probe=20 Pylon=6 Gateway=4 CyberneticsC=1 Zealot=2 Dragoon=5 Assimilator=2 ###' > "$SELF_LOG"
  # Explicitly required to succeed: under `set -e` a plain command's failure should exit,
  # but this one printed ASSERT FAILED and the script continued (measured 2026-10-11),
  # and a self-test that survives the case it exists to catch is not a self-test.
  if ! EXPECT_MIN_INGAME_SECONDS=420 EXPECT_MIN_KILLED=12 EXPECT_MAX_KILLED=40 \
       EXPECT_MIN_PYLONS=5 EXPECT_MIN_GATEWAYS=3 EXPECT_MIN_ZEALOTS=1 \
       EXPECT_MIN_DRAGOONS=5 EXPECT_MIN_ASSIMILATORS=2 EXPECT_MIN_NEXUS=3 \
       EXPECT_MIN_RESOURCE_BALANCE=-200 \
       bash scripts/assert-openbw-scenario.sh "$SELF_LOG"; then
    rm -f "$SELF_LOG"
    fail "scenario assertion self-test: the VALID fixture must pass"
  fi
  printf '%s\n' '### Total time: 428 seconds. ###' \
    '### Units killed/lost: 0/3 ###' '### Resource killed/lost: -300 ###' \
    '### Protoss: Nexus=1 Probe=5 Pylon=1 ###' "Can't find place for Pylon" > "$SELF_LOG"
  if EXPECT_MIN_INGAME_SECONDS=420 EXPECT_MIN_KILLED=12 EXPECT_MAX_KILLED=40 \
       EXPECT_MIN_PYLONS=5 EXPECT_MIN_GATEWAYS=3 EXPECT_MIN_DRAGOONS=5 \
       EXPECT_MIN_ASSIMILATORS=2 EXPECT_MIN_NEXUS=3 \
       EXPECT_MIN_RESOURCE_BALANCE=-200 \
       EXPECT_NO_PLACEMENT_FAILURES=1 bash scripts/assert-openbw-scenario.sh "$SELF_LOG"; then
    rm -f "$SELF_LOG"
    fail "scenario assertion self-test missed the broken fixture"
  fi
  rm -f "$SELF_LOG"
  say "scenario assertion self-test OK (valid fixture passes; broken fixture fails)"
  exit 0
fi

# What counts as a CherryVis trace: trace.json (plain, what the viewer opens),
# trace.json.zst (compressed copy), trace_raw.json (grep-able copy) and
# game_summary.json, all in one directory.
assert_cvis_artifacts() {
  local dir="$1" fail=0
  for f in trace.json trace.json.zst trace_raw.json game_summary.json; do
    if [ -s "$dir/$f" ]; then
      : ok
    else
      say "CVIS MISSING: $dir/$f absent or empty"
      fail=1
    fi
  done
  if ! grep -q 'units_first_seen' "$dir/trace.json" 2>/dev/null; then
    say "CVIS MISSING: no units_first_seen section in $dir/trace.json"
    fail=1
  fi
  return "$fail"
}

if [ "$SELF_TEST_CVIS" -eq 1 ]; then
  SELF_CVIS="$(mktemp -d)"
  mkdir -p "$SELF_CVIS/good" "$SELF_CVIS/bad"
  printf '{"units_first_seen":{"10":[{"id":1}]},"units_logs":{}}' > "$SELF_CVIS/good/trace.json"
  printf 'zstd-bytes' > "$SELF_CVIS/good/trace.json.zst"
  printf '{"units_first_seen":{"10":[{"id":1}]},"units_logs":{}}' > "$SELF_CVIS/good/trace_raw.json"
  printf '{"game_duration_frames": 100}' > "$SELF_CVIS/good/game_summary.json"
  printf '' > "$SELF_CVIS/bad/trace_raw.json"
  if ! assert_cvis_artifacts "$SELF_CVIS/good"; then
    rm -rf "$SELF_CVIS"
    fail "cvis self-test: the VALID fixture must pass"
  fi
  if assert_cvis_artifacts "$SELF_CVIS/bad"; then
    rm -rf "$SELF_CVIS"
    fail "cvis self-test missed the broken fixture"
  fi
  rm -rf "$SELF_CVIS"
  say "cvis artifact self-test OK (valid fixture passes; broken fixture fails)"
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
  # A --save-replay run needs the host to reach onGameEnd(), which is where the engine
  # writes the .rep. SIGKILLing it here would lose exactly the run being recorded, so in
  # that mode it is asked to stop first and given a moment; the -9 stays as the fallback
  # that prevents the stale-host problem GameExecution.md #4 describes.
  if [ "${SAVE_REPLAY:-0}" = "1" ]; then
    pkill -x BWAPILauncher 2>/dev/null || true
    for _ in $(seq 1 20); do
      pgrep -x BWAPILauncher >/dev/null 2>&1 || break
      sleep 0.25
    done
  fi
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
  bash scripts/check-jar-freshness.sh "$JAR" 2>&1 | sed 's/^/    /' || true
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
BOT_RUN_DIR="/sc-ai/BOTS/AtlantisOpenBW/AI"
mkdir -p "$BOT_RUN_DIR"
cp "$JAR" "$BOT_RUN_DIR/Atlantis.jar"

# The bot resolves build orders against BWAPI_DATA_PATH + AI/build_orders/, so
# this must point at a bwapi-data root that actually has them. Prefer the
# repository's own tree; fall back to a bot folder that has one.
BWAPI_DATA_ROOT="$ATLANTIS_DIR/bwapi-data"
if [ ! -d "$BWAPI_DATA_ROOT/AI/build_orders" ]; then
  for candidate in "$WINE_BOT_DIR/.." "/sc-ai/BOTS/AtlantisP"; do
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
# The bot ends its own game before the HOST's timeout kills the host (measured
# 2026-10-08: without this the host died first, the client was orphaned and
# looped on "No server proc ID" until the script SIGTERM'd it, so a played game
# still reported failure). ForceExitLocallyAfterRealSeconds calls
# Atlantis.onEnd -> System.exit(0), which is the only exit that leaves the host
# alive to be torn down cleanly. TIMEOUT_SECONDS (150, see the top of this file)
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
  if [ -n "${BUILDER_LOG:-}" ]; then
    echo "BUILDER_LOG=$BUILDER_LOG" >> "$1"
  fi
  if [ "${CVIS:-0}" = "1" ]; then
    # --cvis: the bot records its CherryVis trace for this run. Written only
    # when asked - the trace costs per-frame bookkeeping the normal tiers skip.
    echo "GENERATE_CHERRYVIS_REPLAY=true" >> "$1"
  fi
  if [ "${SHORT_GATE:-0}" = "1" ]; then
    # --short: end the game the moment the opening is PROVEN (a finished Cybernetics Core
    # and at least one Dragoon). Scoring that early turns the tier from "wait 20 minutes
    # and hope" into "does the opening get there at all", which is what iterating on
    # production needs. The bot owns the check (it can see its own buildings); the shell
    # only asserts the same facts at the end so a run that exits early without them fails.
    echo "CORE_AND_DRAGOON_GATE=true" >> "$1"
  fi
  if [ "${END_GAME_VIA_ENGINE:-0}" = "1" ]; then
    # Only written for a --save-replay run: the engine needs to outlive the bot long
    # enough to flush the replay, and the bot must leave the game instead of exiting.
    echo "END_GAME_VIA_ENGINE=true" >> "$1"
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
# === Log location (owner's report 2026-10-11: "the bot log disappeared") =====
# It used to be the single path `out/openbw/bot.log`, and `out/` is a BUILD directory:
# the next run truncated it and `rm -rf out` (which the test runner's own hygiene rule
# recommends before a definitive run) deleted it. So a log that mattered was gone exactly
# when it was wanted.
#
# Now every run writes into its own directory under $E2E_LOGS_DIR, named by
# timestamp + scenario, so:
#   * runs never overwrite each other;
#   * the directory is the deployed bot tree (owner's ruling, 2026-10-11), NOT `_AI/`:
#     `_AI/` is the readable knowledge base and per-run bulk does not belong in it;
#   * `latest` is a stable symlink inside that directory, so "where is the log?" has one
#     answer that does not change between runs.
RUN_STAMP="$(date +%Y%m%d_%H%M%S)"
RUN_TIER="adhoc"
if [ "$SHORT_GATE" -eq 1 ]; then RUN_TIER="short";
elif [ "$SCENARIO" -eq 1 ]; then RUN_TIER="long"; fi
RUN_TAG="${RUN_TIER}-${RACE}-vs-${ENEMY_RACE}"
RUN_DIR="$E2E_LOGS_DIR/${RUN_STAMP}_${RUN_TAG}"
mkdir -p "$RUN_DIR"
ln -sfn "$RUN_DIR" "$E2E_LOGS_DIR/latest"

# A previous --cvis run's trace sits in the bot's write dir; without clearing,
# a killed/crashed run would "collect" that stale trace and pass the artifact
# check on files it never produced (measured 2026-10-12).
if [ "${CVIS:-0}" = "1" ]; then
  rm -rf "$BOT_ROOT/bwapi-data/write/cherryvis_dir.rep.cvis"
fi
# The buildings ledger is rewritten by the bot on every warp; without
# clearing, a run that warps nothing would collect the previous run's rows.
rm -f "$BOT_ROOT/bwapi-data/write/BUILDINGS.json"
# Same for the structured event log (appended, never rewritten).
rm -f "$BOT_ROOT/bwapi-data/write/EVENTS.jsonl"
# Same for the audited production decisions (append-only FileWriter, never
# truncated - without clearing, every run collects all previous games:
# measured 2026-10-17, E2E_DECISIONS rows past the game's last frame with
# supply totals the game never reached, e.g. 200/200 in a 65-total game).

SERVER_LOG="$RUN_DIR/server.log"
BOT_LOG="$RUN_DIR/E2E_LOG"
DEBUG_LOG="$RUN_DIR/E2E_DEBUG"
mkdir -p "$(dirname "$SERVER_LOG")"

# The run's identity, written next to the log so a later reader does not have to guess
# which command produced it (the verdict table's whole purpose, _AI/e2e/README.md).
cat > "$RUN_DIR/run.txt" <<EOF
map=$MAP race=$RACE enemy_race=$ENEMY_RACE enemy_count=$ENEMY_COUNT
scenario=${RUN_TAG}
timeout_seconds=$TIMEOUT_SECONDS ingame_time=$INGAME_TIME
jar=$JAR
expect_min_pylons=${EXPECT_MIN_PYLONS:-} expect_min_gateways=${EXPECT_MIN_GATEWAYS:-} expect_min_zealots=${EXPECT_MIN_ZEALOTS:-}
expect_min_dragoons=${EXPECT_MIN_DRAGOONS:-} expect_min_assimilators=${EXPECT_MIN_ASSIMILATORS:-} expect_min_nexus=${EXPECT_MIN_NEXUS:-}
expect_min_cores=${EXPECT_MIN_CORES:-}
EOF

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
# --save-replay: the engine writes the .rep itself when the game ends
# (GameEvents.cpp:330, driven by AutoMenuManager's `save_replay` path), so all this
# does is give it a path. The file is written INTO the replay directory rather than
# copied afterwards, so a run that is killed (timeout, SIGKILL) still leaves whatever
# the engine had flushed - a copy step at the end would lose exactly the runs worth
# inspecting.
REPLAY_ENV=()
if [ "$SAVE_REPLAY" -eq 1 ]; then
  mkdir -p "$REPLAY_DIR"
  # The engine runs the configured path through strftime and then strips every
  # character that is not [A-Za-z0-9-_. /] (GameEvents.cpp:375-390), so a map name
  # with parentheses - `(3)TauCross1.1` - comes out mangled or empty and the file is
  # written somewhere unexpected. Use a plain ASCII name with no `%`/`$`/brackets.
  #REPLAY_NAME="atlantis_$(date +%Y-%m-%d_%H:%M:%S)_${RACE}_vs_${ENEMY_RACE}"
  REPLAY_NAME="atlantis_$(date +%Y-%m-%d_%H:%M:%S)_E2E"
  REPLAY_FILE="$REPLAY_DIR/$REPLAY_NAME.rep"
  # An array, not a string: `env` is exec'd directly (no shell between `setsid` and
  # `env`), so a "$VAR=value" string would be passed through literally and the setting
  # would never reach the process. Measured 2026-10-10.
  #
  # NOTE: the engine setting arrives, but a bounded run still produces NO .rep, because
  # the launcher kills the host before its game loop reaches onGameEnd() - the full
  # explanation and the three ways to fix it are in _AI/PLAN-OPENBW.md (
  # "--save-replay"). The flag is kept because it is correct as far as it goes and is
  # the half that needs no policy decision.
  REPLAY_ENV=(BWAPI_CONFIG_AUTO_MENU__SAVE_REPLAY="$REPLAY_FILE")
  # END_GAME_VIA_ENGINE (set at the top of the script, so write_env saw it) makes the
  # bot ask the engine to end the match instead of System.exit-ing out from under the
  # replay flush. See Env.endGameViaEngine.
  say "recording replay to: $REPLAY_FILE (bot will end the game via the engine)"
fi

if [ "$SAVE_REPLAY" -eq 1 ]; then
  # Two separate invocations rather than one with a possibly-empty array: quoting
  # "${REPLAY_ENV[@]}" on an EMPTY array passes a single empty argument to `env`,
  # which it rejects ("env: '': No such file or directory") - i.e. enabling nothing
  # would break the default run.
  setsid nohup env BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON \
    BWAPI_CONFIG_AUTO_MENU__ENEMY_COUNT="$ENEMY_COUNT" \
    "${REPLAY_ENV[@]}" \
    timeout "$HOST_KILL_SECONDS" bash "$SERVER_SCRIPT" "$MAP" "$RACE" "$ENEMY_RACE" \
    >"$SERVER_LOG" 2>&1 </dev/null &
else
  setsid nohup env BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON \
    BWAPI_CONFIG_AUTO_MENU__ENEMY_COUNT="$ENEMY_COUNT" \
    timeout "$HOST_KILL_SECONDS" bash "$SERVER_SCRIPT" "$MAP" "$RACE" "$ENEMY_RACE" \
    >"$SERVER_LOG" 2>&1 </dev/null &
fi
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

# === Timing (owner's request, 2026-10-11) ==================================
# When the run started and stopped in WALL-CLOCK terms, and how long it took.
# `SECONDS` is bash's own counter and cannot be affected by the clock shifting,
# so the duration is exact even if the machine's time steps mid-run.
SECONDS=0
BOT_STARTED_AT="$(date +%H:%M:%S)"
say "starting bot: $BOT_RUN_DIR (headless, GAME_LAUNCHER=OPENBW)"
say "bot started playing at $BOT_STARTED_AT"
# cwd = the bot ROOT, because the build-order path is relative to the working
# directory and is "AI/build_orders/" (see the link section above).
cd "$BOT_ROOT"
BOT_EXIT=0
# `--map=` wins over Main's hard-coded map choices
# (ActiveMap.readMapFromCliArgument), so the map this script hosts and the map
# the bot analyses are the same one. Without it the bot's own default was used
# while the harness hosted something else (measured 2026-10-08: the bot analysed
# a UMS map and could place nothing on the real one).
timeout "$BOT_KILL_SECONDS" java -jar "$BOT_RUN_DIR/Atlantis.jar" "--map=$MAP" >"$BOT_LOG" 2>"$DEBUG_LOG" &
BOT_PID=$!
# Streams stay split on purpose: stdout is the game log (HELLO, GameSummary,
# build orders, placements), stderr the diagnostics (worker gates, stuck
# workers, refusal breakdowns, stack traces). Merging them back is how the
# main log drowned in thousands of diagnostic lines; EVENTS.jsonl carries
# the queryable twin of the hot families.
# Hang catcher (NEXT #69): a run whose teardown hung died between the trace write
# and System.exit with no frame to name. While the bot runs, a background poll
# watches log growth: if the java child is alive but the log has not grown for
# 30 s, its stacks are dumped once to the run dir - then the watcher gets out
# of the way (the 15 s backstop / the outer timeout still end the run).
# Silent unless it fires; skipped entirely when jstack is missing, and on
# END_GAME_VIA_ENGINE runs: after leaveGame the bot is supposed to linger (the
# JNativeHook hook thread holds the JVM) while the launcher flushes the replay,
# so the log stalls by design - three such runs each dumped a false-positive
# jstack (main+client gone, only the hook thread alive, .rep flushed fine).
HANG_WATCHER_PID=""
ENGINE_PATH_END=0
grep -q '^END_GAME_VIA_ENGINE=true$' "$BOT_RUN_DIR/ENV" 2>/dev/null && ENGINE_PATH_END=1
if command -v jstack >/dev/null 2>&1 && [ "$ENGINE_PATH_END" -eq 0 ]; then
  (
    i=0
    while [ "$i" -lt 9 ]; do
      sleep 10
      i=$(( i + 1 ))
      BOT_JAVA_PID="$(pgrep -P "$BOT_PID" 2>/dev/null | head -1)"
      if [ -n "$BOT_JAVA_PID" ] && kill -0 "$BOT_JAVA_PID" 2>/dev/null \
        && [ $(( $(date +%s) - $(stat -c %Y "$BOT_LOG" 2>/dev/null || echo 0) )) -gt 30 ]; then
        jstack -l "$BOT_JAVA_PID" > "$RUN_DIR/jstack-hang.txt" 2>&1 || true
        break
      fi
    done
  ) &
  HANG_WATCHER_PID=$!
fi
wait "$BOT_PID" || BOT_EXIT=$?
[ -n "$HANG_WATCHER_PID" ] && kill "$HANG_WATCHER_PID" 2>/dev/null || true

BOT_TOOK_SECONDS="$SECONDS"
BOT_FINISHED_AT="$(date +%H:%M:%S)"
say "bot finished playing at $BOT_FINISHED_AT, took ${BOT_TOOK_SECONDS}s"
say "bot exit code: $BOT_EXIT"
say "server log: $SERVER_LOG"
say "bot log:    $BOT_LOG"

# NOTE for the result block below: `ForceExitLocallyInOpenBW` (owner's, added
# 2026-10-11) ends an OpenBW game the moment we hold >= 700 minerals - a deliberate
# "if we are hoarding, the run is broken" tripwire. So a run that reports
# "Prevented too long game ... min:7xx" STOPPED ITSELF, and its `E2E FAIL` means the
# success gate was not reached before the tripwire fired - not that the runner broke.
# Look for that line before concluding a short game was a crash.

# === The last run, at stable paths (owner's request, 2026-10-11) ==========
# A copy, not a symlink: the run directory is timestamped and a symlink would
# change target under a reader who opened it mid-run, while a copy is a snapshot
# of one specific run. Written after every run, including a failed one - a crash
# is exactly when the file is wanted. NOTE: this runs after the collection
# below - E2E_DECISIONS only exists once collected; copying before collection
# silently copied nothing, which is how the stable decisions file went missing.

# Buildings ledger collection (always on): the bot rewrites
# bwapi-data/write/BUILDINGS.json on every warp, so an interrupted run
# still leaves its rows. Copy next to the logs; a missing file means the
# bot never warped a building (or died before the first one).
if [ -f "$BOT_ROOT/bwapi-data/write/BUILDINGS.json" ]; then
  cp -f "$BOT_ROOT/bwapi-data/write/BUILDINGS.json" "$RUN_DIR/BUILDINGS.json" 2>/dev/null || true
  say "buildings ledger collected: $RUN_DIR/BUILDINGS.json"
else
  say "buildings ledger missing: $BOT_ROOT/bwapi-data/write/BUILDINGS.json was never written"
fi

# Structured event log (same shape: appended by the bot, flushed ~30 s).
if [ -f "$BOT_ROOT/bwapi-data/write/EVENTS.jsonl" ]; then
  cp -f "$BOT_ROOT/bwapi-data/write/EVENTS.jsonl" "$RUN_DIR/EVENTS.jsonl" 2>/dev/null || true
  say "event log collected: $RUN_DIR/EVENTS.jsonl (query: scripts/query-events.py --file $RUN_DIR/EVENTS.jsonl)"
else
  say "event log missing: $BOT_ROOT/bwapi-data/write/EVENTS.jsonl was never written"
fi
if [ -f "$BOT_ROOT/bwapi-data/write/DECISIONS.log" ]; then
  cp -f "$BOT_ROOT/bwapi-data/write/DECISIONS.log" "$RUN_DIR/E2E_DECISIONS" 2>/dev/null || true
  say "decisions log collected: $RUN_DIR/E2E_DECISIONS"
else
  say "decisions log missing: $BOT_ROOT/bwapi-data/write/DECISIONS.log was never written"
fi

# Stable "last run" copies (after collection, never before - see note above).
cp -f "$BOT_LOG" "$E2E_LOGS_DIR/E2E_LOG" 2>/dev/null || true
cp -f "$DEBUG_LOG" "$E2E_LOGS_DIR/E2E_DEBUG" 2>/dev/null || true
cp -f "$RUN_DIR/E2E_DECISIONS" "$E2E_LOGS_DIR/E2E_DECISIONS" 2>/dev/null || true

# A one-line header so the copy explains itself: a reader who finds
# E2E_LOG days later needs to know which run it was and when it played.
{
  echo "# last E2E run: $RUN_TIER | $MAP | $RACE vs $ENEMY_RACE | started $BOT_STARTED_AT, finished $BOT_FINISHED_AT, took ${BOT_TOOK_SECONDS}s, exit $BOT_EXIT"
  echo "# full run: $RUN_DIR"
  echo "#"
  cat "$E2E_LOGS_DIR/E2E_LOG" 2>/dev/null || true
} > "$E2E_LOGS_DIR/E2E_LOG.tmp" 2>/dev/null && mv -f "$E2E_LOGS_DIR/E2E_LOG.tmp" "$E2E_LOGS_DIR/E2E_LOG" 2>/dev/null || true

# === CherryVis trace collection (only with --cvis) ==========================
# The bot writes bwapi-data/write/cherryvis_dir.rep.cvis/ next to where it ran
# (BOT_ROOT, its working directory). A trace left there is machine-local dust;
# copy it into the run directory (durable, next to the logs) and, when the run
# also recorded a replay, next to the .rep as <name>.rep.cvis/ - the layout the
# viewer opens. A --cvis run with no trace is a failure, not a pass.
if [ "${CVIS:-0}" = "1" ]; then
  CVIS_SRC="$BOT_ROOT/bwapi-data/write/cherryvis_dir.rep.cvis"
  CVIS_DEST="$RUN_DIR/cvis"
  mkdir -p "$CVIS_DEST"
  if [ -d "$CVIS_SRC" ]; then
    cp -f "$CVIS_SRC"/trace.json "$CVIS_SRC"/trace.json.zst "$CVIS_SRC"/trace_raw.json "$CVIS_SRC"/game_summary.json "$CVIS_DEST/" 2>/dev/null || true
    say "cherryvis trace collected: $CVIS_DEST"
  else
    say "cherryvis trace missing: $CVIS_SRC was never written (was GENERATE_CHERRYVIS_REPLAY seen by the bot?)"
  fi
  if [ "${SAVE_REPLAY:-0}" = "1" ] && [ -n "${REPLAY_FILE:-}" ]; then
    # Pair with the ACTUAL .rep on disk, not the configured name: the engine
    # runs the path through strftime and strips every char outside
    # [A-Za-z0-9-_. /] (measured: colons vanish, so REPLAY_FILE never exists
    # and a cvis dir named after it would sit next to nothing). Newest .rep wins.
    ACTUAL_REP="$(ls -t "$REPLAY_DIR"/*.rep 2>/dev/null | head -1 || true)"
    if [ -n "$ACTUAL_REP" ]; then
      mkdir -p "$ACTUAL_REP.cvis"
      cp -f "$CVIS_DEST"/trace.json "$CVIS_DEST"/trace.json.zst "$CVIS_DEST"/trace_raw.json "$CVIS_DEST"/game_summary.json "$ACTUAL_REP.cvis/" 2>/dev/null || true
      say "cherryvis trace next to replay: $ACTUAL_REP.cvis"
    else
      say "cherryvis trace has no .rep to pair with (no .rep in $REPLAY_DIR)"
    fi
  fi
  if ! assert_cvis_artifacts "$CVIS_DEST"; then
    # Do not fail here: when the bot was killed or crashed, onGameEnd never ran
    # and a trace cannot exist by design (measured: a run killed at the bot-cap
    # mid-opening). Record the miss; the gate below fails it on a clean run.
    CVIS_MISSING=1
    say "CVIS MISSING: trace artifacts absent - see paths above"
  else
    CVIS_MISSING=0
    say "CVIS OK: trace artifacts present"
  fi
fi

# === The result block ======================================================
# Printed on EVERY exit path from here on, pass or fail (owner's ruling 2026-10-11): the
# tier, whether the run stopped itself at the success gate, the full roster, and the
# reason for a failure. A verdict that only appears on success teaches nothing from the
# runs that matter - and the runs that matter are the failures.
print_result_block() {
  local status="$1"       # OK | FAIL
  local detail="$2"
  echo "============================================================"
  echo "E2E $status  [$RUN_TIER]  $detail"
  echo "  map  : $MAP"
  echo "  time : started $BOT_STARTED_AT, finished $BOT_FINISHED_AT, took ${BOT_TOOK_SECONDS}s"
  echo "  log  : $BOT_LOG"
  echo "  last : $E2E_LOGS_DIR/E2E_LOG"
  echo "============================================================"
}

if [ "$BOT_EXIT" -ne 0 ]; then
  say "bot did not exit cleanly; log tail:"
  tail -20 "$BOT_LOG" | sed 's/^/    /'
  # A timed-out or crashed bot is never a passing scenario, even when no
  # optional expectations were supplied. Preserve timeout's exit code rather
  # than printing SCENARIO PASSED from a partial GameSummary.
  if [ "$BOT_EXIT" = "124" ]; then
    print_result_block "FAIL" "bot hit the ${BOT_KILL_SECONDS}s bot-cap (TIMEOUT_SECONDS=${TIMEOUT_SECONDS}) without reaching the success gate"
  else
    print_result_block "FAIL" "bot exited with code $BOT_EXIT (crash or unclean exit)"
  fi
  exit "$BOT_EXIT"
fi
# A run in which the client never attached is a failure, even without optional
# expectations. Otherwise an empty/logless run can look like a pass.
if ! grep -q 'HELLO_ATLANTIS' "$BOT_LOG" 2>/dev/null; then
  say "ASSERT FAILED: the bot never attached - no HELLO_ATLANTIS in $BOT_LOG"
  print_result_block "FAIL" "the bot never attached (no HELLO_ATLANTIS in the log)"
  exit 1
fi
# === Scenario assertions ===================================================
# A run in which the client never attached is a failure, even without optional
# expectations. Otherwise an empty/logless run can look like a pass.
# (The marker above is asserted by LauncherBuildsTheJarTest: it checks that the preserved
# bot exit code comes before this section, so a crash can never reach a passing assertion.
# Keep the exact text `# === Scenario assertions`.)

# The EXPECT_* variables MUST be exported: the assertion script is a child process, and an
# unexported shell variable is invisible to it - which is exactly why every real run printed
# a verdict and then SCENARIO PASSED regardless of the numbers (measured 2026-10-11: a
# `--short` run with dragoons=0 passed its `EXPECT_MIN_DRAGOONS=1`). The inline `VAR=... bash`
# form used by --self-test-assertions hid it, because inline assignments ARE inherited.
export EXPECT_MIN_INGAME_SECONDS EXPECT_MIN_KILLED EXPECT_MAX_KILLED \
  EXPECT_MIN_PYLONS EXPECT_MIN_GATEWAYS EXPECT_MIN_ZEALOTS EXPECT_MIN_DRAGOONS \
  EXPECT_MIN_ASSIMILATORS EXPECT_MIN_NEXUS EXPECT_MIN_CORES EXPECT_MIN_RESOURCE_BALANCE \
  EXPECT_NO_PLACEMENT_FAILURES 2>/dev/null || true

# The gate the bot stops on (when armed) is reported separately from the assertions: a run
# can reach the gate and still be missing something the shell expects, and saying which is
# which is the difference between "the opening never got there" and "it got there but the
# building set is incomplete".
GATE_MARK=""
if grep -q 'ForceExitWhenOpeningProven' "$BOT_LOG" 2>/dev/null; then
  GATE_MARK="success gate reached (Core + Dragoon)"
fi

if ! bash "$ATLANTIS_DIR/scripts/assert-openbw-scenario.sh" "$BOT_LOG" "$DEBUG_LOG"; then
  say "SCENARIO FAILED (see $BOT_LOG)"
  print_result_block "FAIL" "${GATE_MARK:-success gate NOT reached} - an expected structure/unit is missing (see reasons above)"
  exit 1
fi
# A --cvis run whose bot exited cleanly must have left a trace: onGameEnd writes
# it on every clean exit, so absence here means the writer never ran, not that
# the game was short. (A killed/crashed bot is already failed above.)
if [ "${CVIS:-0}" = "1" ] && [ "${CVIS_MISSING:-0}" = "1" ]; then
  say "SCENARIO FAILED: --cvis asked for a trace and none was written (see $BOT_LOG)"
  print_result_block "FAIL" "cherryvis trace missing on a clean run"
  exit 1
fi
say "SCENARIO PASSED"
say "  log: $BOT_LOG"
print_result_block "OK" "${GATE_MARK:-success gate not armed - full expectations met at game end}"
exit "$BOT_EXIT"
