#!/bin/bash
# Play one OpenBW UMS micro map headlessly and report what happened.
#
# Usage:
#   bash scripts/run-openbw-ums.sh "maps/ums/rav/protoss/1Drag_v_1Zeal.scm" [debug]
#
#   map   : path under the harness maps dir, must contain `ums/`
#           (e.g. "maps/ums/rav/protoss/1Drag_v_1Zeal.scm").
#   debug : 0 (default) or 1. With 1 the bot writes the per-frame
#           builder trace (BUILDER_LOG=1) into the debug log.
#
# This is the micro-tuning loop, not a test: no expectations, no verdict
# gates, no ledger checks. It hosts the game with USE_MAP_SETTINGS (the
# scenario owns unit placement - no melee setup), attaches the bot, waits
# for the game to end itself, collects the logs and cleans up. Exit code
# is the bot's exit code. Compare scripts/run-openbw-e2e.sh (the assertion
# tier) and scripts/run-wine-full.sh (the owner's Wine vehicle).
#
# Hard limits: 30 s wall-clock (owner 2026-10-19: micro duels run at full
# engine speed, so a healthy duel ends in seconds - the cap only kills
# hangs; the 150 s CONVENTIONS §17 cap stays for the macro e2e tier),
# 20 game minutes self-end. Fixed, not overridable - a longer run needs
# the owner's explicit permission.

set -u

ATLANTIS_DIR="/sc-ai/Atlantis"
HARNESS_DIR="/sc-ai/StardustDevEnvironment"
GAME_DIR="$HARNESS_DIR/build/test"
SERVER_SCRIPT="$HARNESS_DIR/scripts/run-openbw-server.sh"
LAUNCHER="$HARNESS_DIR/build/bin/BWAPILauncher"
WINE_BOT_DIR="/sc-ai/BOTS/AtlantisP/AI"
BOT_RUN_DIR="/sc-ai/BOTS/AtlantisOpenBW/AI"
BOT_ROOT="$(dirname "$BOT_RUN_DIR")"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="/sc-ai/BOTS/AtlantisOpenBW/AI/Atlantis.jar"
UMS_LOGS_DIR="/sc-ai/BOTS/AtlantisOpenBW/UMS_LOGS"

TIMEOUT_SECONDS=30
INGAME_TIME=$((60 * 20))
# ENEMY_COUNT default 7, overridable like in run-openbw-e2e.sh (which defaults
# to 1 for melee). For UMS the default keeps every joinable slot alive; see the
# hosting comment below for why 1 amputates scenario armies.
ENEMY_COUNT="${ENEMY_COUNT:-7}"
HOST_KILL_SECONDS="$TIMEOUT_SECONDS"
BOT_KILL_SECONDS=$(( TIMEOUT_SECONDS - 10 ))
# -k on both timeouts: without kill-escalation a TERM-ignoring JVM (or a
# wedged launcher child) hangs `wait` forever and the only verdict is the
# outer 180 s timeout blaming the engine (measured 2026-10-20: --game on
# 1Drag hit the outer cap while every inner cap is 30 s or less).

say() { echo "[openbw-ums] $*"; }
fail() { echo "[openbw-ums] FATAL: $*" >&2; exit 1; }

MAP="${1:-}"
DEBUG="${2:-0}"
[ -n "$MAP" ] || fail "usage: run-openbw-ums.sh \"maps/ums/<map>.scm\" [debug]"
case "$MAP" in
  *ums/*) ;;
  *) fail "not a UMS map path (must contain ums/): $MAP" ;;
esac
[ "$DEBUG" = "0" ] || [ "$DEBUG" = "1" ] || fail "debug must be 0 or 1, got: $DEBUG"

# Teardown owns the whole lifecycle: a leftover host holds the game table
# and the next run's client adopts a dead PID (CHALLENGES/GameExecution.md).
# pkill -x, never -f: -f matches this shell's own command line.
cleanup() {
  pkill -9 -x BWAPILauncher 2>/dev/null || true
  rm -f /tmp/bwapi_socket_* 2>/dev/null || true
  return 0
}
trap cleanup EXIT INT TERM

# --- Preconditions ---
[ -x "$SERVER_SCRIPT" ] || fail "OpenBW server script not found: $SERVER_SCRIPT"
[ -x "$LAUNCHER" ] || fail "BWAPILauncher not built (cmake --build $HARNESS_DIR/build)"
[ -f "$JAR" ] || fail "bot jar not found: $JAR (build with scripts/build-bot-jar.sh)"
[ -d "$GAME_DIR" ] || fail "harness game dir not found: $GAME_DIR"
for mpq in StarDat.mpq BrooDat.mpq Patch_rt.mpq; do
  [ -f "$GAME_DIR/$mpq" ] || fail "missing $mpq in $GAME_DIR (harness not built?)"
done
# Fail fast on a bad map instead of burning every downstream timeout and
# blaming the engine: a typo here used to read as "server hung" 180 s later.
case "$MAP" in
  /*) [ -f "$MAP" ] || fail "map not found: $MAP" ;;
  *)  # The harness serves maps from a snapshot (build/test/maps) that rots: a
      # map added to the live tree later is missing here. Self-heal by copying
      # it from the live tree (same relative path) instead of failing. Mirror
      # of the fallback in build-openbw.sh (--smoke pre-check); keep in sync.
      if [ ! -f "$GAME_DIR/$MAP" ]; then
        LIVE_MAP="/sc-ai/starcraft/maps/${MAP#maps/}"
        if [ -f "$LIVE_MAP" ]; then
          say "map missing under $GAME_DIR, copying from live tree: $LIVE_MAP"
          mkdir -p "$(dirname "$GAME_DIR/$MAP")"
          cp -p "$LIVE_MAP" "$GAME_DIR/$MAP" \
            || fail "could not copy $LIVE_MAP into $GAME_DIR"
        else
          fail "map not found: $GAME_DIR/$MAP (pass a path under $GAME_DIR, e.g. maps/ums/rav/protoss/1Drag_v_1Zeal.scm)"
        fi
      fi ;;
esac
# -x matches the process NAME exactly. -f matches the whole command line, which
# also matched the shell running this script (its argv contains the pattern),
# producing a false "ChaosLauncher is running" fatal before any game started.
# Note the comm truncation trap (see kill-leftovers.sh): "ChaosLauncher.exe"
# is 17 chars, the kernel keeps 15, so -x can never match it - anchor -f on
# the exe path instead, both capitalizations (Wine reports either).
# Patterns are anchored to argv[0] (^...). An unanchored "/(name)" also matched
# this script's own `bash -c` line, whose text contains the path - measured
# 2026-10-20: the old pattern returned the calling shell plus the real process.
if pgrep -f '^([^ ]*/)?(StarCraft|ChaosLauncher|Chaoslauncher)\.exe( |$)' >/dev/null 2>&1; then
  fail "StarCraft/ChaosLauncher is running - kill it and retry."
fi

# The jar must match the working tree AND its build inputs (libs included):
# build-bot-jar.sh skips the build itself when the target jar is already
# fresh, so every caller gets the fast path without re-implementing the
# check here (a src-only check here once shipped a stale binding: the lib
# changed, the sources did not, and the game played the old jar).
# A play on a stale jar debugs a bug that is already fixed (2026-10-08).
timeout 120 bash "$ATLANTIS_DIR/scripts/build-bot-jar.sh" "$JAR" \
  || fail "bot jar build failed"
bash "$ATLANTIS_DIR/scripts/check-jar-freshness.sh" "$JAR" --quiet \
  || fail "rebuild did not produce a matching jar"

# --- Bot directory (GAME_LAUNCHER=OPENBW, never the Wine folder) ---
mkdir -p "$BOT_RUN_DIR"
cp "$JAR" "$BOT_RUN_DIR/Atlantis.jar"
BWAPI_DATA_ROOT="$ATLANTIS_DIR/bwapi-data"
[ -d "$BWAPI_DATA_ROOT/AI/build_orders" ] || fail "no build_orders under $BWAPI_DATA_ROOT"
write_env() {
  cat > "$1" <<EOF
LOCAL=true
GAME_LAUNCHER=OPENBW
BWAPI_DATA_PATH=$BWAPI_DATA_ROOT
FORCE_GG_FOR_ENEMY=false
FORCE_END_GAME_AFTER_REAL_SECONDS=$TIMEOUT_SECONDS
FORCE_END_GAME_AFTER_INGAME_SECONDS=$INGAME_TIME
EOF
  if [ "$DEBUG" = "1" ]; then
    echo "BUILDER_LOG=1" >> "$1"
  fi
}
write_env "$BOT_RUN_DIR/ENV"
mkdir -p "$BOT_ROOT/AI"
write_env "$BOT_ROOT/ENV"
write_env "$BOT_ROOT/AI/ENV"
if [ ! -e "$BOT_ROOT/AI/build_orders" ]; then
  ln -s "$BWAPI_DATA_ROOT/AI/build_orders" "$BOT_ROOT/AI/build_orders"
fi

# --- Run dir (one per run, never overwritten) ---
RUN_STAMP="$(date +%Y%m%d_%H%M%S)"
MAP_SLUG="$(basename "$MAP" | tr -c 'A-Za-z0-9._-' '_')"
RUN_DIR="$UMS_LOGS_DIR/${RUN_STAMP}_${MAP_SLUG}"
mkdir -p "$RUN_DIR"
ln -sfn "$RUN_DIR" "$UMS_LOGS_DIR/latest"
SERVER_LOG="$RUN_DIR/server.log"
BOT_LOG="$RUN_DIR/UMS_LOG"
DEBUG_LOG="$RUN_DIR/UMS_DEBUG"
cat > "$RUN_DIR/run.txt" <<EOF
map=$MAP debug=$DEBUG
timeout_seconds=$TIMEOUT_SECONDS ingame_time=$INGAME_TIME
jar=$JAR
EOF

# --- Host (scenario owns the setup: no melee unit spawn) ---
# Clean leftovers through the shared helper instead of ad-hoc pkill calls.
# The old `pgrep -f`/`pkill -f` forms matched this script's own command line and
# killed the calling shell; kill-leftovers.sh matches process NAMES exactly.
if [ -x "$SCRIPT_DIR/kill-leftovers.sh" ]; then
  "$SCRIPT_DIR/kill-leftovers.sh" >/dev/null 2>&1 || true
else
  pkill -9 -x BWAPILauncher 2>/dev/null || true
fi
rm -f /tmp/bwapi_socket_* /dev/shm/bwapi_shared_memory_* 2>/dev/null || true

# After cleanup there must be no launcher left and no game table. If there is,
# every later wait would just burn the timeout and then blame the engine.
# Failing here turns "sometimes hangs for 30 s" into one explained line.
if pgrep -x BWAPILauncher >/dev/null 2>&1; then
  say "preflight: BWAPILauncher still running after cleanup (pids: $(pgrep -x BWAPILauncher | tr '\n' ' '))"
  fail "cannot start a deterministic run while another launcher is alive"
fi
if [ -e /dev/shm/bwapi_shared_memory_game_list ]; then
  say "preflight: stale game table survived cleanup:"
  "$SCRIPT_DIR/bwapi-publish.sh" 2>&1 | sed 's/^/    /'
  rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*
  if [ -e /dev/shm/bwapi_shared_memory_game_list ]; then
    fail "game table is not removable (owned by another user?) - cannot host deterministically"
  fi
  say "preflight: stale game table removed, continuing"
fi
say "hosting UMS game: map=$MAP (USE_MAP_SETTINGS, scenario owns the setup)"
# ENEMY_COUNT=7: the scenario owns the setup. The auto-menu default (1) closes
# every EitherPreferComputer slot past the first one, deleting scenario armies
# placed on slots 2+ (measured 2026-10-21: 8Drag_v_8Drag spawned 8 of 16
# Dragoons and the match never started, the bot hung at "Connection
# successful"). 7 keeps all seven joinable slots; this script only hosts UMS
# maps (non-ums/ paths are refused above), so melee hosting is unaffected.
setsid nohup env BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON \
  BWAPI_CONFIG_AUTO_MENU__GAME_TYPE=USE_MAP_SETTINGS \
  BWAPI_CONFIG_AUTO_MENU__ENEMY_COUNT="$ENEMY_COUNT" \
  timeout -k 5 "$HOST_KILL_SECONDS" bash "$SERVER_SCRIPT" "$MAP" Protoss Zerg \
  >"$SERVER_LOG" 2>&1 </dev/null &
SERVER_WRAPPER_PID=$!

HOSTED=0
for _ in $(seq 1 60); do
  # A published game means a slot whose serverProcessID is a LIVE pid.
  # "The file is non-empty" is not enough: a crashed server leaves its pid in
  # the table, the client then fails with "No server proc ID", and the run
  # looks non-deterministic for no reason. bwapi-publish.sh checks /proc.
  if "$SCRIPT_DIR/bwapi-publish.sh" >/dev/null 2>&1; then
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
  say "no live game published within 30 s; why:"
  "$SCRIPT_DIR/bwapi-publish.sh" | sed 's/^/    /'
  say "server log tail:"
  tail -20 "$SERVER_LOG" | sed 's/^/    /'
  say "server process: $(pgrep -x BWAPILauncher >/dev/null && echo alive || echo dead)"
  fail "no live game registry within 30 s"
fi
say "hosted: $("$SCRIPT_DIR/bwapi-publish.sh" | tail -1)"
# The registry says "live" while the map itself may have failed to load
# (wrong mode, unreadable scenario): a published game with zero created
# units plays an empty map to every timeout downstream. Name it here.
if ! grep -q "has been created" "$SERVER_LOG" 2>/dev/null; then
  say "server published but created zero units - map $MAP did not load (wrong mode or unreadable scenario?); server log tail:"
  tail -20 "$SERVER_LOG" | sed 's/^/    /'
  fail "empty map load, not a hung server"
fi

# --- Client (cwd = bot root: build orders resolve from AI/build_orders/) ---
rm -f "$BOT_ROOT/bwapi-data/write/EVENTS.jsonl" "$BOT_ROOT/bwapi-data/write/BUILDINGS.json" "$BOT_ROOT/bwapi-data/write/DECISIONS.log"
SECONDS=0
say "starting bot (GAME_LAUNCHER=OPENBW)"
cd "$BOT_ROOT"
BOT_EXIT=0
timeout -k 5 "$BOT_KILL_SECONDS" java -jar "$BOT_RUN_DIR/Atlantis.jar" "--map=$MAP" >"$BOT_LOG" 2>"$DEBUG_LOG" &
BOT_PID=$!
wait "$BOT_PID" || BOT_EXIT=$?
say "bot finished, took ${SECONDS}s, exit code: $BOT_EXIT"

# --- Collect ---
[ -f "$BOT_ROOT/bwapi-data/write/EVENTS.jsonl" ] \
  && cp -f "$BOT_ROOT/bwapi-data/write/EVENTS.jsonl" "$RUN_DIR/EVENTS.jsonl" \
  && say "event log: $RUN_DIR/EVENTS.jsonl" \
  || say "no EVENTS.jsonl (bot warped nothing / died before first flush)"
[ -f "$BOT_ROOT/bwapi-data/write/BUILDINGS.json" ] \
  && cp -f "$BOT_ROOT/bwapi-data/write/BUILDINGS.json" "$RUN_DIR/BUILDINGS.json" \
  && say "ledger: $RUN_DIR/BUILDINGS.json" \
  || say "no BUILDINGS.json"
cp -f "$BOT_LOG" "$UMS_LOGS_DIR/UMS_LOG" 2>/dev/null || true
cp -f "$DEBUG_LOG" "$UMS_LOGS_DIR/UMS_DEBUG" 2>/dev/null || true
say "run dir: $RUN_DIR"
say "bot log tail:"
tail -15 "$BOT_LOG" | sed 's/^/    /'

# --- Verdict line: killed / lost / times hit, one line for the tuner ---
# Kills come from the bot's own end-of-game summary; hits taken are the
# HIT twin rows in the flushed event log (one row per landed hit on us).
# Missing values (bot died before the summary, no flush) read as "?"/0,
# never as zero kills - an inconclusive run must not look clean.
KILLED="?"
LOST="?"
SUMMARY_LINE="$(grep -a -m1 '### Units killed/lost:' "$BOT_LOG" 2>/dev/null || true)"
if [ -n "$SUMMARY_LINE" ]; then
  KILLED="$(echo "$SUMMARY_LINE" | sed -E 's|.*killed/lost: *([0-9]+)/.*|\1|')"
  LOST="$(echo "$SUMMARY_LINE" | sed -E 's|.*killed/lost: *[0-9]+/([0-9]+).*|\1|')"
fi
TIMES_HIT=0
[ -f "$RUN_DIR/EVENTS.jsonl" ] \
  && TIMES_HIT="$(grep -a -c '"key":"HIT"' "$RUN_DIR/EVENTS.jsonl" 2>/dev/null || echo 0)"
say "KILLED:$KILLED, LOST:$LOST, TIMES_HIT:$TIMES_HIT"
echo "killed=$KILLED lost=$LOST times_hit=$TIMES_HIT" >> "$RUN_DIR/run.txt"
exit "$BOT_EXIT"
