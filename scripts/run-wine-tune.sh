#!/bin/bash
# Gradient-guided tuner loop (NEXT #160, step 6): run a batch, propose
# the next params from the hall of fame, repeat.
#
# Usage:
#   bash scripts/run-wine-tune.sh "1Drag_v_1Zeal.scm" ROUNDS K OUTDIR [--env=FILE] [--seed=N] [--step=D] [--radius=D] [--engine=wine|openbw|sim]
#
# Engines (atlantis.debug.tweaker.TuningEngines, the source of truth -
# the strings below mirror it): wine tunes today; openbw and sim fail
# fast with the reason instead of running a broken loop.
#
# Each round runs run-wine-batch.sh with K games (duels are noisy -
# the scenario wants replicates) and the current fragment as KEY=VALUE
# overrides. Round 1 has no fragment, so it randomizes exactly like a
# plain batch (cold start). After each round TunerDriver reads the hall
# of fame plus the round manifest, saves the archive, and writes the
# next fragment; BEST=<fitness> on its stdout drives the schedule
# (halve step and radius when the best stops improving).
#
# Owner-run only (CONVENTIONS §14); one game at a time, never
# parallelize Wine games. Overnight N-game sweeps stay owner-called
# (#168b); this loop is the machinery, proven here on short rounds.
set -u

ATLANTIS_DIR="/sc-ai/Atlantis"
MAP=""
ROUNDS=""
K=""
OUTDIR=""
ENV_ARG=""
ENGINE="wine"
SEED="$RANDOM"
STEP=""
RADIUS=""
for arg in "$@"; do
  case "$arg" in
    --env=*) ENV_ARG="$arg" ;;
    --seed=*) SEED="${arg#--seed=}" ;;
    --step=*) STEP="${arg#--step=}" ;;
    --radius=*) RADIUS="${arg#--radius=}" ;;
    --engine=*) ENGINE="${arg#--engine=}" ;;
    *)
      if [ -z "$MAP" ]; then MAP="$arg";
      elif [ -z "$ROUNDS" ]; then ROUNDS="$arg";
      elif [ -z "$K" ]; then K="$arg";
      elif [ -z "$OUTDIR" ]; then OUTDIR="$arg";
      else echo "[wine-tune] ignoring extra arg: $arg" >&2; fi ;;
  esac
done
[ -n "$MAP" ] && [ "$ROUNDS" -gt 0 2>/dev/null ] && [ "$K" -gt 0 2>/dev/null ] && [ -n "$OUTDIR" ] \
  || { echo "usage: run-wine-tune.sh \"<map.scm>\" ROUNDS K OUTDIR [--env=FILE] [--seed=N] [--step=D] [--radius=D] [--engine=wine|openbw|sim]" >&2; exit 1; }
case "$ENGINE" in
  wine) BATCH="scripts/run-wine-batch.sh" ;;
  openbw|sim)
    echo "[wine-tune] engine '$ENGINE' is declared but not tunable yet:" >&2
    echo "[wine-tune] openbw writes no tweaker rows and its duel map does not tick;" >&2
    echo "[wine-tune] sim has no batch runner yet (defined next slice)." >&2
    exit 2 ;;
  *) echo "[wine-tune] unknown engine '$ENGINE' (wine|openbw|sim)" >&2; exit 2 ;;
esac
[ -n "$ENV_ARG" ] || ENV_ARG="--env=bwapi-data/AI/ENV-WINE-FAST"

cd "$ATLANTIS_DIR" || exit 1
mkdir -p "$OUTDIR"

# Compiled classes for the driver (same flags as run-tests.sh, Java 8
# bytecode; the game jar is built separately per game by run-wine-full).
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
mkdir -p out/production/Atlantis
echo "[wine-tune] compiling tooling classes"
javac --release 8 -nowarn -cp "$CP" -d out/production/Atlantis $(find src -name '*.java') \
  || { echo "[wine-tune] compile failed" >&2; exit 1; }

HOF="$OUTDIR/halloffame.txt"
FRAG="$OUTDIR/fragment.env"
PREV_BEST=""
r=0
while [ "$r" -lt "$ROUNDS" ]; do
  r=$(( r + 1 ))
  echo "[wine-tune] round $r/$ROUNDS (step=$STEP radius=$RADIUS seed=$SEED)"
  RDIR="$OUTDIR/round-$r"
  # shellcheck disable=SC2086
  if [ -s "$FRAG" ]; then
    # shellcheck disable=SC2046
    bash "$BATCH" "$MAP" "$K" "$RDIR" $(grep -v '^#' "$FRAG") ${ENV_ARG:+$ENV_ARG} > "$OUTDIR/round-$r.log" 2>&1 \
      || echo "[wine-tune] round $r: batch runner failed, continuing"
  else
    # shellcheck disable=SC2086
    bash "$BATCH" "$MAP" "$K" "$RDIR" ${ENV_ARG:+$ENV_ARG} > "$OUTDIR/round-$r.log" 2>&1 \
      || echo "[wine-tune] round $r: batch runner failed, continuing"
  fi
  if [ ! -f "$RDIR/manifest.txt" ]; then
    echo "[wine-tune] round $r: no manifest, stopping" >&2
    exit 1
  fi
  DRIVER_OUT="$(java -cp "out/production/Atlantis:$CP" atlantis.debug.tweaker.TunerDriver \
    --hof "$HOF" --manifest "$RDIR/manifest.txt" --out "$FRAG" \
    ${STEP:+--step=$STEP} ${RADIUS:+--radius=$RADIUS} --seed "$SEED" 2>&1)" || {
    echo "[wine-tune] round $r: proposer failed, stopping" >&2
    echo "$DRIVER_OUT" | tail -5 >&2
    exit 1
  }
  echo "$DRIVER_OUT" | grep -v "^BEST=" || true
  BEST="$(echo "$DRIVER_OUT" | grep -m1 "^BEST=" | cut -d= -f2)"
  STEP="$(echo "$DRIVER_OUT" | grep -m1 "^STEP=" | cut -d= -f2)"
  RADIUS="$(echo "$DRIVER_OUT" | grep -m1 "^RADIUS=" | cut -d= -f2)"
  if [ -n "$PREV_BEST" ] && [ "$BEST" != "unknown" ] && [ "$PREV_BEST" != "unknown" ]; then
    WORSE="$(awk -v b="$BEST" -v p="$PREV_BEST" 'BEGIN{print (b >= p) ? 1 : 0}')"
    if [ "$WORSE" = "1" ]; then
      STEP="$(awk -v s="$STEP" 'BEGIN{printf "%.4f", s / 2}')"
      RADIUS="$(awk -v r="$RADIUS" 'BEGIN{printf "%.4f", r / 2}')"
      echo "[wine-tune] best did not improve ($PREV_BEST -> $BEST): halving step=$STEP radius=$RADIUS"
    fi
  fi
  if [ "$BEST" != "unknown" ]; then PREV_BEST="$BEST"; fi
  SEED=$(( SEED + 1 ))
done

echo "[wine-tune] done: $ROUNDS rounds, archive $HOF"
