#!/usr/bin/env bash
# Kill leftover game processes and clear stale transport state.
#
# Usage:
#   bash scripts/kill-leftovers.sh [--dry-run]
#
# Two Wine/OpenBW games on one box cross-attach and mutually kill
# (measured 2026-10-20: a client joined another live game), and a dead
# host's PID in the game table reads as "No server proc ID". Run this
# before starting a game when the box may not be yours alone.
# pkill -x only (never -f): a -f pattern matches this shell's own
# command line, so the shell SIGKILLs itself, reading as a hang.
# English-only output and comments (CONVENTIONS §1).

set -u

DRY_RUN="${1:-}"

kill_one() {
  if pgrep -x "$1" >/dev/null 2>&1; then
    if [ "$DRY_RUN" = "--dry-run" ]; then
      echo "[kill-leftovers] would kill: $1 ($(pgrep -x "$1" | tr '\n' ' '))"
    else
      pkill -9 -x "$1" 2>/dev/null || true
      echo "[kill-leftovers] killed: $1"
    fi
  fi
}

kill_one "StarCraft.exe"
kill_one "Chaoslauncher.exe"
kill_one "BWAPILauncher"
kill_one "Atlantis.jar"

if [ "$DRY_RUN" = "--dry-run" ]; then
  echo "[kill-leftovers] would clear: /tmp/bwapi_socket_* /dev/shm/bwapi_shared_memory_*"
else
  rm -f /tmp/bwapi_socket_* /dev/shm/bwapi_shared_memory_* 2>/dev/null || true
  echo "[kill-leftovers] transport cleared"
fi

if pgrep -x "StarCraft.exe" >/dev/null 2>&1 \
  || pgrep -x "Chaoslauncher.exe" >/dev/null 2>&1 \
  || pgrep -x "BWAPILauncher" >/dev/null 2>&1; then
  echo "[kill-leftovers] STILL RUNNING (see above) - refuse to call the box quiet" >&2
  exit 1
fi
echo "[kill-leftovers] box quiet"
