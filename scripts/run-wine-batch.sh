#!/bin/bash
# Overnight kite tuning loop on the real duel (Wine 1Drag_v_1Zeal).
#
# Usage:
#   bash scripts/run-wine-batch.sh "1Drag_v_1Zeal.scm" N OUTDIR [KEY=VALUE ...] [--env=FILE] [--headless]
#
# Runs N Wine games. Each game gets a private env file (OUTDIR/game-N/env:
# a copy of --env=FILE, default bwapi-data/AI/ENV-WINE-FAST, plus
# PARAM_TWEAKER=1 and any KEY=VALUE overrides), passed to the bot with
# --env=. The shared ENV is never touched. Each game randomizes the
# registered coefficients in-JVM (ParamTweakerFactory) unless overridden
# (reproducible validation runs). After each game the per-game CSV
# (W/L, seconds, killed, lost, hits taken, shots fired, score, param
# values) and EVENTS.jsonl land in OUTDIR, plus a batch.csv summary line.
# The bot also overwrites param_tweaker/__tweaker.csv every game (last
# result only, header + one row) - the dated files stay the archive.
# The per-game copy below reads that fixed name (freshness-checked),
# never a newest-pick of the archive.
#
# Visible by default; pass --headless for unattended runs.
#
# Examples:
#   bash scripts/run-wine-batch.sh "1Drag_v_1Zeal.scm" 30 /tmp/opencode/tune1
#   bash scripts/run-wine-batch.sh "1Drag_v_1Zeal.scm" 5 /tmp/opencode/tune2 KITE_STEP_TILES=2.5 KITE_MARGIN_BASE_TILES=1.0
#
# Owner-run only (CONVENTIONS §14); models run it with the per-case
# go-ahead. One game at a time - never parallelize Wine games.

set -u

ATLANTIS_DIR="/sc-ai/Atlantis"

MAP=""
N=""
OUTDIR=""
ENV_BASE="bwapi-data/AI/ENV-WINE-FAST"
HIDDEN_FLAG=""
EXTRA=""
for arg in "$@"; do
  case "$arg" in
    --env=*) ENV_BASE="${arg#--env=}" ;;
    --hidden|--headless) HIDDEN_FLAG="--headless" ;;
    --visible) HIDDEN_FLAG="" ;;
    *=*) EXTRA="$EXTRA $arg" ;;
    *)
      if [ -z "$MAP" ]; then MAP="$arg";
      elif [ -z "$N" ]; then N="$arg";
      elif [ -z "$OUTDIR" ]; then OUTDIR="$arg";
      else echo "[wine-batch] ignoring extra arg: $arg" >&2; fi ;;
  esac
done
[ -n "$MAP" ] && [ "$N" -gt 0 2>/dev/null ] && [ -n "$OUTDIR" ] || { echo "usage: run-wine-batch.sh \"<map.scm>\" N OUTDIR [KEY=VALUE ...] [--env=FILE] [--headless]" >&2; exit 1; }
[ -f "$ENV_BASE" ] || { echo "[wine-batch] no such base env: $ENV_BASE" >&2; exit 1; }

mkdir -p "$OUTDIR"
CSV="$OUTDIR/batch.csv"
[ -f "$CSV" ] || echo "game,result,seconds,killed,lost,hits,shots,score" > "$CSV"

ATLANTIS_DIR="/sc-ai/Atlantis"
cd "$ATLANTIS_DIR" || exit 1

i=0
while [ "$i" -lt "$N" ]; do
  i=$(( i + 1 ))
  echo "[wine-batch] game $i/$N ($MAP)"
  # Per-game env file: base + tweaker flag + explicit overrides. The
  # shared ENV is never touched (owner: scripts must not leak per-run
  # flags into watched games) - the bot reads this file via --env=.
  # shellcheck disable=SC2086
  GDIR="$OUTDIR/game-$i"
  mkdir -p "$GDIR"
  GAME_ENV="$GDIR/env"
  cp -f "$ENV_BASE" "$GAME_ENV"
  printf '\nPARAM_TWEAKER=1\n' >> "$GAME_ENV"
  for kv in $EXTRA; do
    key="${kv%%=*}"
    val="${kv#*=}"
    if grep -q "^${key}=" "$GAME_ENV" 2>/dev/null; then
      sed -i -E "s|^${key}=.*|${key}=${val}|" "$GAME_ENV"
    else
      printf '%s=%s\n' "$key" "$val" >> "$GAME_ENV"
    fi
  done

  # Collect per-game artifacts before the next run wipes them.
  # Staleness guard: a failed start leaves the previous game's files
  # behind - only accept a run whose EVENTS is newer than the run
  # itself and carries a GAME_END (measured: game-26 copied game-25).
  touch "$GDIR/.started"
  # shellcheck disable=SC2086
  if ! bash scripts/run-wine-ums.sh "$MAP" --env="$GAME_ENV" $HIDDEN_FLAG > "$OUTDIR/game-$i.log" 2>&1; then
    echo "[wine-batch] game $i: runner failed (see game-$i.log), continuing"
  fi
  cp -f out/wine/EVENTS.jsonl "$GDIR/EVENTS.jsonl" 2>/dev/null || true
  cp -f out/wine/WINE_LOG "$GDIR/WINE_LOG" 2>/dev/null || true
  if [ ! "$GDIR/EVENTS.jsonl" -nt "$GDIR/.started" ] \
    || ! grep -q '"key":"GAME_END"' "$GDIR/EVENTS.jsonl" 2>/dev/null; then
    echo "[wine-batch] game $i: INVALID (no fresh game end), skipped" | tee -a "$OUTDIR/batch.log"
    rm -f "$GDIR/EVENTS.jsonl" "$GDIR/tweaker.csv"
  else
    # Deterministic per-game params: the bot overwrites the fixed-name
    # __tweaker.csv every game, so copy it before the next game
    # overwrites it again. Never newest-pick the dated archive here
    # (multi-file pick proved racy and dropped all four game rows).
    if [ -f param_tweaker/__tweaker.csv ] && [ param_tweaker/__tweaker.csv -nt "$GDIR/.started" ]; then
      cp -f param_tweaker/__tweaker.csv "$GDIR/tweaker.csv"
      tail -1 "$GDIR/tweaker.csv" | awk -F';' -v g="$i" '{print g","$2","$3","$4","$5","$6","$7","$8}' >> "$CSV" 2>/dev/null || true
    fi
  fi
  if [ -f "$GDIR/EVENTS.jsonl" ]; then
    python3 scripts/summarize-fight.py "$GDIR/EVENTS.jsonl" > "$GDIR/digest.txt" 2>&1 || true
  fi
  HITS="$(grep -oE "hits taken: [0-9]+" "$GDIR/digest.txt" 2>/dev/null | grep -oE "[0-9]+" | head -1)"
  FRAMES="$(grep -oE "game end: frame=[0-9]+" "$GDIR/digest.txt" 2>/dev/null | grep -oE "[0-9]+" | head -1)"
  echo "[wine-batch] game $i: frames=${FRAMES:-?} hits=${HITS:-?} (see $GDIR)"
done

# Batch manifest (NEXT #160 step 5): one file per batch that later
# meta-analysis reads instead of re-walking per-game dirs. Same line
# format as atlantis.debug.tweaker.TunerBatchManifest.render, so the
# Java round-trip pin covers it: game dirs + verdict + param digest +
# full vector. Mirrors batch.csv (valid games only; invalid ones stay
# in batch.log). PARAM_DIGEST covers the param portion of the tweaker
# row only (fields 9+, fixed column order), so equal vectors hash
# equal across batches.
MANIFEST="$OUTDIR/manifest.txt"
JAR_FP="$(md5sum "$HOME/.scbw/bots/AtlantisP/AI/Atlantis.jar" 2>/dev/null | cut -d' ' -f1)"
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
    DIGEST="$(printf '%s' "$PARAMS" | md5sum | cut -d' ' -f1)"
    VEC="$(paste -d'|' <(head -1 "$TW" | tr ';' '\n' | tail -n +9) <(printf '%s' "$PARAMS" | tr ';' '\n') | awk -F'|' '{printf "%s=%s,", $1, $2}' | sed 's/,$//')"
    echo "game-$g|$V|$hits|$seconds|$DIGEST|$VEC"
  done
} > "$MANIFEST"
echo "[wine-batch] manifest: $MANIFEST"

echo "[wine-batch] done: $N games, summary in $CSV"
column -t -s, "$CSV" 2>/dev/null | head -35 || cat "$CSV"
