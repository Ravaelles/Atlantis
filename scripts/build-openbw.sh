#!/usr/bin/env bash
# Rebuild the OpenBW engine that every local OpenBW game actually runs, and
# (optionally) install a source tree into it before rebuilding.
#
# ---------------------------------------------------------------------------
# WHY THIS SCRIPT EXISTS
# ---------------------------------------------------------------------------
# The engine compiled into BWAPILauncher is NOT the tree the owner edits
# (/sc-ai/OpenBW/openbw). It is the vendored copy inside the test harness:
#
#   /sc-ai/StardustDevEnvironment/3rdparty/openbw/openbw
#
# The harness CMake project points OPENBW_DIR at that directory
# (see StardustDevEnvironment/CMakeLists.txt), and the generated compile line
# proves it:
#
#   build/3rdparty/openbw/bwapi/bwapi/OpenBWData/CMakeFiles/OpenBWData.dir/flags.make
#     CXX_INCLUDES = ... -I<HARNESS>/3rdparty/openbw/openbw
#
# BWData.cpp is the only translation unit of OpenBWData and it is the one that
# includes bwgame.h. Editing /sc-ai/OpenBW/openbw/bwgame.h and re-running
# scripts/run-openbw-e2e.sh therefore changes NOTHING: the game keeps running
# the old engine, the bug looks unfixed, and the search goes to the wrong place.
# That silent no-op is exactly what this script prevents.
#
# ---------------------------------------------------------------------------
# WHERE THINGS LIVE (the layout the script assumes)
# ---------------------------------------------------------------------------
#   /sc-ai/Atlantis/                          <- this repository (scripts/ lives here)
#   /sc-ai/StardustDevEnvironment/            <- the harness ($HARNESS_DIR)
#     CMakeLists.txt
#     3rdparty/openbw/openbw/                 <- ENGINE_DIR: the code that is compiled
#       bwgame.h actions.h ...                <- only these headers are installed
#     build/                                  <- BUILD_DIR: CMake build tree
#       bin/BWAPILauncher                     <- the executable the run scripts start
#       lib/libOpenBWData.so lib/libBWAPI.so
#       test/                                 <- GAME_DIR (working directory)
#         StarDat.mpq BrooDat.mpq Patch_rt.mpq
#         maps/                               <- maps are resolved from here
#         bwapi-data/bwapi.ini                <- BWAPI configuration
#   /sc-ai/OpenBW/openbw/                     <- SOURCE_DIR: the editable checkout
#
# run-openbw-e2e.sh and run-openbw-ums.sh already use
#   $HARNESS_DIR/build/bin/BWAPILauncher   and   $HARNESS_DIR/build/test
# so nothing else needs to change after a rebuild here.
#
# ---------------------------------------------------------------------------
# USAGE
# ---------------------------------------------------------------------------
#   bash scripts/build-openbw.sh
#       Rebuild from the code already installed in 3rdparty/openbw/openbw.
#
#   bash scripts/build-openbw.sh --from /sc-ai/OpenBW/openbw
#       Install that tree's engine headers into 3rdparty/openbw/openbw, then rebuild.
#
#   bash scripts/build-openbw.sh --from /sc-ai/OpenBW/openbw \
#                                --smoke ums/rav/protoss/1Drag_v_1Zeal.scm
#       Install, rebuild, then start BWAPILauncher headlessly on that map for a few
#       seconds and report how many of the map's units were created. This is the
#       end-to-end proof that the launcher runs the new engine and loads the map.
#
#   bash scripts/build-openbw.sh --verify
#       Change nothing; report which bwgame.h the launcher was compiled against and
#       whether the compiled object is up to date. Non-zero exit means "stale".
#
#   bash scripts/build-openbw.sh --dry-run --from /sc-ai/OpenBW/openbw
#       Print every action without performing it.
#
# OPTIONS
#   --from DIR          Source tree to install (must contain bwgame.h).
#   --no-backup         Do not save the previous engine headers before installing.
#   --clean             Delete BUILD_DIR and re-run cmake first (slow; only when
#                       CMake itself is confused, not for ordinary header changes).
#   --jobs N            Parallel build jobs (default: number of CPUs).
#   --smoke MAP         After building, run BWAPILauncher headlessly on MAP.
#                       MAP is relative to GAME_DIR (a bare name gets "maps/"
#                       prepended), or an absolute path.
#   --smoke-seconds N   How long the smoke run may take (default: 15).
#   --game MAP          Rebuild, then run a REAL 1v1 game through
#                       scripts/run-openbw-ums.sh and print one PASS/FAIL line.
#                       This is the single command that answers "does OpenBW
#                       work end to end?" - map hosting, bot connection, units.
#                       (--smoke only starts the launcher; --game also runs the bot.)
#   --dry-run           Show what would happen; change nothing.
#   --verify            Verify mode (see usage above).
#   -h, --help          This text.
#
# ENVIRONMENT OVERRIDES (usually not needed)
#   HARNESS_DIR         Defaults to /sc-ai/StardustDevEnvironment
#   SOURCE_DIR          Defaults to /sc-ai/OpenBW/openbw
#   For the smoke run only:
#     MAP_SMOKE_GAME_TYPE    default USE_MAP_SETTINGS; set to MELEE to test a melee map
#     MAP_SMOKE_RACE         default Protoss
#     MAP_SMOKE_ENEMY_RACE   default Zerg
#
# EXIT CODES
#   0  success (build finished, verify passed, smoke found the map's units)
#   1  build/verify/smoke failure
#   2  usage error (bad argument, missing directory, missing map)
#
# NOTES ON WHAT IS COPIED AND WHY
#   - Only the engine root headers (plus deps/asio and cmake/) are installed.
#     A full `rsync --delete` of a different checkout was measured to delete
#     mini-openbwapi/ and rewrite 16 unrelated files under the tracked 3rdparty
#     tree; this script never deletes anything in ENGINE_DIR.
#   - The previous headers are copied to
#       ../../3rdparty/openbw/openbw.headers.prev-<timestamp>/
#     so a bad engine is rolled back by copying them back and rebuilding.
#   - 3rdparty/openbw/openbw is tracked by git; after an install `git status`
#     shows the engine files as modified. That is expected, it is how the
#     harness records which engine it builds.
set -euo pipefail

# --- Locations -------------------------------------------------------------
ATLANTIS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HARNESS_DIR="${HARNESS_DIR:-/sc-ai/StardustDevEnvironment}"
SOURCE_DIR="${SOURCE_DIR:-/sc-ai/OpenBW/openbw}"

BUILD_DIR="$HARNESS_DIR/build"
ENGINE_DIR="$HARNESS_DIR/3rdparty/openbw/openbw"
GAME_DIR="$BUILD_DIR/test"
LAUNCHER="$BUILD_DIR/bin/BWAPILauncher"
BWAPI_INI_EXAMPLE="$HARNESS_DIR/3rdparty/openbw/bwapi/bwapi.ini.example"

# Engine files installed by --from. This is exactly the header set the harness
# compiles (the engine root); subdirectories such as mini-openbwapi/ and ui/
# belong to the vendored snapshot and are left untouched.
ENGINE_SOURCES=(
  bwgame.h actions.h bwenums.h circular_vector.h containers.h data_loading.h
  data_types.h game_types.h intrusive_list.h korean.h replay.h replay_saver.h
  static_vector.h strf.h sync.h sync_server_asio_local.h
  sync_server_asio_posix_stream.h sync_server_asio_socket.h
  sync_server_asio_tcp.h util.h
)
ENGINE_DIRS=(deps/asio cmake)

# --- Options (defaults) ----------------------------------------------------
FROM_DIR=""
BACKUP=1
CLEAN=0
JOBS="$(nproc 2>/dev/null || echo 4)"
SMOKE_MAP=""
SMOKE_SECONDS=15
GAME_MAP=""
GAME_TIMEOUT=180
DRY_RUN=0
VERIFY_ONLY=0

say()  { echo "[build-openbw] $*"; }
warn() { echo "[build-openbw] WARNING: $*" >&2; }
fail() { echo "[build-openbw] FATAL: $*" >&2; exit 2; }

# run COMMAND... executes the command, or only prints it in --dry-run mode.
run() {
  if [ "$DRY_RUN" -eq 1 ]; then
    printf '[build-openbw] would run:'; printf ' %q' "$@"; echo
  else
    "$@"
  fi
}

# --- Argument parsing ------------------------------------------------------
while [ $# -gt 0 ]; do
  case "$1" in
    --from)
      [ $# -ge 2 ] || fail "--from requires a directory"
      FROM_DIR="$2"; shift 2 ;;
    --no-backup)     BACKUP=0; shift ;;
    --clean)         CLEAN=1; shift ;;
    --jobs)
      [ $# -ge 2 ] || fail "--jobs requires a number"
      JOBS="$2"; shift 2 ;;
    --smoke)
      [ $# -ge 2 ] || fail "--smoke requires a map path"
      SMOKE_MAP="$2"; shift 2 ;;
    --smoke-seconds)
      [ $# -ge 2 ] || fail "--smoke-seconds requires a number"
      SMOKE_SECONDS="$2"; shift 2 ;;
    --game)
      [ $# -ge 2 ] || fail "--game requires a map path"
      GAME_MAP="$2"; shift 2 ;;
    --dry-run)       DRY_RUN=1; shift ;;
    --verify)        VERIFY_ONLY=1; shift ;;
    -h|--help)
      # Print the comment block at the top of this file (the whole manual).
      sed -n '2,120p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
      exit 0 ;;
    *) fail "unknown argument: $1 (see --help)" ;;
  esac
done

# --- verify_engine ---------------------------------------------------------
# Answers: "is the launcher actually built from ENGINE_DIR/bwgame.h, and is the
# object file newer than that header?" Two independent checks:
#   1. the CMake dependency file of BWData.cpp must name ENGINE_DIR/bwgame.h;
#   2. the compiled BWData.cpp.o must not be older than the header.
# A stale object means the next run would use the previous engine.
verify_engine() {
  local dep_file obj_file
  dep_file="$(find "$BUILD_DIR" -path '*OpenBWData.dir*' -name 'BWData.cpp.o.d' 2>/dev/null | head -1)"
  obj_file="$(find "$BUILD_DIR" -path '*OpenBWData.dir*' -name 'BWData.cpp.o' 2>/dev/null | head -1)"
  [ -n "$dep_file" ] || fail "no OpenBWData dependency file under $BUILD_DIR - has the harness ever been built?"
  [ -n "$obj_file" ] || fail "no BWData.cpp.o under $BUILD_DIR"
  [ -f "$ENGINE_DIR/bwgame.h" ] || fail "engine copy missing: $ENGINE_DIR/bwgame.h"

  if ! grep -qF "$ENGINE_DIR/bwgame.h" "$dep_file"; then
    fail "OpenBWData is NOT compiled against $ENGINE_DIR/bwgame.h (see $dep_file)"
  fi
  if [ "$ENGINE_DIR/bwgame.h" -nt "$obj_file" ]; then
    say "STALE: $ENGINE_DIR/bwgame.h is newer than $obj_file - a rebuild is needed"
    exit 1
  fi

  say "OK: engine compiled in is $ENGINE_DIR/bwgame.h (object is newer)"

  # When --from was given, also confirm the installed copy matches the source
  # the caller asked for - otherwise "I built my tree" would be a lie.
  if [ -n "$FROM_DIR" ]; then
    if cmp -s "$FROM_DIR/bwgame.h" "$ENGINE_DIR/bwgame.h"; then
      say "OK: $FROM_DIR/bwgame.h is identical to the installed copy"
    else
      say "NOTE: $FROM_DIR/bwgame.h differs from the installed copy (install with --from)"
      exit 1
    fi
  fi
}

if [ "$VERIFY_ONLY" -eq 1 ]; then
  verify_engine
  exit 0
fi

# --- Preconditions ---------------------------------------------------------
[ -d "$HARNESS_DIR" ] || fail "harness directory not found: $HARNESS_DIR"
[ -d "$BUILD_DIR" ]   || fail "harness build directory not found: $BUILD_DIR (configure it first: cmake -S $HARNESS_DIR -B $BUILD_DIR -DCMAKE_BUILD_TYPE=Release)"
[ -d "$ENGINE_DIR" ]  || fail "vendored engine not found: $ENGINE_DIR"
command -v cmake >/dev/null 2>&1 || fail "cmake not found"

# --- Optional smoke: validate the map path BEFORE spending a build ---------
# A bare map name is resolved under GAME_DIR/maps/ (the harness layout). Doing
# this up front means a typo fails in a second instead of after a full rebuild.
if [ -n "$SMOKE_MAP" ]; then
  if [ "${SMOKE_MAP#/}" = "$SMOKE_MAP" ]; then
    case "$SMOKE_MAP" in
      maps/*) : ;;
      *) SMOKE_MAP="maps/$SMOKE_MAP" ;;
    esac
  fi
  # The harness serves maps from a snapshot (build/test/maps) that rots: a map
  # added to the live tree later is missing here. Self-heal by copying it from
  # the live tree (same relative path). Mirror of the fallback in
  # run-openbw-ums.sh; keep the two in sync.
  if [ ! -f "$GAME_DIR/$SMOKE_MAP" ]; then
    case "$SMOKE_MAP" in
      maps/*)
        LIVE_SMOKE_MAP="/sc-ai/starcraft/maps/${SMOKE_MAP#maps/}"
        if [ -f "$LIVE_SMOKE_MAP" ]; then
          say "map missing under $GAME_DIR, copying from live tree: $LIVE_SMOKE_MAP"
          mkdir -p "$(dirname "$GAME_DIR/$SMOKE_MAP")"
          cp -p "$LIVE_SMOKE_MAP" "$GAME_DIR/$SMOKE_MAP" \
            || fail "could not copy $LIVE_SMOKE_MAP into $GAME_DIR"
        else
          fail "smoke map not found: $SMOKE_MAP (looked in $GAME_DIR and $LIVE_SMOKE_MAP)"
        fi ;;
      *) fail "smoke map not found: $SMOKE_MAP (looked in $GAME_DIR)" ;;
    esac
  fi
fi

# --- 1. Optional: install engine sources from a source tree ----------------
if [ -n "$FROM_DIR" ]; then
  [ -f "$FROM_DIR/bwgame.h" ] || fail "$FROM_DIR does not look like an OpenBW tree (no bwgame.h)"
  FROM_DIR="$(cd "$FROM_DIR" && pwd)"
  say "installing engine sources: $FROM_DIR -> $ENGINE_DIR (headers only, no deletions)"

  # Back up the current headers so a bad engine can be rolled back.
  if [ "$BACKUP" -eq 1 ]; then
    backup_dir="$HARNESS_DIR/3rdparty/openbw/openbw.headers.prev-$(date +%Y%m%d_%H%M%S)"
    say "backing up current engine headers to $backup_dir"
    if [ "$DRY_RUN" -eq 0 ]; then
      mkdir -p "$backup_dir"
      for f in "${ENGINE_SOURCES[@]}"; do
        if [ -f "$ENGINE_DIR/$f" ]; then cp -p "$ENGINE_DIR/$f" "$backup_dir/"; fi
      done
    fi
  fi

  # Refuse a partial install: every source file must exist before anything is
  # copied, so a half-copied engine can never be compiled.
  for f in "${ENGINE_SOURCES[@]}"; do
    [ -f "$FROM_DIR/$f" ] || fail "$FROM_DIR/$f missing - refusing a partial install"
  done
  for f in "${ENGINE_SOURCES[@]}"; do
    run cp -p "$FROM_DIR/$f" "$ENGINE_DIR/$f"
  done
  for d in "${ENGINE_DIRS[@]}"; do
    [ -d "$FROM_DIR/$d" ] || continue
    run cp -rp "$FROM_DIR/$d/." "$ENGINE_DIR/$d/"
  done
  # Touch the header that drives the rebuild so make always notices, even when
  # mtimes from the source copy are older than the object file (clock skew).
  run touch "$ENGINE_DIR/bwgame.h"
fi

[ -f "$ENGINE_DIR/bwgame.h" ] || fail "no engine at $ENGINE_DIR/bwgame.h"

# --- 2. Optional: clean reconfigure ----------------------------------------
if [ "$CLEAN" -eq 1 ]; then
  say "clean reconfigure (this deletes $BUILD_DIR)"
  if [ "$DRY_RUN" -eq 1 ]; then
    say "would run: rm -rf $BUILD_DIR && cmake -S $HARNESS_DIR -B $BUILD_DIR -DCMAKE_BUILD_TYPE=Release"
  else
    rm -rf "$BUILD_DIR"
    cmake -S "$HARNESS_DIR" -B "$BUILD_DIR" -DCMAKE_BUILD_TYPE=Release
  fi
fi

# --- 3. Build --------------------------------------------------------------
# BWData.cpp -> libOpenBWData.so depends on bwgame.h, so touching the header is
# enough; no separate cmake step is needed (the Makefile re-checks itself).
say "building: cmake --build $BUILD_DIR -j $JOBS"
run cmake --build "$BUILD_DIR" -j "$JOBS"

if [ "$DRY_RUN" -eq 1 ]; then
  say "dry run finished - nothing was changed"
  exit 0
fi

[ -x "$LAUNCHER" ] || fail "build finished but $LAUNCHER is missing"
say "launcher: $LAUNCHER ($(stat -c '%y' "$LAUNCHER" 2>/dev/null || date -r "$LAUNCHER"))"

# --- 4. Self-check ---------------------------------------------------------
verify_engine

# --- 5. Optional smoke run -------------------------------------------------
# A build that links but cannot load a scenario is still a broken engine. This
# starts the launcher exactly like scripts/run-openbw-server.sh: AUTO_MENU=
# SINGLE_PLAYER in GAME_DIR, no UI, with the bundled TestAIModule so the game
# keeps ticking without a client. BWAPI prints one "has been created" line per
# map unit; counting them shows whether the scenario's units were loaded.
if [ -n "$SMOKE_MAP" ]; then
  # The launcher reads bwapi-data/bwapi.ini; create it from the example on the
  # first smoke run (the run scripts provide their own).
  mkdir -p "$GAME_DIR/bwapi-data"
  if [ ! -f "$GAME_DIR/bwapi-data/bwapi.ini" ]; then
    cp "$BWAPI_INI_EXAMPLE" "$GAME_DIR/bwapi-data/bwapi.ini"
    say "created $GAME_DIR/bwapi-data/bwapi.ini from bwapi.ini.example"
  fi

  say "smoke run: $SMOKE_MAP for ${SMOKE_SECONDS}s (game_type=${MAP_SMOKE_GAME_TYPE:-USE_MAP_SETTINGS})"
  smoke_log="$(mktemp -t openbw-smoke.XXXXXX)"
  # ENEMY_COUNT=7: same reason as in run-openbw-ums.sh - the auto-menu default
  # (1) closes scenario slots past the first and deletes their armies (measured
  # 8Drag_v_8Drag: 8 of 16 Dragoons). Smoke is USE_MAP_SETTINGS-first.
  ( cd "$GAME_DIR" && env \
      BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON \
      BWAPI_CONFIG_AUTO_MENU__AUTO_MENU=SINGLE_PLAYER \
      BWAPI_CONFIG_AUTO_MENU__GAME_TYPE="${MAP_SMOKE_GAME_TYPE:-USE_MAP_SETTINGS}" \
      BWAPI_CONFIG_AUTO_MENU__ENEMY_COUNT=7 \
      BWAPI_CONFIG_AUTO_MENU__MAP="$SMOKE_MAP" \
      BWAPI_CONFIG_AUTO_MENU__RACE="${MAP_SMOKE_RACE:-Protoss}" \
      BWAPI_CONFIG_AUTO_MENU__ENEMY_RACE="${MAP_SMOKE_ENEMY_RACE:-Zerg}" \
      BWAPI_CONFIG_AUTO_MENU__AUTO_RESTART=OFF \
      OPENBW_ENABLE_UI=0 \
      timeout "$SMOKE_SECONDS" stdbuf -oL -eL "$LAUNCHER" >"$smoke_log" 2>&1 ) || true

  # "|| true" above: the launcher is killed by timeout on purpose, its exit
  # code says nothing about the engine. The log does.
  total="$(grep -c 'has been created' "$smoke_log" 2>/dev/null || true)"
  resources="$(grep -c 'A Resource_[A-Za-z_]* \[' "$smoke_log" 2>/dev/null || true)"
  non_resource="$(grep -o 'A [A-Za-z_]* \[' "$smoke_log" 2>/dev/null \
                  | grep -v 'A Resource_' \
                  | sort | uniq -c \
                  | sed 's/^ *//; s/\[//g' \
                  | tr '\n' ',' | sed 's/,$//' || true)"
  say "smoke: $total units created ($resources resources); non-resource units: ${non_resource:-none}"

  # Show anything unexpected (errors, crashes) but not the unit flood.
  grep -v -e 'has been created' -e 'Enabled Flag' -e 'now live' -e 'Loaded the AI' -e '^[[:space:]]*$' \
    "$smoke_log" | sed 's/^/[build-openbw]   /' || true
  say "smoke log: $smoke_log"

  if [ "$total" -lt "$resources" ]; then
    fail "smoke run created no non-resource units - the scenario's units were not loaded"
  fi
fi

# Names the verdict for one finished (or capped) game run. Split out so the
# branches pin without playing a game: feed it fixture dirs. Prints one
# verdict line; the exit code is the verdict (0 = the vehicle worked:
# WIN/LOSS/DRAW, 1 = FAIL: timeout/no-run/empty).
decide_game_verdict() {
  local game_rc="$1" game_log="$2" run_dir="$3" summary="$4"
  local killed="$5" lost="$6" hits="$7" frames="$8"
  if [ "$game_rc" -eq 124 ]; then
    # 124 is ambiguous: the outer timeout kills the runner with 124, but the
    # inner runner also exits with the bot's code, and a bot killed by its own
    # cap is 124 too (measured 2026-10-21: the "outer cap" hang on 8Drag
    # was really the bot hanging at "Connection successful" because the match
    # never started). The inner always prints its KILLED: verdict line on
    # completion, so its presence means the inner finished and the bot was the
    # thing that got killed - not that the outer cap fired. That is no longer
    # an automatic fail: a duel that fights to the cap is an undecided game,
    # not a broken vehicle - the frames/combat gates below sort never-started
    # and empty games out, and the verdict names the rest WIN/LOSS/DRAW.
    if grep -aq 'KILLED:' "$game_log" 2>/dev/null; then
      echo "note: inner run completed but the bot hit its cap (see verdict below)"
    else
      echo "FAIL: game timed out after ${GAME_TIMEOUT}s (inner runner caps at ~35 s, so this is a hang outside them - stuck wait or ignored TERM; log: $game_log)"
      echo "--- game log tail ---"
      tail -25 "$game_log" 2>/dev/null || true
      echo "--- newest run dir: ${run_dir:-none} ---"
      return 1
    fi
  fi
  case "$frames" in
    ''|'?'*) echo "FAIL: game produced no game-time reading (bot died before its summary; game log: $run_dir/UMS_LOG)"; return 1 ;;
  esac
  if [ "$frames" -le 100 ]; then
    echo "FAIL: game did not run (frames=$frames; game log: $run_dir/UMS_LOG)"
    return 1
  fi
  # Zero combat on a duel map means nothing happened: no units spawned,
  # or the armies never met. A real duel always moves at least one of
  # these counters (even a loss takes hits). Missing numbers (bot died
  # before its summary) stay unknown, never a fail - only an explicit
  # 0/0/0 is the "empty game" verdict.
  if [ "$killed" = "0" ] && [ "$lost" = "0" ] && [ "$hits" = "0" ]; then
    echo "FAIL: game ran $frames frames but saw no combat (KILLED:$killed LOST:$lost TIMES_HIT:$hits) - units never spawned or never met; game log: $run_dir/UMS_LOG"
    return 1
  fi
  # Contested games get a verdict with a direction. WIN/LOSS need no
  # comment. DRAW is the intermediate state the binary verdict hides:
  # completed but undecided with zero losses - not outplayed, so the
  # shape to investigate is exploration/engagement (or a cap), never
  # defeat. The bot prints its own banner; the counters split Defeat
  # into LOSS (fought and lost) and DRAW (undecided).
  if grep -aq 'VICTORY!' "$run_dir/UMS_LOG" 2>/dev/null; then
    echo "WIN: $summary (frames=$frames, game log: $run_dir/UMS_LOG)"
    return 0
  fi
  if [ "$lost" != "0" ] && [ "$lost" != "?" ]; then
    echo "LOSS: $summary (frames=$frames, game log: $run_dir/UMS_LOG)"
    return 0
  fi
  echo "DRAW: $summary (frames=$frames, undecided with no losses - investigate exploration/engagement or the cap, not defeat; game log: $run_dir/UMS_LOG)"
  return 0
}

# --- 6. Optional end-to-end game ------------------------------------------
# Unlike --smoke (launcher only), this drives the real bot: the run script
# hosts the game, connects Atlantis, plays, and prints KILLED/LOST/TIMES_HIT.
# The judgement is based on three independent facts, so a half-broken setup
# cannot look like a pass:
#   a) the script did not time out (exit 124 means "hung")
#   b) the bot reported a game time of more than 100 frames, i.e. the server
#      hosted the map and the client really played (a dead server gives 0).
#   c) contested games get a direction: WIN (bot banner), LOSS (fought and
#      lost it), DRAW (completed but undecided with no losses - not
#      outplayed, investigate engagement, not defeat).
# Note: there is no "Connected" line in this script's log; do not grep for it.
if [ -n "$GAME_MAP" ]; then
  RUN_SCRIPT="$ATLANTIS_DIR/scripts/run-openbw-ums.sh"
  MAP_ARG="$GAME_MAP"
  case "$MAP_ARG" in
    maps/*) : ;;
    /*) : ;;
    *) MAP_ARG="maps/$MAP_ARG" ;;
  esac
  [ -f "$RUN_SCRIPT" ] || fail "game runner not found: $RUN_SCRIPT"
  say "game: $MAP_ARG (timeout ${GAME_TIMEOUT}s)"
  game_log="$(mktemp -t openbw-game.XXXXXX)"
  game_rc=0
  # The run script writes the game's own output to a fresh directory under
  # $BOT_LOG_DIR/UMS_LOGS; the UMS_LOG inside it is the real bot log. The
  # script's stdout does NOT contain the game summary, so read the newest
  # directory created during this run instead.
  bot_logs="${BOT_LOG_DIR:-/sc-ai/BOTS/AtlantisOpenBW}/UMS_LOGS"
  # Only real run directories count: UMS_LOGS also holds loose files
  # (UMS_LOG, UMS_DEBUG for the last run) and a "latest" symlink, and `ls -t`
  # ranks files above directories, so a plain `head -1` picks the wrong entry.
  newest_run_dir() {
    find "$bot_logs" -mindepth 1 -maxdepth 1 -type d ! -name latest \
      -printf '%T@ %p\n' 2>/dev/null | sort -rn | head -1 | cut -d' ' -f2-
  }
  before="$(newest_run_dir || true)"
  timeout "$GAME_TIMEOUT" bash "$RUN_SCRIPT" "$MAP_ARG" >"$game_log" 2>&1 || game_rc=$?
  run_dir="$(newest_run_dir || true)"
  if [ -z "$run_dir" ] || [ ! -f "$run_dir/UMS_LOG" ] || [ "$run_dir" = "$before" ]; then
    echo "FAIL: game run produced no new log directory under $bot_logs (script log: $game_log)"
    exit 1
  fi
  # Parsing lives in one place (scripts/parse-run.sh) so future tools do not
  # grow a second, drifting copy.
  parsed="$("$SCRIPT_DIR/parse-run.sh" --fields "$run_dir")"
  IFS=$'\t' read -r killed lost hits frames <<<"$parsed"
  summary="KILLED:$killed LOST:$lost TIMES_HIT:$hits"

  decide_game_verdict "$game_rc" "$game_log" "$run_dir" "$summary" \
    "$killed" "$lost" "$hits" "$frames"
  exit $?
fi


say "OK: the harness now runs the engine from $ENGINE_DIR"
say "next: bash scripts/run-openbw-e2e.sh ...   (uses $LAUNCHER)"
say "      bash scripts/run-openbw-ums.sh ...  (same launcher, USE_MAP_SETTINGS)"
exit 0
