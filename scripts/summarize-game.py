#!/usr/bin/env python3
"""One-game digest from a Wine run directory (or the live out/wine/).

Reads run.txt (verdict), BUILDINGS.json (roster + tech times),
EVENTS.jsonl (twins) and WINE_LOG (bot summary) and prints the datum
block: result, roster, per-unit table, refusal gates, tech gates,
kite/shot/hit totals. This is the readout used after every macro
game - run it, paste the verdict-relevant lines, archive the dir.
Usage:
  scripts/summarize-game.py out/wine_runs/20261020_100736__3_TauCross.scx_
  scripts/summarize-game.py out/wine          # live copy, game may be running
"""
import json
import os
import statistics
import sys
from collections import Counter, defaultdict


def load_jsonl(path):
    rows = []
    try:
        with open(path, errors="replace") as f:
            for line in f:
                line = line.strip()
                if line.startswith("{"):
                    try:
                        rows.append(json.loads(line))
                    except ValueError:
                        pass
    except OSError:
        pass
    return rows


def main(run_dir):
    get = lambda n: os.path.join(run_dir, n)
    print("== " + run_dir)

    # --- verdict ---
    for name in ("run.txt", "WINE_LOG"):
        try:
            with open(get(name), errors="replace") as f:
                txt = f.read()
        except OSError:
            continue
        for line in txt.splitlines():
            if "KILLED:" in line and "TIMES_HIT" in line:
                print("verdict:", line.strip().split("say] ")[-1].split("log:")[0].strip())
            if "Units killed/lost" in line:
                print("bot:", line.strip())

    # --- roster ---
    try:
        ledger = json.load(open(get("BUILDINGS.json")))
    except (OSError, ValueError):
        ledger = []
    seen = {}
    for r in ledger:
        t = r.get("type", "?")
        if t not in seen:
            seen[t] = (r.get("time", "?"), r.get("tile", "?"))
    if seen:
        print("roster:", ", ".join("%s@%s" % (t, v[0]) for t, v in sorted(seen.items())))

    rows = load_jsonl(get("EVENTS.jsonl"))
    if not rows:
        print("no EVENTS.jsonl")
        return
    frames = [r.get("frame", 0) for r in rows if r.get("frame")]
    if frames:
        print("frames: %d-%d (%.1f game-min)" % (min(frames), max(frames), (max(frames) - min(frames)) / 1440.0))

    # --- per-unit table ---
    traces = defaultdict(list)
    deaths = {}
    for r in rows:
        k = r.get("key", "")
        if k == "UNIT_TRACE":
            traces[r.get("unit")].append(r)
        elif k == "UNIT_DIED":
            deaths[r.get("unit")] = r
    if traces or deaths:
        print("id   type              alive  died   min_hp  avg_dist  killer")
        for uid in sorted(set(list(traces) + list(deaths))):
            rs = sorted(traces.get(uid, []), key=lambda r: r.get("frame", 0))
            d = deaths.get(uid, {})
            hps = [r.get("hp", 0) + r.get("shields", 0) for r in rs]
            dists = [r.get("dist", -1) / 10.0 for r in rs if r.get("dist", -1) >= 0]
            typ = (rs[0].get("type", "?") if rs else d.get("type", "?"))
            print("%-4s %-17s %-6s %-6s %-7s %-9s %s" % (
                uid, typ,
                rs[0].get("frame", "?") if rs else "?",
                d.get("frame", "alive") if uid in deaths else "alive",
                min(hps) if hps else "?",
                round(sum(dists) / len(dists), 2) if dists else "?",
                d.get("killer", "-")))

    # --- gates and twins ---
    refused = Counter()
    tech = Counter()
    kite = Counter()
    fired, held, hits = 0, 0, 0
    per_unit_shots = defaultdict(list)
    tactics = Counter()
    other = Counter()
    for r in rows:
        k = r.get("key", "")
        if k == "BUILD_REFUSED":
            refused[r.get("guard", "?")] += 1
        elif k == "TECH_CHECK":
            tech[r.get("refusedBy", "?")] += 1
        elif k == "KITE":
            kite[str(r.get("result", "?"))[:20]] += 1
        elif k == "SHOT":
            if r.get("result") == "shot-fired":
                fired += 1
                per_unit_shots[r.get("unit", "?")].append(r.get("frame", 0))
            else:
                held += 1
        elif k == "HIT":
            hits += 1
        elif k == "CLUSTER":
            tactics[r.get("tactic", "?")] += 1
        elif k in ("SUPPLY_BLOCKED", "ORDER_SAVING_FOR_EXPANSION", "GAME_END"):
            other[k] += 1
    if refused:
        print("refused:", dict(refused.most_common(8)))
    if tech:
        print("tech-gates:", dict(tech.most_common(6)))
    gaps = []
    for frames in per_unit_shots.values():
        fs = sorted(frames)
        gaps.extend([b - a for a, b in zip(fs, fs[1:]) if 0 < b - a < 600])
    print("kite:", dict(kite), "| shots fired/holding: %d/%d" % (fired, held),
          ("| median per-unit gap %d" % statistics.median(gaps)) if gaps else "", "| hits:", hits)
    if tactics:
        print("tactics:", dict(tactics))
    if other:
        print("other:", dict(other))


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1].rstrip("/"))
