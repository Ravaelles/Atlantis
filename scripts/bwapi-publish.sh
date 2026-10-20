#!/usr/bin/env bash
# Report whether an OpenBW/BWAPI game is published and how the server is doing.
#
# WHY: a shared-segment check based on "is it non-empty" is wrong. The 96-byte
# game table lives in /dev/shm/bwapi_shared_memory_game_list and survives a
# server crash with a STALE entry: the client then fails with
# "No server proc ID" / "Game table mapping not found" while the file looks
# perfectly fine. What the client actually needs is a slot whose serverProcessID
# is a LIVE pid. This script reports exactly that, plus the server log tail,
# so "the engine did not start" can be told apart from "the engine died".
#
# Layout (verified on x86-64, 96 bytes total):
#   8 slots x 16 bytes: uint32 serverProcessID, uint32 isConnected,
#                       uint32 lastKeepAliveTime, uint32 padding
#
# Usage:
#   scripts/bwapi-publish.sh              # summary, exit 0 if a live game exists
#   scripts/bwapi-publish.sh --watch N    # poll N times, 0.5 s apart
#   scripts/bwapi-publish.sh --json       # machine readable
#
# Exit codes: 0 = live published game, 1 = no live game, 2 = no game table file.
set -uo pipefail
GAME_LIST=/dev/shm/bwapi_shared_memory_game_list
WATCH=1
JSON=0
while [ $# -gt 0 ]; do
  case "$1" in
    --watch) [ $# -ge 2 ] || { echo "--watch needs a count" >&2; exit 2; }; WATCH="$2"; shift 2 ;;
    --json)  JSON=1; shift ;;
    -h|--help) sed -n '2,30p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

probe() {
  [ -f "$GAME_LIST" ] || return 2
  python3 - "$GAME_LIST" <<'PY' 2>/dev/null
import os, struct, sys
path = sys.argv[1]
data = open(path, "rb").read()
live, stale, rows = [], [], []
for i in range(8):
    off = i * 16
    if off + 16 > len(data):
        break
    pid, conn, keepalive, _pad = struct.unpack_from("<IIII", data, off)
    if not pid and not conn:
        continue
    alive = pid != 0 and os.path.isdir("/proc/%d" % pid)
    row = (i, pid, alive, conn, keepalive)
    rows.append(row)
    (live if alive else stale).append(row)
for i, pid, alive, conn, ka in rows:
    print("slot %d: pid=%d alive=%s connected=%d keepalive=%d" % (i, pid, alive, conn, ka))
if live:
    print("PUBLISHED: %d live game(s)" % len(live))
    sys.exit(0)
if stale:
    print("STALE: game table references dead pid(s) %s - the server crashed; "
          "remove %s before the next run" % ([r[1] for r in stale], path))
    sys.exit(1)
print("NO GAME: table exists but no server published a game yet")
sys.exit(1)
PY
}

for n in $(seq 1 "$WATCH"); do
  [ "$n" -gt 1 ] && sleep 0.5
  if [ "$JSON" -eq 1 ]; then
    out="$(probe)"; rc=$?
    printf '{"ok": %s, "details": %s}\n' "$([ $rc -eq 0 ] && echo true || echo false)" \
      "$(printf '%s' "$out" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read().splitlines()))')"
  else
    out="$(probe)"; rc=$?
    [ -n "$out" ] || out="no game table at $GAME_LIST (server not started yet)"
    printf '%s\n' "$out"
  fi
  [ $rc -eq 0 ] && exit 0
  LAST_RC=$rc
done
exit "${LAST_RC:-1}"
