#!/bin/bash
# Contract test for build-openbw.sh --game verdicts (NEXT: DRAW state).
# No game is played: fixture run dirs feed decide_game_verdict(),
# extracted verbatim from build-openbw.sh (no copy to drift).
#
#   bash scripts/test-openbw-verdict.sh
#
# Exit 0 = all verdicts as specified, non-zero = contract broken.
set -u

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GAME_TIMEOUT=60
export GAME_TIMEOUT

WORK="$(mktemp -d -t openbw-verdict.XXXXXX)"
trap 'rm -rf "$WORK"' EXIT

# Extract the function verbatim (function header .. its closing brace).
awk '/^decide_game_verdict\(\) \{/,/^\}$/' "$SCRIPT_DIR/build-openbw.sh" > "$WORK/verdict.sh"
[ -s "$WORK/verdict.sh" ] || { echo "FAIL: could not extract decide_game_verdict"; exit 1; }
# shellcheck disable=SC1090
source "$WORK/verdict.sh"

PASS=0
FAILN=0
check() { # name expected_word expected_code -- args...
  local name="$1" word="$2" code="$3"
  shift 3
  out="$("$@" 2>&1)"
  rc=$?
  if [ $rc -eq "$code" ] && echo "$out" | grep -q "^$word"; then
    echo "ok: $name"
    PASS=$((PASS + 1))
  else
    echo "FAIL: $name (want $word/$code, got rc=$rc: $(echo "$out" | head -1))"
    FAILN=$((FAILN + 1))
  fi
}

mkbotlog() { # dir banner[K|VIC|none]
  mkdir -p "$1"
  case "${2:-none}" in
    VIC) echo "############ VICTORY! ###############" > "$1/UMS_LOG" ;;
    DEF) echo "############ Defeat... ###############" > "$1/UMS_LOG" ;;
    *) : > "$1/UMS_LOG" ;;
  esac
}
mkgamelog() { # file [with-killed]
  if [ "${1:-}" = "with-killed" ]; then echo "KILLED:0 LOST:0 TIMES_HIT:8" > "$2"; else : > "$2"; fi
}

# 1. WIN: banner rules, counters don't matter.
D="$WORK/win"; mkbotlog "$D" VIC; G="$WORK/g1.log"; mkgamelog with-killed "$G"
check "win-banner" "WIN" 0 decide_game_verdict 0 "$G" "$D" "KILLED:1 LOST:0 TIMES_HIT:5" 1 0 5 500

# 2. LOSS: Defeat + losses.
D="$WORK/loss"; mkbotlog "$D" DEF; G="$WORK/g2.log"; mkgamelog with-killed "$G"
check "loss-fought" "LOSS" 0 decide_game_verdict 0 "$G" "$D" "KILLED:3 LOST:7 TIMES_HIT:40" 3 7 40 500

# 3. DRAW: Defeat, zero losses, contested (the capped duel shape).
D="$WORK/draw"; mkbotlog "$D" DEF; G="$WORK/g3.log"; mkgamelog with-killed "$G"
check "draw-capped" "DRAW" 0 decide_game_verdict 124 "$G" "$D" "KILLED:0 LOST:0 TIMES_HIT:14" 0 0 14 486

# 4. DRAW: Defeat, kills but no losses (unresolved, not outplayed).
D="$WORK/draw2"; mkbotlog "$D" DEF; G="$WORK/g4.log"; mkgamelog with-killed "$G"
check "draw-kills-no-loss" "DRAW" 0 decide_game_verdict 0 "$G" "$D" "KILLED:2 LOST:0 TIMES_HIT:9" 2 0 9 500

# 5. FAIL: empty game (explicit 0/0/0).
D="$WORK/empty"; mkbotlog "$D" DEF; G="$WORK/g5.log"; mkgamelog with-killed "$G"
check "fail-empty" "FAIL" 1 decide_game_verdict 0 "$G" "$D" "KILLED:0 LOST:0 TIMES_HIT:0" 0 0 0 500

# 6. FAIL: outer timeout, inner never finished (no KILLED: line).
D="$WORK/hang"; mkbotlog "$D" none; G="$WORK/g6.log"; : > "$G"
check "fail-hang" "FAIL" 1 decide_game_verdict 124 "$G" "$D" "KILLED:? LOST:? TIMES_HIT:?" "?" "?" "?" "?"

# 7. FAIL: frames too few (dead server).
D="$WORK/short"; mkbotlog "$D" none; G="$WORK/g7.log"; mkgamelog with-killed "$G"
check "fail-short" "FAIL" 1 decide_game_verdict 0 "$G" "$D" "KILLED:0 LOST:0 TIMES_HIT:0" 0 0 0 12

# 8. Inner cap with combat still decides, not fails (the 124 fall-through).
D="$WORK/cap"; mkbotlog "$D" DEF; G="$WORK/g8.log"; mkgamelog with-killed "$G"
check "cap-with-combat-draws" "DRAW" 0 decide_game_verdict 124 "$G" "$D" "KILLED:0 LOST:0 TIMES_HIT:6" 0 0 6 300

echo "verdicts: pass=$PASS fail=$FAILN"
[ "$FAILN" -eq 0 ]
