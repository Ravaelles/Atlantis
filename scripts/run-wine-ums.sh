#!/bin/bash
# Play one StarCraft map under Wine.
#
# Usage:
#   bash scripts/run-wine-ums.sh "1Drag_v_1Zeal.scm" [--env=FILE] [--headless]
#
#   map   : bare map file name (resolved under the game's maps/ tree,
#           e.g. "1Drag_v_1Zeal.scm") or a maps/-relative path.
#   --env : explicit env file for the bot, e.g.
#           --env=bwapi-data/AI/ENV-WINE-FAST for unattended speed
#           (speed 0, frameskip 1000, GUI off). Relative paths resolve
#           against the repo root. Without it the bot reads the shared
#           ENV (your watched-game settings, untouched by this script).
#   --headless : run the whole session under Xvfb :99 - nothing ever
#           maps on the desktop (no resolution change, no window).
#           Visible by default (owner 2026-10-21: IDE runs must
#           show the game).
#
# Thin wrapper over scripts/run-wine-full.sh (the tested core: jar
# rebuild, teardown). Exists so a Wine launch is one obvious command
# with an explicit env file, mirroring scripts/run-openbw-ums.sh
# (map + debug). This script NEVER writes to any ENV file (owner:
# per-run flags must not leak into watched games). Owner-run only
# (CONVENTIONS §14); models run it only with the owner's per-case
# go-ahead.
#
# After the game it reports the honest throughput (game frames per
# wall second from GameSummary): speed 0 only goes as fast as the
# bot's own frame cost under Wine allows (~24 fps realtime means the
# bot consumes the whole frame budget - BWAPI turn-sync, not the
# speed setting).

set -u

MAP=""
ENV_ARG=""
HIDDEN_ARG=""
for arg in "$@"; do
  case "$arg" in
    --env=*) ENV_ARG="$arg" ;;
    --headless|--hidden) HIDDEN_ARG="--headless" ;;
    --visible) HIDDEN_ARG="" ;;
    *) [ -z "$MAP" ] && MAP="$arg" || { echo "[wine-ums] ignoring extra arg: $arg" >&2; } ;;
  esac
done
[ -n "$MAP" ] || { echo "usage: run-wine-ums.sh \"<map.scm>\" [--env=FILE] [--headless]" >&2; exit 1; }

# Visible by default (owner 2026-10-21: IDE runs must show the game).
# --headless runs the whole session under Xvfb :99 instead.
if [ -n "$HIDDEN_ARG" ]; then
  echo "[wine-ums] hidden Wine desktop (Xvfb :99, invisible)"
  export HIDDEN=1
else
  export HIDDEN=0
fi

if [ -n "$ENV_ARG" ]; then
  echo "[wine-ums] map=$MAP env=$ENV_ARG"
else
  echo "[wine-ums] map=$MAP env=(shared, untouched)"
fi

if [ -n "$ENV_ARG" ]; then
  # shellcheck disable=SC2086
  bash "$(dirname "$0")/run-wine-full.sh" "$MAP" "$ENV_ARG" $HIDDEN_ARG
else
  # shellcheck disable=SC2086
  bash "$(dirname "$0")/run-wine-full.sh" "$MAP" $HIDDEN_ARG
fi
EXIT=$?

# Honest throughput from the game summary (frames / wall seconds).
LOG="out/wine/WINE_LOG"
if [ -f "$LOG" ]; then
  FRAMES="$(grep -oE "Total frames: [0-9]+" "$LOG" | tail -1 | grep -oE "[0-9]+")"
  SECONDS_WALL="$(grep -oE "Total time: [0-9]+" "$LOG" | tail -1 | grep -oE "[0-9]+")"
  if [ -n "$FRAMES" ] && [ -n "$SECONDS_WALL" ] && [ "$SECONDS_WALL" -gt 0 ]; then
    echo "[wine-ums] throughput: $(( FRAMES / SECONDS_WALL )) frames/s wall ($FRAMES frames in ${SECONDS_WALL}s)"
  fi
  if grep -q "WINE_SUPER_FAST_MODE: speed 0" "$LOG"; then
    echo "[wine-ums] super-fast mode confirmed in game log (frameskip 1000)"
  elif grep -q "PARAM_TWEAKER: speed 0" "$LOG"; then
    echo "[wine-ums] tweaker speed confirmed in game log (frameskip 500)"
  fi
fi
exit "$EXIT"
