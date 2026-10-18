#!/bin/bash
# Overnight kite tuning loop on the real duel (Wine 1Drag_v_1Zeal).
#
# Usage:
#   bash scripts/run-wine-batch.sh "1Drag_v_1Zeal.scm" N OUTDIR [KEY=VALUE ...]
#
# Runs N Wine games. Each game randomizes the registered coefficients
# in-JVM (ParamTweakerFactory) unless overridden by KEY=VALUE pairs
# (applied verbatim via ENV, reproducible validation runs). After each
# game the per-game CSV (W/L, seconds, killed, lost, hits taken,
# shots fired, score, param values) and EVENTS.jsonl land in OUTDIR,
# plus a batch.csv summary line.
#
# Examples:
#   bash scripts/run-wine-batch.sh "1Drag_v_1Zeal.scm" 30 /tmp/opencode/tune1
#   bash scripts/run-wine-batch.sh "1Drag_v_1Zeal.scm" 5 /tmp/opencode/tune2 KITE_STEP_TILES=2.5 KITE_MARGIN_BASE_TILES=1.0
#
# Owner-run only (CONVENTIONS §14); models run it with the per-case
# go-ahead. One game at a time - never parallelize Wine games.

set -u

MAP="${1:-}"
N="${2:-}"
OUTDIR="${3:-}"
shift 3 2>/dev/null || { echo "usage: run-wine-batch.sh \"<map.scm>\" N OUTDIR [KEY=VALUE ...]" >&2; exit 1; }
[ -n "$MAP" ] && [ "$N" -gt 0 2>/dev/null ] && [ -n "$OUTDIR" ] || { echo "usage: run-wine-batch.sh \"<map.scm>\" N OUTDIR [KEY=VALUE ...]" >&2; exit 1; }

mkdir -p "$OUTDIR"
CSV="$OUTDIR/batch.csv"
[ -f "$CSV" ] || echo "game,result,seconds,killed,lost,hits,shots,score" > "$CSV"

ATLANTIS_DIR="/sc-ai/Atlantis"
cd "$ATLANTIS_DIR" || exit 1

i=0
while [ "$i" -lt "$N" ]; do
  i=$(( i + 1 ))
  echo "[wine-batch] game $i/$N ($MAP)"
  # Per-game ENV overlay: PARAM_TWEAKER=1 plus any explicit overrides.
  # run-wine-full.sh borrows and restores the shared ENV; values set here
  # are the run's, and the loop rewrites them every game.
  for kv in "$@"; do
    key="${kv%%=*}"
    val="${kv#*=}"
    if grep -q "^${key}=" bwapi-data/AI/ENV 2>/dev/null; then
      sed -i -E "s|^${key}=.*|${key}=${val}|" bwapi-data/AI/ENV
    else
      printf '\n%s=%s\n' "$key" "$val" >> bwapi-data/AI/ENV
    fi
  done
  if grep -q "^PARAM_TWEAKER=" bwapi-data/AI/ENV 2>/dev/null; then
    sed -i -E "s|^PARAM_TWEAKER=.*|PARAM_TWEAKER=1|" bwapi-data/AI/ENV
  else
    printf '\nPARAM_TWEAKER=1\n' >> bwapi-data/AI/ENV
  fi

  if ! bash scripts/run-wine-ums.sh "$MAP" 1 > "$OUTDIR/game-$i.log" 2>&1; then
    echo "[wine-batch] game $i: runner failed (see game-$i.log), continuing"
  fi

  # Collect per-game artifacts before the next run wipes them.
  GDIR="$OUTDIR/game-$i"
  mkdir -p "$GDIR"
  cp -f out/wine/EVENTS.jsonl "$GDIR/EVENTS.jsonl" 2>/dev/null || true
  GCSV="$(ls -t param_tweaker/*.csv 2>/dev/null | head -1)"
  if [ -n "${GCSV:-}" ]; then
    cp -f "$GCSV" "$GDIR/tweaker.csv"
    tail -1 "$GCSV" | awk -F';' -v g="$i" '{print g","$2","$3","$4","$5","$6","$7","$8}' >> "$CSV" 2>/dev/null || true
  fi
  python3 scripts/summarize-fight.py "$GDIR/EVENTS.jsonl" > "$GDIR/digest.txt" 2>&1 || true
  HITS="$(grep -oE "hits taken: [0-9]+" "$GDIR/digest.txt" 2>/dev/null | grep -oE "[0-9]+" | head -1)"
  FRAMES="$(grep -oE "game end: frame=[0-9]+" "$GDIR/digest.txt" 2>/dev/null | grep -oE "[0-9]+" | head -1)"
  echo "[wine-batch] game $i: frames=${FRAMES:-?} hits=${HITS:-?} (see $GDIR)"
done

echo "[wine-batch] done: $N games, summary in $CSV"
column -t -s, "$CSV" 2>/dev/null | head -35 || cat "$CSV"
