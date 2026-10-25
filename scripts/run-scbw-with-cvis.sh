#!/bin/bash
# Record a CherryVis trace for a docker (scbw.play) match.
#
# scbw.play itself has no --cvis flag (verified: no cvis/CHERRYVIS key in
# the installed scbw package) - the switch is the bot's ENV file, which
# the container mounts from the bot slot. This wrapper flips
# GENERATE_CHERRYVIS_REPLAY to true for one run and restores it after,
# so routine games pay no trace cost.
#
#   bash scripts/run-scbw-with-cvis.sh --headless --timeout 1000 \
#     --map="sscai/(3)TauCross.scx" --bots AtlantisP:P "Zealot Hell"
#
# After the game the trace lives in the newest game dir:
#   ~/.scbw/games/<GAME>/write_0/cherryvis_*.cvis/trace.json
# which is the layout the viewer opens. Needs one quiet box: never run
# alongside the test suite or another game (CONVENTIONS one-game rule).

set -u

BOT_ENV="${SCBW_BOT_ENV:-$HOME/.scbw/bots/AtlantisP/AI/ENV}"

if [ ! -f "$BOT_ENV" ]; then
  echo "[scbw-cvis] bot ENV not found: $BOT_ENV (set SCBW_BOT_ENV)" >&2
  exit 1
fi

BACKUP="$(mktemp)"
cp "$BOT_ENV" "$BACKUP"
restore() { cp "$BACKUP" "$BOT_ENV"; rm -f "$BACKUP"; }
trap restore EXIT

sed -i 's/^GENERATE_CHERRYVIS_REPLAY=.*/GENERATE_CHERRYVIS_REPLAY=true/' "$BOT_ENV"
grep -q '^GENERATE_CHERRYVIS_REPLAY=true' "$BOT_ENV" \
  || echo "GENERATE_CHERRYVIS_REPLAY=true" >> "$BOT_ENV"
echo "[scbw-cvis] trace ON for this run ($BOT_ENV)"

BEFORE="$(ls -t "$HOME/.scbw/games" 2>/dev/null | head -1)"

scbw.play "$@"
STATUS=$?

echo "[scbw-cvis] trace OFF (ENV restored)"
AFTER="$(ls -t "$HOME/.scbw/games" 2>/dev/null | head -1)"
if [ -n "${AFTER:-}" ] && [ "$AFTER" != "$BEFORE" ]; then
  echo "[scbw-cvis] game dir: $HOME/.scbw/games/$AFTER"
  find "$HOME/.scbw/games/$AFTER" -maxdepth 3 -name "trace.json" 2>/dev/null \
    | head -5 | sed 's/^/[scbw-cvis] trace: /'
fi

exit $STATUS
