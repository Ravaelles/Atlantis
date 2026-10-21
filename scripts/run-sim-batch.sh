#!/bin/bash
# Sim batch runner (NEXT #160-sim): the wine batch contract without Wine.
#
# Usage:
#   bash scripts/run-sim-batch.sh "1Drag_v_1Zeal.scm" N OUTDIR [KEY=VALUE ...] [--env=FILE]
#
# Only the duel scenario map plays (anything else fails loudly); --env is
# accepted for tune-driver compatibility and ignored (sim has no game to
# configure). Each game applies its vector in-JVM, plays scripted sim
# duels, and lands the wine-contract artifacts (per-game tweaker.csv,
# batch.csv, manifest.txt) so TunerDriver consumes them unchanged.
#
# No Wine, no lock, no display: safe to run in the fast loop. Slow scopes
# stay owner-called; this script is machine-cheap (sim ticks are plain Java).

set -u

ATLANTIS_DIR="/sc-ai/Atlantis"
MAP=""
N=""
OUTDIR=""
EXTRA=""
for arg in "$@"; do
  case "$arg" in
    --env=*) ;;
    --headless|--hidden|--visible) ;;
    *=*) EXTRA="$EXTRA $arg" ;;
    *)
      if [ -z "$MAP" ]; then MAP="$arg";
      elif [ -z "$N" ]; then N="$arg";
      elif [ -z "$OUTDIR" ]; then OUTDIR="$arg";
      else echo "[sim-batch] ignoring extra arg: $arg" >&2; fi ;;
  esac
done
[ -n "$MAP" ] && [ "$N" -gt 0 2>/dev/null ] && [ -n "$OUTDIR" ] \
  || { echo "usage: run-sim-batch.sh \"<map.scm>\" N OUTDIR [KEY=VALUE ...]" >&2; exit 2; }

cd "$ATLANTIS_DIR" || exit 1

CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
OUT="out/production/Atlantis"
mkdir -p "$OUT"
echo "[sim-batch] compiling tooling classes"
javac --release 8 -nowarn -cp "$CP" -d "$OUT" $(find src -name '*.java') \
  || { echo "[sim-batch] compile failed" >&2; exit 1; }

# shellcheck disable=SC2086
java -cp "$OUT:.:$CP" tests.unit.sim.SimTunerBatch "$MAP" "$N" "$OUTDIR" $EXTRA
