#!/usr/bin/env bash
# Evaluate optional OpenBW scenario expectations against the bot's GameSummary log.
# Usage: assert-openbw-scenario.sh <bot-log>
set -euo pipefail
BOT_LOG="${1:?usage: assert-openbw-scenario.sh <bot-log>}"

log_number() {
  local pattern="$1"
  grep -oE "$pattern" "$BOT_LOG" 2>/dev/null | tail -1 | grep -oE '[-0-9]+' | tail -1 || true
}

INGAME_SECONDS="$(log_number 'Total time: [-0-9]+')"
KILLED="$(log_number 'Units killed/lost: *[0-9]+')"
RESOURCE_BALANCE="$(log_number 'Resource killed/lost: *[-+]?[0-9]+')"
PYLONS="$(log_number 'Protoss buildings: Pylons=[0-9]+')"
GATEWAYS="$(log_number 'Protoss buildings:.*Gateways=[0-9]+')"
FAILED=0
check_min() {
  local expected="$1" actual="$2" label="$3"
  [ -n "$expected" ] || return 0
  if [ -z "$actual" ] || [ "$actual" -lt "$expected" ]; then
    echo "ASSERT FAILED: $label was ${actual:-?}, expected >= $expected"
    FAILED=1
  fi
}

check_max() {
  local expected="$1" actual="$2" label="$3"
  [ -n "$expected" ] || return 0
  if [ -z "$actual" ] || [ "$actual" -gt "$expected" ]; then
    echo "ASSERT FAILED: $label was ${actual:-?}, expected <= $expected"
    FAILED=1
  fi
}

echo "verdict: ingame=${INGAME_SECONDS:-?}s killed=${KILLED:-?} resourceBalance=${RESOURCE_BALANCE:-?} pylons=${PYLONS:-?} gateways=${GATEWAYS:-?}"
check_min "${EXPECT_MIN_INGAME_SECONDS:-}" "$INGAME_SECONDS" "in-game seconds"
check_min "${EXPECT_MIN_PYLONS:-}" "$PYLONS" "Pylons"
check_min "${EXPECT_MIN_GATEWAYS:-}" "$GATEWAYS" "Gateways"
check_min "${EXPECT_MIN_KILLED:-}" "$KILLED" "kills"
check_max "${EXPECT_MAX_KILLED:-}" "$KILLED" "kills"
check_min "${EXPECT_MIN_RESOURCE_BALANCE:-}" "$RESOURCE_BALANCE" "resource balance"

if [ "${EXPECT_NO_PLACEMENT_FAILURES:-0}" = "1" ] && grep -q "Can't find place for" "$BOT_LOG"; then
  echo "ASSERT FAILED: placement refusal found in $BOT_LOG"
  FAILED=1
fi
if [ "$FAILED" -ne 0 ]; then exit 1; fi
echo "SCENARIO PASSED"
