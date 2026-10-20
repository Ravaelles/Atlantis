#!/usr/bin/env python3
"""Per-unit fight table from a twin stream (EVENTS.jsonl).

Formation fights are won by shape and cooperation, so the interesting
read is per unit, not per twin: how far each unit stood from the
nearest enemy, at what health, and when it died. UNIT_TRACE rows carry
the samples (one per 30 frames while fighting), UNIT_DIED rows the
deaths. Output is one line per our unit: frames alive, death frame or
SURVIVED, min/avg health, avg distance to the nearest enemy, and the
distance at death.
Usage:
  scripts/fight-units.py out/wine/EVENTS.jsonl
  scripts/fight-units.py /sc-ai/BOTS/AtlantisOpenBW/E2E_LOGS/latest/EVENTS.jsonl
"""
import json
import sys
from collections import defaultdict


def load(path):
    rows = []
    with open(path) as f:
        for line in f:
            line = line.strip()
            if line.startswith("{"):
                rows.append(json.loads(line))
    return rows


def unit_table(rows, with_killer=False):
    """Per-unit table lines (id, type, alive, death, hp, dist, optional
    killer suspect). Shared with summarize-game.py - one implementation,
    two entry points."""
    samples = defaultdict(list)
    deaths = {}
    for r in rows:
        key = r.get("key", "")
        if key == "UNIT_TRACE":
            samples[r.get("unit")].append(r)
        elif key == "UNIT_DIED":
            deaths[r.get("unit")] = r
    if not samples and not deaths:
        return ["no UNIT_TRACE/UNIT_DIED rows (fight had no traced units)"]
    out = ["id   type              alive   died_at  min_hp  avg_hp  avg_dist  last_dist"
             + ("  killer" if with_killer else "")]
    for uid in sorted(set(list(samples) + list(deaths))):
        rows = sorted(samples.get(uid, []), key=lambda r: r.get("frame", 0))
        first = rows[0].get("frame", 0) if rows else "?"
        death = deaths.get(uid, {}).get("frame", "-")
        status = "SURVIVED" if uid not in deaths else "died@" + str(death)
        hps = [r.get("hp", 0) + r.get("shields", 0) for r in rows]
        dists = [r.get("dist", -1) / 10.0 for r in rows if 0 <= r.get("dist", -1) <= 1000]
        min_hp = min(hps) if hps else "?"
        avg_hp = round(sum(hps) / len(hps), 1) if hps else "?"
        avg_d = round(sum(dists) / len(dists), 2) if dists else "?"
        last_raw = rows[-1].get("dist", -1) if rows else -1
        last_d = (last_raw / 10.0) if 0 <= last_raw <= 1000 else "?"
        typ = (rows[0].get("type", "?") if rows
               else deaths.get(uid, {}).get("type", "?"))
        line = ("%-4s %-17s %-7s %-8s %-7s %-7s %-9s %s"
                % (uid, typ, first, status, min_hp, avg_hp, avg_d, last_d))
        if with_killer:
            kd = deaths.get(uid, {})
            line += "  %s(%s)" % (kd.get("killer", "-"), kd.get("killerType", "-"))
        out.append(line)
    return out


def main(path):
    for line in unit_table(load(path)):
        print(line)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1])
