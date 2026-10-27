#!/usr/bin/env bash
# Opening gate for real games: the build order is played in order and the
# bank never hoards before supply 50.
#
# WHY A SCRIPT, NOT A JUNIT TEST
#   This needs a truly working game: mining income, builder travel, placement
#   and the dispatcher all have to run for thousands of frames. The stub-world
#   suite models none of that (minerals are set by hand there), and a real Wine
#   game takes ~10-16 minutes on an exclusive machine (one game at a time, no
#   suite while it runs). So this runs AFTER a game, over its logs - the same
#   shape as scripts/parse-run.sh and the WO-144 adjudication. The in-suite
#   half of the same rule already exists as plan-shape pins
#   (BuildOrderGoalsTest, OpeningDoesNotHoardMineralsTest); this is the half
#   that watches the played game.
#   OpenBW is deliberately not the vehicle (owner: Wine only, #216 hands-off);
#   the old StarEngine economy sim is gone (removed 2026-10-04, the current
#   sim steps fights, not minerals).
#
# WHAT IT CHECKS (all three must hold, else exit 1)
#   1. ORDER: every production row of the build order appears in the played
#      game as a subsequence, in file order. Extras are fine (dynamic Pylons,
#      army units) - order violations are not.
#   2. NO INTRUDER before the first Gateway: a finished building whose type
#      is nowhere in the build order and that completes before the first
#      Gateway is the Forge-before-Gateway class (measured LIVE: orphan Forge
#      f2112-2746 in Zealot-into-Goon, which has no Forge). Expansions and
#      anything after the first Gateway are dynamic territory, not intruders.
#   3. NO HOARD: at every warp/train below --supply-until (default 50) the
#      mineral bank is at most --max-bank (default 550). A bank above that
#      while the opening is still playing means the engine saves for
#      something it should not.
#
# WHAT IT READS (all produced by a normal Wine run)
#   WINE_LOG       the "Use build order: `Name`" line (or --bo to override)
#   BUILDINGS.json one row per warp/train: frame, kind, type, tile,
#                  supplyUsed/supplyTotal, minerals, order
#
# OUTPUT: one PASS/FAIL line plus the evidence lines. Exit 0 pass, 1 fail,
#   2 bad usage / missing dir or files.
#
# Usage:
#   scripts/assert-wine-opening.sh <run-dir> [--bo <name>] [--max-bank N]
#                                        [--supply-until N]
set -uo pipefail

MAX_BANK=550
SUPPLY_UNTIL=50
BO_NAME=""

usage() {
    echo "usage: $0 <run-dir> [--bo <name>] [--max-bank N] [--supply-until N]" >&2
    exit 2
}

DIR="${1:-}"
[ -z "$DIR" ] || [ ! -d "$DIR" ] && usage
shift || true

while [ "$#" -gt 0 ]; do
    case "$1" in
        --bo) BO_NAME="${2:-}"; shift 2 ;;
        --max-bank) MAX_BANK="${2:-}"; shift 2 ;;
        --supply-until) SUPPLY_UNTIL="${2:-}"; shift 2 ;;
        *) usage ;;
    esac
done

REPO="$(cd "$(dirname "$0")/.." && pwd)"

if [ -z "$BO_NAME" ]; then
    BO_NAME="$(grep -m1 -o 'Use build order: `[^`]*`' "$DIR/WINE_LOG" 2>/dev/null | sed 's/Use build order: `//; s/`//')"
    [ -z "$BO_NAME" ] && { echo "FAIL: no build order in WINE_LOG and no --bo given"; exit 2; }
fi

BO_FILE="$REPO/bwapi-data/AI/build_orders/Protoss/$BO_NAME.txt"
[ -f "$BO_FILE" ] || { echo "FAIL: build order file missing: $BO_FILE"; exit 2; }
[ -f "$DIR/BUILDINGS.json" ] || { echo "FAIL: no BUILDINGS.json in $DIR"; exit 2; }

python3 - "$DIR/BUILDINGS.json" "$BO_FILE" "$BO_NAME" "$MAX_BANK" "$SUPPLY_UNTIL" <<'PYEOF'
import json, re, sys

buildings_path, bo_path, bo_name, max_bank, supply_until = sys.argv[1:6]
max_bank, supply_until = int(max_bank), int(supply_until)

# ---- build order rows: "23 - Gateway", "12 - Gateway", "22 - Cybernetics Core" ----
NAME_TO_TYPE = {
    'pylon': 'Pylon', 'gateway': 'Gateway', 'forge': 'Forge',
    'cybernetics core': 'CyberneticsC', 'assimilator': 'Assimilator',
    'nexus': 'Nexus', 'photon cannon': 'Photon_Cannon',
    'citadel of adun': 'Citadel_of_Adun', 'robotics facility': 'Robotics_Facility',
    'observatory': 'Observatory', 'stargate': 'Stargate',
    'zealot': 'Zealot', 'dragoon': 'Dragoon', 'probe': 'Probe',
    'dark templar': 'Dark_Templar', 'archon': 'Archon',
}
bo = []
for line in open(bo_path):
    line = line.strip()
    if not line or line.startswith('//') or line.startswith('#') or line.startswith('@'):
        continue
    m = re.match(r'^(\d+)\s*-\s*(.+?)\s*$', line)
    if not m:
        continue
    supply, what = int(m.group(1)), m.group(2).strip()
    what = re.sub(r'\s*[@\[].*$', '', what).strip()
    what = re.sub(r'\s+x\d+$', '', what, flags=re.IGNORECASE).strip()
    key = NAME_TO_TYPE.get(what.lower(), what.replace(' ', '_'))
    bo.append((supply, key, what))
bo_types = set(t for _, t, _ in bo)

rows = json.load(open(buildings_path))
rows = sorted([r for r in rows if isinstance(r.get('frame'), int)], key=lambda r: r['frame'])

fails = []

# ---- 2. intruder before the first Gateway (buildings only; army units are dynamic) ----
gate_frames = [r['frame'] for r in rows if r.get('kind') == 'building' and r.get('type') == 'Gateway']
first_gate = min(gate_frames) if gate_frames else None
if first_gate is None:
    fails.append('no Gateway ever completed (BO "%s" wants %d)' % (bo_name, sum(1 for _, t, _ in bo if t == 'Gateway')))
else:
    for r in rows:
        if r.get('kind') != 'building' or r['frame'] >= first_gate:
            continue
        if r.get('type') not in bo_types:
            fails.append('intruder before first Gateway f%d: %s at %s (not in BO "%s")'
                         % (r['frame'], r.get('type'), r.get('tile'), bo_name))

# ---- 1. order: BO rows as a subsequence of completions (buildings and units) ----
need = 0
for r in rows:
    if need < len(bo) and r.get('type') == bo[need][1]:
        need += 1
if need < len(bo):
    missing = ['%s@%s' % (t, s) for s, t, _ in bo[need:need + 5]]
    fails.append('BO order broken after %d/%d rows, next missing: %s' % (need, len(bo), ', '.join(missing)))

# ---- 3. hoard: bank cap below the supply horizon ----
worst = (0, None)
for r in rows:
    try:
        supply = int(r.get('supplyUsed', 999))
    except (TypeError, ValueError):
        continue
    if supply >= supply_until:
        continue
    bank = r.get('minerals', 0) or 0
    if bank > worst[0]:
        worst = (bank, r)
if worst[0] > max_bank:
    r = worst[1]
    fails.append('hoard before supply %d: bank %d at f%d (%s, supply %s/%s)'
                 % (supply_until, worst[0], r.get('frame'), r.get('type'),
                    r.get('supplyUsed'), r.get('supplyTotal')))

matched = '%d/%d BO rows in order' % (need, len(bo))
bankinfo = 'max bank below supply %d: %d (cap %d)' % (supply_until, worst[0], max_bank)
gateinfo = ('first Gateway f%d' % first_gate) if first_gate is not None else 'no Gateway'
if fails:
    print('FAIL opening "%s" [%s | %s | %s]' % (bo_name, matched, bankinfo, gateinfo))
    for f in fails:
        print('  - ' + f)
    sys.exit(1)
print('PASS opening "%s" [%s | %s | %s]' % (bo_name, matched, bankinfo, gateinfo))
PYEOF
