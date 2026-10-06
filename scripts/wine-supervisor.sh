#!/usr/bin/env bash
# Wine supervisor: watch the bot client's log for HELLO_WORLD (the BWAPI
# attached marker printed by Atlantis.onStart). If it does not appear within
# the grace period, kill StarCraft + ChaosLauncher so the next start is clean,
# and exit non-zero so the caller knows the attach failed.
#
# Usage: wine-supervisor.sh <log-file> [grace-seconds] (default 90)
set -u

LOG="${1:?usage: wine-supervisor.sh <bot-log-file> [grace-seconds]}"
GRACE="${2:-90}"

echo "[supervisor] watching $LOG for HELLO_WORLD (grace ${GRACE}s)"
END=$(( $(date +%s) + GRACE ))

while [ "$(date +%s)" -lt "$END" ]; do
  if grep -q "HELLO_WORLD" "$LOG" 2>/dev/null; then
    echo "[supervisor] HELLO_WORLD found - bot is attached and playing."
    exit 0
  fi
  sleep 2
done

echo "[supervisor] NO HELLO_WORLD after ${GRACE}s - killing game and launcher."
# NOTE: pkill -x compares against comm, which the kernel truncates to 15
# characters - "Chaoslauncher.exe" (17) never matches, measured 2026-10-06.
# Without -x the pattern is matched against comm only (not the command line),
# so the calling shell is not matched either.
pkill -9 -x StarCraft.exe
pkill -9 Chaoslauncher
wineserver -k
exit 1
