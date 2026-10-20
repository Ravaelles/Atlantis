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

# pgrep -x matches the kernel comm name, which is truncated to 15 characters
# (TASK_COMM_LEN-1). "BWAPILauncher" is 13 chars and works with -x;
# "ChaosLauncher.exe" is 17 and therefore NEVER matches with -x. For those,
# use pgrep -f with the exe path anchored by a leading slash, which cannot
# match this script's own argv (it contains a bare name, not "/ChaosLauncher").
# Both capitalizations are covered: Wine was sighted reporting the lowercase
# variant (chaoslauncher/Chaoslauncher.exe).
find_pids() {
  local name="$1"
  if [ "${#name}" -le 15 ]; then
    pgrep -x "$name" 2>/dev/null
  else
    pgrep -f "/$name([[:space:]]|$)" 2>/dev/null
  fi
}
kill_one() {
  local pids; pids="$(find_pids "$1" | tr '\n' ' ')"
  [ -n "${pids// /}" ] || return 0
  if [ "$DRY_RUN" = "--dry-run" ]; then
    echo "[kill-leftovers] would kill: $1 ($pids)"
  else
    # shellcheck disable=SC2086
    kill -9 $pids 2>/dev/null || true
    echo "[kill-leftovers] killed: $1 ($pids)"
  fi
}

kill_one "StarCraft.exe"
kill_one "ChaosLauncher.exe"
kill_one "Chaoslauncher.exe"  # Wine sometimes reports the exe name with a lowercase l
kill_one "BWAPILauncher"
kill_one "Atlantis.jar"

if [ "$DRY_RUN" = "--dry-run" ]; then
  echo "[kill-leftovers] would clear: /tmp/bwapi_socket_* /dev/shm/bwapi_shared_memory_*"
else
  rm -f /tmp/bwapi_socket_* /dev/shm/bwapi_shared_memory_* 2>/dev/null || true
  echo "[kill-leftovers] transport cleared"
fi

if pgrep -x "StarCraft.exe" >/dev/null 2>&1 \
  || pgrep -f "/ChaosLauncher.exe([[:space:]]|$)" >/dev/null 2>&1 \
  || pgrep -f "/Chaoslauncher.exe([[:space:]]|$)" >/dev/null 2>&1 \
  || pgrep -x "BWAPILauncher" >/dev/null 2>&1; then
  echo "[kill-leftovers] STILL RUNNING (see above) - refuse to call the box quiet" >&2
  exit 1
fi
echo "[kill-leftovers] box quiet"
