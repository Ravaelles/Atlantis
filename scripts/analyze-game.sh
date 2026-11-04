#!/usr/bin/env bash
# Post-game analysis for the token budget (owner ask 2026-11-04):
# run it after any game and read ONLY the counts - drill into the
# per-key files only when a count is non-zero.
#
# Usage: scripts/analyze-game.sh [game-dir-or-events-file]
#   No arg  -> newest OpenBW game, else newest Wine out/wine run.
#   Prints:  HIGH=n MED=m CRIT=c LOW=l warps=n offPlan=m sameTileDups=n status=ok|issues
#   Writes:  <game>/ANALYSIS_SUMMARY.json
#            <game>/analysis/by-key/<SEV>_<KEY>.json   (one file per severity+key)
#
# What counts as an error (and why twin-MEDIUM does not):
# - twins: rows whose lvl is HIGH or CRITICAL. MEDIUM twins are
#   telemetry/chatter by construction (the severity policy says MED
#   is the default chatter level); counting them would report thousands
#   of rows per game and drown the signal. See LogKey javadoc.
# - console: lines tagged [MED]/[HIGH]/[CRIT] (CONVENTIONS §27: every
#   console line carries a tag, and console lines are curated - the
#   audit verdicts, drains, cancels - not telemetry).
# - construction verdicts: the bot's own end-of-game lines
#   ("[MED] Construction issue detected: ...", "[LOW] No issues in
#   Build Order execution detected.") plus same-tile warp repeats
#   from BUILDINGS.json.
set -u

GAME="${1:-}"
if [ "$GAME" = "--json" ]; then GAME=""; fi

EVENTS=""
BUILDINGS=""
BOTLOG=""
# Set only when the caller names a game explicitly (path or dir): the
# auto-detected newest game and the live tree keep the global fallbacks.
EXPLICIT_GAME=""
if [ -n "$GAME" ]; then
  EXPLICIT_GAME=1
fi
if [ -f "$GAME" ]; then
  EVENTS="$GAME"
  GAME="$(dirname "$GAME")"
fi
if [ -z "$EVENTS" ]; then
  if [ -n "$GAME" ]; then
    for cand in "$GAME/write_0/EVENTS.jsonl" "$GAME/EVENTS.jsonl" \
                "$GAME/bwapi-data/write/EVENTS.jsonl"; do
      [ -f "$cand" ] && EVENTS="$cand" && break
    done
  else
    EVENTS="$(ls -t "$HOME/.scbw/games"/GAME_*/write_0/EVENTS.jsonl 2>/dev/null | head -1)"
    [ -z "$EVENTS" ] && EVENTS="$(ls -t /sc-ai/Atlantis/out/wine_runs/*/EVENTS.jsonl 2>/dev/null | head -1)"
    [ -z "$EVENTS" ] && [ -f /sc-ai/Atlantis/bwapi-data/write/EVENTS.jsonl ] \
      && EVENTS=/sc-ai/Atlantis/bwapi-data/write/EVENTS.jsonl
  fi
fi
[ -n "$EVENTS" ] && [ -f "$EVENTS" ] || { echo "analyze-game: no EVENTS.jsonl found" >&2; exit 2; }
GAME="$(cd "$(dirname "$EVENTS")" && pwd)"
case "$GAME" in
  */write_0) GAME="$(dirname "$GAME")" ;;
esac

if [ -f "$GAME/write_0/BUILDINGS.json" ]; then BUILDINGS="$GAME/write_0/BUILDINGS.json";
elif [ -f "$GAME/BUILDINGS.json" ]; then BUILDINGS="$GAME/BUILDINGS.json";
elif [ -f /sc-ai/Atlantis/bwapi-data/write/BUILDINGS.json ]; then BUILDINGS=/sc-ai/Atlantis/bwapi-data/write/BUILDINGS.json; fi

# Console logs are game-local only: the shared out/wine/* copies belong
# to whatever Wine game ran last and must never leak into another game's
# verdict (measured: stale WINE_DEBUG rows attributed to a finished OpenBW
# game). They are used only when analyzing the live tree with no game dir.
BOTLOG=""
for cand in "$GAME/logs_0/bot.log" "$GAME/WINE_LOG"; do
  if [ -f "$cand" ]; then BOTLOG="$cand"; break; fi
done
BOTLOG2=""
if [ -f "$GAME/WINE_DEBUG" ]; then BOTLOG2="$GAME/WINE_DEBUG"; fi
if [ -z "$BOTLOG" ] && [ -z "$EXPLICIT_GAME" ]; then
  [ -f /sc-ai/Atlantis/out/wine/WINE_LOG ] && BOTLOG=/sc-ai/Atlantis/out/wine/WINE_LOG
  [ -f /sc-ai/Atlantis/out/wine/WINE_DEBUG ] && BOTLOG2=/sc-ai/Atlantis/out/wine/WINE_DEBUG
fi

export ANALYZE_EVENTS="$EVENTS" ANALYZE_BUILDINGS="$BUILDINGS"
export ANALYZE_BOTLOG="$BOTLOG" ANALYZE_BOTLOG2="$BOTLOG2" ANALYZE_GAME="$GAME"

python3 - <<'PYEOF'
import json, os, re, sys
from collections import Counter, defaultdict

events = os.environ['ANALYZE_EVENTS']
buildings = os.environ.get('ANALYZE_BUILDINGS') or ''
botlogs = [p for p in (os.environ.get('ANALYZE_BOTLOG') or '',
                       os.environ.get('ANALYZE_BOTLOG2') or '') if p]
game = os.environ.get('ANALYZE_GAME') or os.path.dirname(events)

def load_rows(path):
    rows = []
    try:
        with open(path, errors='replace') as fh:
            for line in fh:
                line = line.strip()
                if not line:
                    continue
                try:
                    rows.append(json.loads(line))
                except ValueError:
                    continue
    except OSError:
        pass
    return rows

sev_of = {'LOW': 'LOW', 'MED': 'MED', 'MEDIUM': 'MED', 'HIGH': 'HIGH',
          'CRIT': 'CRIT', 'CRITICAL': 'CRIT'}

by_key = defaultdict(list)
counts = Counter()

for r in load_rows(events):
    lvl = sev_of.get(str(r.get('lvl', 'MED')).upper(), 'MED')
    if lvl in ('HIGH', 'CRIT'):
        key = '[%s] %s' % (lvl, r.get('key', '?'))
        slim = {k: v for k, v in r.items() if k in (
            'frame', 'time', 'key', 'type', 'cid', 'oid', 'tile', 'prevTile',
            'reason', 'result', 'builder', 'free', 'coming', 'minerals')}
        slim.setdefault('frame', r.get('frame'))
        by_key[key].append(slim)
        counts[lvl] += 1

cline = re.compile(r'^\[(LOW|MED|HIGH|CRIT)\]\s*(.*)$')
for path in botlogs:
    try:
        fh = open(path, errors='replace')
    except OSError:
        continue
    with fh:
        for line in fh:
            line = re.sub(r'\x1b\[[0-9;]*m', '', line.rstrip('\n'))
            m = cline.match(line.strip())
            if not m:
                continue
            sev, text = m.group(1), m.group(2).strip()
            if sev == 'LOW' and not re.search(
                    r'No issues in Build Order|No issues in build', text):
                continue
            if sev == 'LOW':
                key = '[LOW] BUILD_ORDER_CLEAN'
            elif text.startswith('Construction issue detected'):
                key = '[MED] CONSTRUCTION_ISSUE'
            elif text.startswith('BO ORDER MISMATCH'):
                key = '[MED] CONSTRUCTION_ISSUE'
            else:
                key = '[%s] CONSOLE' % sev
            by_key[key].append({'text': text[:300]})
            counts[sev] += 1

warps = []
same_tile_dups = []
off_plan = sum(len(v) for k, v in by_key.items() if k == '[MED] CONSTRUCTION_ISSUE')
try:
    if buildings:
        for r in json.load(open(buildings)):
            if isinstance(r, dict) and r.get('kind') == 'building':
                warps.append(r)
        seen = {}
        for r in warps:
            t = (r.get('type'), str(r.get('tile')))
            if t in seen:
                same_tile_dups.append({'type': t[0], 'tile': t[1],
                                       'frames': [seen[t], r.get('frame')]})
            else:
                seen[t] = r.get('frame')
except (OSError, ValueError):
    pass

status = 'ok'
if counts['HIGH'] or counts['CRIT'] or off_plan or same_tile_dups:
    status = 'issues'

keydir = os.path.join(game, 'analysis', 'by-key')
os.makedirs(keydir, exist_ok=True)
for key, rows in sorted(by_key.items()):
    safe = re.sub(r'[^A-Za-z0-9_.-]+', '_', key).strip('_')
    with open(os.path.join(keydir, safe + '.json'), 'w') as fh:
        json.dump(rows, fh)
summary = {
    'errors': {k: counts.get(k, 0) for k in ('HIGH', 'MED', 'CRIT', 'LOW')},
    'byKey': {k: len(v) for k, v in sorted(by_key.items())},
    'construction': {
        'warps': len(warps),
        'offPlan': off_plan,
        'sameTileDups': same_tile_dups,
    },
    'status': status,
}
with open(os.path.join(game, 'ANALYSIS_SUMMARY.json'), 'w') as fh:
    json.dump(summary, fh, indent=1, sort_keys=True)

print('HIGH=%d MED=%d CRIT=%d LOW=%d warps=%d offPlan=%d sameTileDups=%d status=%s' % (
    counts.get('HIGH', 0), counts.get('MED', 0), counts.get('CRIT', 0),
    counts.get('LOW', 0), len(warps), off_plan, len(same_tile_dups), status))
print('summary: %s' % os.path.join(game, 'ANALYSIS_SUMMARY.json'))
PYEOF
