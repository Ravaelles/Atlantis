#!/bin/bash
# Play one StarCraft map under Wine, headless-fast by default.
#
# Usage:
#   bash scripts/run-wine-ums.sh "1Drag_v_1Zeal.scm" [fast]
#
#   map  : bare map file name (resolved under the game's maps/ tree,
#          e.g. "1Drag_v_1Zeal.scm") or a maps/-relative path.
#   fast : 1 (default) = unattended: speed 0, frameskip 500, GUI off.
#          0 = watchable game (normal speed, GUI on).
#
# Thin wrapper over scripts/run-wine-full.sh (the tested core: jar
# rebuild, ENV borrow-and-restore, teardown). Exists so a Wine launch
# is one obvious command with an explicit speed flag, mirroring
# scripts/run-openbw-ums.sh (map + debug). Owner-run only
# (CONVENTIONS §14); models run it only with the owner's per-case
# go-ahead.
#
# After the game it reports the honest throughput (game frames per
# wall second from GameSummary): speed 0 only goes as fast as the
# bot's own frame cost under Wine allows (~24 fps realtime means the
# bot consumes the whole frame budget - BWAPI turn-sync, not the
# speed setting).

set -u

MAP="${1:-}"
FAST="${2:-1}"
[ -n "$MAP" ] || { echo "usage: run-wine-ums.sh \"<map.scm>\" [fast=1]" >&2; exit 1; }
[ "$FAST" = "0" ] || [ "$FAST" = "1" ] || { echo "fast must be 0 or 1, got: $FAST" >&2; exit 1; }

export WINE_FAST="$FAST"
if [ "$FAST" = "1" ]; then
  echo "[wine-ums] map=$MAP speed: FAST (0, frameskip 500, GUI off)"
else
  echo "[wine-ums] map=$MAP speed: WATCHABLE (normal speed, GUI on)"
fi

bash "$(dirname "$0")/run-wine-full.sh" "$MAP"
EXIT=$?

# Honest throughput from the game summary (frames / wall seconds).
LOG="out/wine/WINE_LOG"
if [ -f "$LOG" ]; then
  FRAMES="$(grep -oE "Total frames: [0-9]+" "$LOG" | tail -1 | grep -oE "[0-9]+")"
  SECONDS_WALL="$(grep -oE "Total time: [0-9]+" "$LOG" | tail -1 | grep -oE "[0-9]+")"
  if [ -n "$FRAMES" ] && [ -n "$SECONDS_WALL" ] && [ "$SECONDS_WALL" -gt 0 ]; then
    echo "[wine-ums] throughput: $(( FRAMES / SECONDS_WALL )) frames/s wall ($FRAMES frames in ${SECONDS_WALL}s)"
  fi
  if [ "$FAST" = "1" ]; then
    if grep -q "WINE_FAST: speed 0" "$LOG"; then
      echo "[wine-ums] speed flag confirmed in game log (frameskip 500)"
    elif grep -q "PARAM_TWEAKER: speed 0" "$LOG"; then
      echo "[wine-ums] tweaker speed confirmed in game log (frameskip 500)"
    else
      echo "[wine-ums] WARNING: speed lines missing from game log - flags did not reach the game"
    fi
  fi
fi
exit "$EXIT"
