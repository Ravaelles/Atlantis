#!/usr/bin/env bash
# Deterministic micro-benchmark: cost of one frame of combat decisions.
# See src/tests/benchmark/TreeConstructionBenchmark.java.
#
#   bash scripts/benchmark-trees.sh              # run and print
#   bash scripts/benchmark-trees.sh --save       # run and record the baseline
#   bash scripts/benchmark-trees.sh --check      # run and fail if it got slower
#
# The comparison is against a baseline recorded on the *same machine*
# (--save), never against a number from someone else's: the stub world is
# Mockito-inflated, the absolute figure says more about the JVM and the CPU than
# about the bot, and gating on it would produce failures nobody could act on.
# What a baseline is good for is catching a refactor that made the per-frame work
# slower on the machine you work on - which is the regression that silently
# costs a game.
set -euo pipefail

cd "$(dirname "$0")/.."

MODE="run"
TOLERANCE=20
case "${1:-}" in
  --save)  MODE="save" ;;
  --check) MODE="check" ;;
  "")      MODE="run" ;;
  *)       echo "usage: $0 [--save|--check]" >&2; exit 2 ;;
esac

OUT="out/benchmark"
BASELINE="_AI/benchmarks/frame-pipeline.txt"
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"

rm -rf "$OUT"
mkdir -p "$OUT"
javac -nowarn -cp "$CP" -d "$OUT" src/tests/benchmark/TreeConstructionBenchmark.java -sourcepath src
RESULT="$(java -cp "$OUT:$CP" tests.benchmark.TreeConstructionBenchmark | tail -n 1)"

echo "$RESULT"
NS_PER_FRAME="$(echo "$RESULT" | sed -n 's/.*best-ns-per-frame-per-unit=\([0-9]*\).*/\1/p')"
if [ -z "$NS_PER_FRAME" ]; then
  echo "could not read the measurement out of: $RESULT" >&2
  exit 1
fi

# Machine signature: what has to match for two numbers to mean anything. No
# /proc, no system paths - see CONVENTIONS §8.
SIGNATURE="$(java -version 2>&1 | head -1) / $(uname -sm)"

case "$MODE" in
  save)
    mkdir -p "$(dirname "$BASELINE")"
    {
      echo "# Recorded by scripts/benchmark-trees.sh --save on $(date -u +%Y-%m-%d)"
      echo "# Machine: $SIGNATURE"
      echo "# best-ns-per-frame-per-unit; only comparable with a run on the same machine."
      echo "$NS_PER_FRAME"
    } > "$BASELINE"
    echo "saved $BASELINE ($NS_PER_FRAME)"
    ;;

  check)
    if [ ! -f "$BASELINE" ]; then
      echo "no baseline in $BASELINE - record one with --save on this machine" >&2
      exit 1
    fi

    RECORDED_SIGNATURE="$(sed -n 's/^# Machine: //p' "$BASELINE")"
    RECORDED="$(grep -E '^[0-9]+$' "$BASELINE" | tail -1)"

    if [ "$SIGNATURE" != "$RECORDED_SIGNATURE" ]; then
      echo "not comparable: the baseline was recorded on"
      echo "  $RECORDED_SIGNATURE"
      echo "and this run is on"
      echo "  $SIGNATURE"
      echo "re-record it here with --save if you want this machine guarded."
      exit 0
    fi

    WORST=$(( RECORDED * (100 + TOLERANCE) / 100 ))
    if [ "$NS_PER_FRAME" -gt "$WORST" ]; then
      echo "SLOWER than the baseline by more than ${TOLERANCE}%:" >&2
      echo "  baseline $RECORDED ns/frame/unit, now $NS_PER_FRAME" >&2
      exit 1
    fi

    CHANGE=$(( (NS_PER_FRAME - RECORDED) * 100 / RECORDED ))
    echo "within ${TOLERANCE}% of the baseline ($RECORDED ns/frame/unit, ${CHANGE}%)"
    ;;
esac