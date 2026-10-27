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
    # Anchor to argv[0]: an unanchored "/name" also matches the shell whose
    # command line merely mentions the path (measured: calling shell + process).
    # -i: Wine is inconsistent about the case of the exe name in argv[0]
    # ("ChaosLauncher.exe" vs "chaoslauncher.exe"), both occur in the wild.
    pgrep -if "^([^ ]*/)?${name//./\\.}( |$)" 2>/dev/null
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

# Same matcher as find_pids(), not a second hand-written copy: the old
# verification used an unanchored -f pattern, which matched THIS shell (its
# command line contains the filename) and reported "STILL RUNNING" even after a
# successful kill. One matcher, one source of truth.
remaining=""
for name in BWAPILauncher StarCraft.exe ChaosLauncher.exe Chaoslauncher.exe; do
  pids="$(find_pids "$name" | tr '\n' ' ')"
  [ -n "${pids// /}" ] && remaining="$remaining$name($pids) "
done
if [ -n "$remaining" ]; then
  if [ "$DRY_RUN" = "--dry-run" ]; then
    # Nothing was killed, so processes are expected to remain; reporting
    # STILL RUNNING here would be a false alarm.
    echo "[kill-leftovers] dry run: $remaining would be killed"
    exit 0
  fi
  echo "[kill-leftovers] STILL RUNNING: $remaining- refuse to call the box quiet" >&2
  exit 1
fi

# The game lock can outlive the game processes: Wine children inherit
# the runner's flock FD, so a dead run's services.exe/winedevice.exe
# keep holding /tmp/atlantis-wine-game.lock with no game alive
# (measured 2026-10-27: the next run refused on a "quiet" box).
# Killing is deliberately NOT automatic here: a runner in early
# startup holds the same lock legitimately, and only a live game's
# absence (checked above) tells stale from live. Report and refuse;
# the operator kills the named holders after confirming no game runs.
LOCK_FILE="/tmp/atlantis-wine-game.lock"
if [ -f "$LOCK_FILE" ]; then
  holders="$(fuser "$LOCK_FILE" 2>/dev/null || true)"
  holders="$(echo "$holders" | sed 's/^[^:]*: *//')"
  if [ -n "${holders// /}" ]; then
    echo "[kill-leftovers] game lock still held by: $holders" >&2
    echo "[kill-leftovers] box NOT quiet - confirm no game runs, kill the holders, retry" >&2
    exit 1
  fi
fi

echo "[kill-leftovers] box quiet"
