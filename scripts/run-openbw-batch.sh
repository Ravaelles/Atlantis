#!/usr/bin/env bash
# ParamTweaker batch on OpenBW UMS micro maps (the unattended tuning loop).
#
# Usage:
#   bash scripts/run-openbw-batch.sh "maps/ums/rav/protoss/ZealDrag_v_LingsUpg.scm" N OUTDIR [KEY=VALUE ...]
#
# Runs N OpenBW games, one at a time (never parallelize: one game per box).
# Each game gets PARAM_TWEAKER=1 plus any KEY=VALUE overrides through
# OPENBW_EXTRA_ENV, so the bot randomizes the registered coefficients
# in-JVM (ParamTweakerFactory) unless overridden (reproducible runs).
# After each game the per-game CSV (W/L, seconds, killed, lost, hits,
# shots, score, param values) and EVENTS.jsonl land in OUTDIR/game-N,
# plus a batch.csv summary line and a manifest (NEXT #160 step 5 shape).
# A SUMMARY.md per batch goes to OUTDIR and to
# "/sc-ai/E2E OpenBW LOGS/PARAM_TWEAKER_LOGS/" (dated file + refreshed
# `latest`), so the last optimization result is always one read away.
#
# Mirrors scripts/run-wine-batch.sh (same CSV, manifest and staleness
# rules); validity rule is identical: a run counts only with a fresh
# EVENTS.jsonl carrying GAME_END.

set -u

ATLANTIS_DIR="/sc-ai/Atlantis"
RUN_SCRIPT="$ATLANTIS_DIR/scripts/run-openbw-ums.sh"
BOT_ROOT="${OPENBW_BOT_ROOT:-/sc-ai/BOTS/AtlantisOpenBW}"
TWEAKER_CSV="$BOT_ROOT/param_tweaker/__tweaker.csv"
TWEAKER_LOGS="/sc-ai/E2E OpenBW LOGS/PARAM_TWEAKER_LOGS"

say()  { echo "[openbw-batch] $*"; }
fail() { echo "[openbw-batch] FATAL: $*" >&2; exit 1; }

MAP="${1:-}"; N="${2:-}"; OUTDIR="${3:-}"
shift 3 2>/dev/null || fail "usage: run-openbw-batch.sh \"maps/ums/<map>.scm\" N OUTDIR [KEY=VALUE ...]"
[ -n "$MAP" ] && [ "$N" -gt 0 2>/dev/null ] && [ -n "$OUTDIR" ] \
  || fail "usage: run-openbw-batch.sh \"maps/ums/<map>.scm\" N OUTDIR [KEY=VALUE ...]"
case "$MAP" in *ums/*) ;; *) fail "not a UMS map path (must contain ums/): $MAP" ;; esac
[ -x "$RUN_SCRIPT" ] || fail "game runner not found: $RUN_SCRIPT"

EXTRA="PARAM_TWEAKER=1"
for kv in "$@"; do
  case "$kv" in *=*) EXTRA="$EXTRA
$kv" ;; *) fail "override must be KEY=VALUE, got: $kv" ;; esac
done

mkdir -p "$OUTDIR"
CSV="$OUTDIR/batch.csv"
[ -f "$CSV" ] || echo "game,result,seconds,killed,lost,hits,shots,score" > "$CSV"
: > "$OUTDIR/batch.log"

BATCH_START="$(date +%s)"
i=0
while [ "$i" -lt "$N" ]; do
  i=$(( i + 1 ))
  say "game $i/$N ($MAP)"
  GDIR="$OUTDIR/game-$i"
  mkdir -p "$GDIR"
  touch "$GDIR/.started"
  SECONDS=0
  # shellcheck disable=SC2086
  if ! OPENBW_EXTRA_ENV="$EXTRA" bash "$RUN_SCRIPT" "$MAP" > "$GDIR/run.log" 2>&1; then
    say "game $i: runner exited non-zero (see $GDIR/run.log), collecting anyway"
  fi
  WALL="$SECONDS"
  echo "wall_seconds=$WALL" > "$GDIR/wall.txt"
  RUN_DIR_LINE="$(grep -a -m1 'run dir: ' "$GDIR/run.log" 2>/dev/null | sed 's/.*run dir: //')"
  if [ -n "$RUN_DIR_LINE" ] && [ -d "$RUN_DIR_LINE" ]; then
    cp -f "$RUN_DIR_LINE/UMS_LOG" "$GDIR/UMS_LOG" 2>/dev/null || true
    cp -f "$RUN_DIR_LINE/EVENTS.jsonl" "$GDIR/EVENTS.jsonl" 2>/dev/null || true
  fi
  if [ ! -f "$GDIR/EVENTS.jsonl" ] || [ ! "$GDIR/EVENTS.jsonl" -nt "$GDIR/.started" ] \
    || ! grep -aq '"key":"GAME_END"' "$GDIR/EVENTS.jsonl" 2>/dev/null; then
    echo "game $i: INVALID (no fresh game end, wall=${WALL}s), skipped" | tee -a "$OUTDIR/batch.log"
    rm -f "$GDIR/EVENTS.jsonl" "$GDIR/tweaker.csv"
  else
    if [ -f "$TWEAKER_CSV" ] && [ "$TWEAKER_CSV" -nt "$GDIR/.started" ]; then
      cp -f "$TWEAKER_CSV" "$GDIR/tweaker.csv"
      tail -1 "$GDIR/tweaker.csv" | awk -F';' -v g="$i" '{print g","$2","$3","$4","$5","$6","$7","$8}' >> "$CSV" 2>/dev/null || true
    else
      echo "game $i: no fresh tweaker.csv (wall=${WALL}s)" | tee -a "$OUTDIR/batch.log"
    fi
    python3 "$ATLANTIS_DIR/scripts/summarize-fight.py" "$GDIR/EVENTS.jsonl" > "$GDIR/digest.txt" 2>&1 || true
  fi
  GAMESEC="$(grep -a -m1 -oE 'Total time: [0-9]+' "$GDIR/UMS_LOG" 2>/dev/null | grep -oE '[0-9]+' | head -1)"
  echo "game_seconds=${GAMESEC:-?}" >> "$GDIR/wall.txt"
  say "game $i: wall=${WALL}s game-sec=${GAMESEC:-?} (see $GDIR)"
done
BATCH_END="$(date +%s)"

# Manifest (same shape as the wine batch): map, jar, per-game verdicts.
MANIFEST="$OUTDIR/manifest.txt"
JAR_FP="$(md5sum "$BOT_ROOT/AI/Atlantis.jar" 2>/dev/null | cut -d' ' -f1)"
[ -z "$JAR_FP" ] && JAR_FP="unknown"
{
  echo "map=$MAP"
  echo "jar=$JAR_FP"
  echo "games=$(($(wc -l < "$CSV") - 1))"
  tail -n +2 "$CSV" 2>/dev/null | while IFS=, read -r g result seconds killed lost hits shots score; do
    TW="$OUTDIR/game-$g/tweaker.csv"
    [ -f "$TW" ] || continue
    case "$result" in W) V=WIN;; *) V=LOSS;; esac
    PARAMS="$(tail -1 "$TW" | cut -d';' -f9-)"
    [ -z "$PARAMS" ] && continue
    echo "game=$g verdict=$V seconds=$seconds killed=$killed lost=$lost hits=$hits shots=$shots score=$score params=$PARAMS"
  done
} > "$MANIFEST"

# Per-batch summary: wall/game timing, validity, verdict split.
VALID="$(($(wc -l < "$CSV") - 1))"
WINS="$(tail -n +2 "$CSV" 2>/dev/null | awk -F, '$2=="W"' | wc -l)"
AVG_WALL="$(awk -F= '/^wall_seconds=/{s+=$2; n++} END{if(n) printf "%.1f", s/n; else print "?"}' "$OUTDIR"/game-*/wall.txt 2>/dev/null)"
AVG_GAMESEC="$(awk -F= '/^game_seconds=/{if($2!="?"){s+=$2; n++}} END{if(n) printf "%.1f", s/n; else print "?"}' "$OUTDIR"/game-*/wall.txt 2>/dev/null)"
STAMP="$(date +%Y%m%d_%H%M%S)"
SUMMARY="$OUTDIR/SUMMARY.md"
{
  echo "# OpenBW ParamTweaker batch $STAMP"
  echo ""
  echo "- map: \`$MAP\`, games requested: $N, valid: $VALID, wins: $WINS"
  echo "- jar md5: \`$JAR_FP\`"
  echo "- batch wall: $((BATCH_END - BATCH_START)) s"
  echo "- avg wall per game: ${AVG_WALL}s, avg game-seconds per game: ${AVG_GAMESEC}s"
  echo "- out: \`$OUTDIR\` (per-game dirs, batch.csv, manifest.txt)"
  echo ""
  echo "## batch.csv"
  echo ""
  echo '```'
  cat "$CSV" 2>/dev/null
  echo '```'
} > "$SUMMARY"

mkdir -p "$TWEAKER_LOGS"
BASE="$(basename "$MAP" | tr -c 'A-Za-z0-9._-' '_')"
LOGNAME="${STAMP}_${BASE}_N${N}.md"
cp -f "$SUMMARY" "$TWEAKER_LOGS/$LOGNAME"
ln -sfn "$TWEAKER_LOGS/$LOGNAME" "$TWEAKER_LOGS/latest"
say "summary: $SUMMARY -> $TWEAKER_LOGS/$LOGNAME (latest refreshed)"
say "batch done: valid=$VALID/$N wins=$WINS avg_wall=${AVG_WALL}s"
