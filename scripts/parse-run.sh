#!/usr/bin/env bash
# Parse one game run directory and print a single summary line.
#
# WHY THIS IS A SEPARATE FILE
#   The parsing used to live inline in build-openbw.sh --game, so any other
#   tool (a future batch runner, a CI job) would have re-implemented it and the
#   two copies would drift. One parser, one place to fix.
#
# WHAT IT READS (all produced by a normal run, see run-openbw-ums.sh)
#   UMS_LOG          the bot log; contains "Total time: N" and the final
#                    "### Units killed/lost:    K/L ###"
#   EVENTS.jsonl     one JSON row per event; "HIT" rows are accepted hits
#
# OUTPUT (one line, machine readable)
#   KILLED:<n|?> LOST:<n|?> TIMES_HIT:<n> FRAMES:<n>
#   "?" means the value was not found. That is deliberate: an unresolved run
#   must not look like a clean 0, otherwise a crashed game reads as a draw.
#
# Usage:
#   scripts/parse-run.sh <run-dir>
#   scripts/parse-run.sh --fields <run-dir>   # tab separated, for shell use
#
# Exit codes: 0 parsed (even with "?" values), 2 bad usage / missing dir.
set -uo pipefail

fields=0
if [ "${1:-}" = "--fields" ]; then fields=1; shift; fi
dir="${1:-}"
if [ -z "$dir" ] || [ ! -d "$dir" ]; then
  echo "usage: $0 [--fields] <run-dir>" >&2
  exit 2
fi

log="$dir/UMS_LOG"
events="$dir/EVENTS.jsonl"

# --- frames ----------------------------------------------------------------
# Atlantis prints "### Total time: 545 seconds. ###" (seconds, not frames).
# The line is matched loosely because the spacing differs between builds.
frames="$(sed -nE 's/.*Total time:[[:space:]]*([0-9]+).*/\1/p' "$log" 2>/dev/null | tail -1)"

# --- kills / losses --------------------------------------------------------
# "### Units killed/lost:    5/1 ###" -> K=5, L=1. A missing summary yields
# empty strings, reported as "?" so an interrupted run is visible.
killed="$(sed -nE 's/.*Units killed\/lost:[[:space:]]*([0-9?]+)\/([0-9?]+).*/\1/p' "$log" 2>/dev/null | tail -1)"
lost="$(sed -nE 's/.*Units killed\/lost:[[:space:]]*([0-9?]+)\/([0-9?]+).*/\2/p' "$log" 2>/dev/null | tail -1)"

# --- hits ------------------------------------------------------------------
# One row per accepted hit. Missing file -> 0 (a run that never fought).
hits=0
if [ -f "$events" ]; then
  hits="$(grep -c '\bHIT\b' "$events" 2>/dev/null || true)"
  hits="${hits:-0}"
fi

killed="${killed:-?}"
lost="${lost:-?}"
frames="${frames:-?}"

if [ "$fields" -eq 1 ]; then
  printf '%s\t%s\t%s\t%s\n' "$killed" "$lost" "$hits" "$frames"
else
  printf 'KILLED:%s LOST:%s TIMES_HIT:%s FRAMES:%s\n' "$killed" "$lost" "$hits" "$frames"
fi
exit 0
