#!/usr/bin/env bash
# End-to-end check that OpenBW hosts a game correctly, without a bot.
#
# WHY
#   Three separate regressions lived in the server/client pump and none of them
#   was caught by a test, because checking the handshake was a manual step:
#     1. the connect handshake byte was only sent from a frame that could not
#        happen before the client attached (deadlock),
#     2. the game data segment was unlinked right after accept (client saw an
#        empty player table),
#     3. MatchStart was published into a queue that was cleared before anyone
#        could read it.
#   This script starts the real launcher, drives the handshake with
#   tools/bwapi-handshake-probe, and fails unless the probe reports a working
#   handshake AND a ticking match AND MatchStart AND a populated player table.
#
# WHAT IT CHECKS (all four must pass)
#   BYTE_2_OK        server sends the go-ahead byte
#   MATCH_TICKING    frameCount advanced while attached
#   MatchStart=YES   the client-visible event queue contains MatchStart
#   players=N/12     the player table is published (N > 0)
#
# Usage:
#   scripts/test-openbw-host.sh                       # default UMS map
#   scripts/test-openbw-host.sh --map ums/rav/terran/M&M_v_ZealDrag.scx
#   scripts/test-openbw-host.sh --game-type MELEE
#
# Requirements: the harness must be built (build/bin/BWAPILauncher) and the
# probe source must compile (plain cc, no deps).
#
# Exit codes: 0 pass, 1 check failed, 2 setup problem (no launcher, no probe).
set -uo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="$(cd "$SCRIPT_DIR/../tools" && pwd)"
HARNESS_DIR="${HARNESS_DIR:-/sc-ai/StardustDevEnvironment}"
LAUNCHER="$HARNESS_DIR/build/bin/BWAPILauncher"
GAME_DIR="$HARNESS_DIR/build/test"
MAP="maps/ums/rav/terran/M&M_v_ZealDrag.scx"
GAME_TYPE="${MAP_SMOKE_GAME_TYPE:-USE_MAP_SETTINGS}"
RACE="${MAP_SMOKE_RACE:-Protoss}"
ENEMY_RACE="${MAP_SMOKE_ENEMY_RACE:-Zerg}"
HOST_TIMEOUT=25

while [ $# -gt 0 ]; do
  case "$1" in
    --map)       MAP="$2"; shift 2 ;;
    --game-type) GAME_TYPE="$2"; shift 2 ;;
    --timeout)   HOST_TIMEOUT="$2"; shift 2 ;;
    -h|--help)   sed -n '2,32p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

say()  { echo "[test-openbw-host] $*"; }
fail() { echo "[test-openbw-host] FAIL: $*"; exit 1; }

[ -x "$LAUNCHER" ] || { echo "[test-openbw-host] setup: no launcher at $LAUNCHER" >&2; exit 2; }
[ -f "$GAME_DIR/$MAP" ] || { echo "[test-openbw-host] setup: no map $GAME_DIR/$MAP" >&2; exit 2; }
command -v cc >/dev/null 2>&1 || { echo "[test-openbw-host] setup: cc not found" >&2; exit 2; }

# Build the probe from source every run: a stale binary would make the test lie.
PROBE="$(mktemp -t bwapi-probe.XXXXXX)"
trap 'rm -f "$PROBE"' EXIT
cc -O2 -o "$PROBE" "$TOOLS_DIR/bwapi-handshake-probe.c" || { echo "[test-openbw-host] setup: probe build failed" >&2; exit 2; }

# Clean slate: a leftover launcher or a stale game table makes the result
# meaningless (the client would attach to somebody else's dead game).
if [ -x "$SCRIPT_DIR/kill-leftovers.sh" ]; then
  "$SCRIPT_DIR/kill-leftovers.sh" >/dev/null 2>&1 || true
else
  pkill -9 -x BWAPILauncher 2>/dev/null || true
fi
rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_* 2>/dev/null || true

say "hosting $MAP ($GAME_TYPE)"
log="$(mktemp -t openbw-host.XXXXXX)"
( cd "$GAME_DIR" && env \
    BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON \
    BWAPI_CONFIG_AUTO_MENU__AUTO_MENU=SINGLE_PLAYER \
    BWAPI_CONFIG_AUTO_MENU__GAME_TYPE="$GAME_TYPE" \
    BWAPI_CONFIG_AUTO_MENU__MAP="$MAP" \
    BWAPI_CONFIG_AUTO_MENU__RACE="$RACE" \
    BWAPI_CONFIG_AUTO_MENU__ENEMY_RACE="$ENEMY_RACE" \
    BWAPI_CONFIG_AUTO_MENU__AUTO_RESTART=OFF \
    OPENBW_ENABLE_UI=0 \
    timeout "$HOST_TIMEOUT" "$LAUNCHER" >"$log" 2>&1 ) &
host_pid=$!

# Wait for the listening socket the probe needs.
ready=0
for _ in $(seq 1 200); do
  if compgen -G "/tmp/bwapi_socket_*" >/dev/null; then ready=1; break; fi
  sleep 0.1
done
if [ "$ready" -ne 1 ]; then
  say "server log tail:"; tail -20 "$log" | sed 's/^/    /'
  fail "server never opened a listening socket within 20s"
fi

out="$("$PROBE" --auto --timeout-ms 8000 --frames 10 2>&1)"
rc=$?
printf '%s\n' "$out" | sed 's/^/[test-openbw-host]   /'

# Stop the host before judging, so a failure never leaves a game running.
kill -9 "$host_pid" 2>/dev/null || true
pkill -9 -x BWAPILauncher 2>/dev/null || true

check() {  # check "label" pattern
  if printf '%s' "$out" | grep -qE "$2"; then
    say "ok   $1"
  else
    say "FAIL $1"
    failed=1
  fi
}
failed=0
check "handshake byte 2"      'VERDICT: BYTE_2_OK'
check "match is ticking"      'VERDICT: MATCH_[A-Z_]*TICKING|VERDICT: MATCH_TICKING'
check "MatchStart published"  'MatchStart in event queue: YES'
check "player table filled"   'players with start location: [1-9][0-9]*/12'

if [ "$failed" -ne 0 ] || [ "$rc" -ne 0 ]; then
  say "server log tail:"
  tail -15 "$log" | sed 's/^/    /'
  say "host log: $log"
  fail "one or more host checks failed (probe exit=$rc)"
fi

rm -f "$log"
say "PASS: handshake, ticking match, MatchStart and players are all correct"
exit 0
