#!/usr/bin/env bash
# Evaluate optional OpenBW scenario expectations against the bot's GameSummary log.
# Usage: assert-openbw-scenario.sh <bot-log>
#
# The patterns follow what `GameSummary.print` ACTUALLY emits (measured 2026-10-11):
#
#   ### Total time: 1209 seconds. ###
#   ### Units killed/lost:    0/0 ###
#   ### Resource killed/lost: 0 ###
#   ### Protoss: Nexus=1 Probe=25 Pylon=4 ###
#
# That last line is a per-type report ordered by first completion, so a type is simply
# absent until one is built - it does NOT contain an empty `Gateways=0`. The old patterns
# here looked for `Protoss buildings: Pylons=N Gateways=N`, a format that no longer exists,
# which is why every verdict printed `pylons=? gateways=?` for a run that had built four
# Pylons. A report that cannot read its own log is worse than no report; the patterns are
# pinned by `LauncherBuildsTheJarTest` so the next format change fails loudly.
set -euo pipefail
BOT_LOG="${1:?usage: assert-openbw-scenario.sh <bot-log> [debug-log]}"
# Placement refusals print to stderr (ErrorLog), so since the stream split
# they live in debug.log, not bot.log. Default keeps old single-arg calls
# working (a missing debug file simply never matches).
DEBUG_LOG="${2:-/dev/null}"

log_number() {
  local pattern="$1"
  grep -oE "$pattern" "$BOT_LOG" 2>/dev/null | tail -1 | grep -oE '[-0-9]+' | tail -1 || true
}

# `Type=<count>` on the summary line. names are race-stripped by AUnitType.name(), so the
# entry is `Pylon=`, not `Protoss_Pylon=`. The non-alnum guard stops `Pylon=` from matching
# the tail of another name.
unit_count() {
  local type="$1"
  log_number "(^|[^A-Za-z])${type}=[0-9]+"
}

INGAME_SECONDS="$(log_number 'Total time: [-0-9]+')"
KILLED="$(log_number 'Units killed/lost: *[0-9]+')"
RESOURCE_BALANCE="$(log_number 'Resource killed/lost: *[-+]?[0-9]+')"
PYLONS="$(unit_count 'Pylon')"
GATEWAYS="$(unit_count 'Gateway')"
ZEALOTS="$(unit_count 'Zealot')"
DRAGOONS="$(unit_count 'Dragoon')"
ASSIMILATORS="$(unit_count 'Assimilator')"
NEXUSES="$(unit_count 'Nexus')"
# The summary line reports the type as `CyberneticsC`, not `CyberneticsCore`
# (AUnitType.name() strips the race prefix and the display name is short), so the
# pattern has to match the text that is really printed - see the format note above.
CORES="$(unit_count 'CyberneticsC')"
FAILED=0
REASONS=()
check_min() {
  local expected="$1" actual="$2" label="$3"
  [ -n "$expected" ] || return 0
  # `actual` empty means the type never appeared on the summary line, i.e. we built ZERO
  # of it - report that as 0, not as `?`, or the failure reads like a parsing problem when
  # it is a production one. (`?` is kept for the in-game seconds, where an empty value
  # really does mean the summary was never printed.)
  local shown="${actual:-0}"
  if [ -z "$actual" ] || [ "$actual" -lt "$expected" ]; then
    REASONS+=("${label}=${shown}, wanted >= ${expected}")
    FAILED=1
  fi
}

check_max() {
  local expected="$1" actual="$2" label="$3"
  [ -n "$expected" ] || return 0
  local shown="${actual:-0}"
  if [ -z "$actual" ] || [ "$actual" -gt "$expected" ]; then
    REASONS+=("${label}=${shown}, wanted <= ${expected}")
    FAILED=1
  fi
}

check_min "${EXPECT_MIN_INGAME_SECONDS:-}" "$INGAME_SECONDS" "in-game seconds"
check_min "${EXPECT_MIN_PYLONS:-}" "$PYLONS" "Pylons"
check_min "${EXPECT_MIN_GATEWAYS:-}" "$GATEWAYS" "Gateways"
check_min "${EXPECT_MIN_ZEALOTS:-}" "$ZEALOTS" "Zealots"
check_min "${EXPECT_MIN_DRAGOONS:-}" "$DRAGOONS" "Dragoons"
check_min "${EXPECT_MIN_ASSIMILATORS:-}" "$ASSIMILATORS" "Assimilators"
check_min "${EXPECT_MIN_NEXUS:-}" "$NEXUSES" "Nexus"
check_min "${EXPECT_MIN_CORES:-}" "$CORES" "CyberneticsCores"
check_min "${EXPECT_MIN_KILLED:-}" "$KILLED" "kills"
check_max "${EXPECT_MAX_KILLED:-}" "$KILLED" "kills"
check_min "${EXPECT_MIN_RESOURCE_BALANCE:-}" "$RESOURCE_BALANCE" "resource balance"

if [ "${EXPECT_NO_PLACEMENT_FAILURES:-0}" = "1" ] \
  && { grep -q "Can't find place for" "$BOT_LOG" 2>/dev/null \
    || grep -q "Can't find place for" "$DEBUG_LOG" 2>/dev/null; }; then
  REASONS+=("placement refusal found in the log ('Can't find place for')")
  FAILED=1
fi

# === The result block =====================================================
# One machine-readable line first (what a script or a CI job greps), then the roster
# and the reason for a failure in words (what a human reads). The roster is printed on
# BOTH paths on purpose: on a pass it is the proof of what was built, and on a failure
# it is what separates "it built nothing" from "it built most of it and missed one".
echo "verdict: ingame=${INGAME_SECONDS:-?}s killed=${KILLED:-?} resourceBalance=${RESOURCE_BALANCE:-?}" \
  "pylons=${PYLONS:-0} gateways=${GATEWAYS:-0} zealots=${ZEALOTS:-0} dragoons=${DRAGOONS:-0}" \
  "assimilators=${ASSIMILATORS:-0} nexus=${NEXUSES:-0}"

echo "roster: Pylon=${PYLONS:-0} Gateway=${GATEWAYS:-0} Zealot=${ZEALOTS:-0}" \
  "Dragoon=${DRAGOONS:-0} Assimilator=${ASSIMILATORS:-0} Nexus=${NEXUSES:-0}" \
  "CyberneticsCore=${CORES:-0}"

echo "expected: Pylon>=${EXPECT_MIN_PYLONS:-0} Gateway>=${EXPECT_MIN_GATEWAYS:-0}" \
  "Zealot>=${EXPECT_MIN_ZEALOTS:-0} Dragoon>=${EXPECT_MIN_DRAGOONS:-0}" \
  "Assimilator>=${EXPECT_MIN_ASSIMILATORS:-0} Nexus>=${EXPECT_MIN_NEXUS:-0}" \
  "CyberneticsCore>=${EXPECT_MIN_CORES:-0}"

if [ "$FAILED" -ne 0 ]; then
  echo "ASSERTION FAILED because:"
  for reason in "${REASONS[@]}"; do
    echo "  - missing/insufficient: $reason"
  done
  exit 1
fi
echo "ASSERTION OK: every expected structure/unit was produced."
echo "SCENARIO PASSED"
