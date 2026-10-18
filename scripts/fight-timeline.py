#!/usr/bin/env python3
"""Per-cycle decision timeline from a twin stream (EVENTS.jsonl).

Splits the duel into cooldown cycles (between consecutive shot-fired
rows) and prints one line per cycle: what the unit decided and what
it cost. Wrong DECISIONS read as a repeated pattern here (e.g. six
"step, step, hit, hit" cycles in a row); wrong IMPLEMENTATION reads
as a single broken row (a shot at dist 0, a step into a wall).

Usage:
  scripts/fight-timeline.py out/wine/EVENTS.jsonl
"""
import json
import sys


def load(path):
    rows = []
    with open(path) as f:
        for line in f:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return sorted(rows, key=lambda r: r["frame"])


def main(path):
    rows = load(path)
    shots = [r for r in rows if r.get("key") == "SHOT"
             and r.get("result") == "shot-fired"]
    if not shots:
        print("no shots - the unit never fired (see HIT/KITE for why)")
        return
    # What the unit did right before each hit: the latest KITE/SHOT row
    # strictly before it (list order breaks same-frame ties).
    decisions = [r for r in rows if r.get("key") in ("KITE", "SHOT")]
    def prev_of(hit):
        cands = [r for r in decisions if r["frame"] < hit["frame"]]
        if not cands:
            return None
        return cands[-1]
    bounds = [s["frame"] for s in shots] + [rows[-1]["frame"] + 1]
    for i, s in enumerate(shots):
        lo, hi = bounds[i], bounds[i + 1]
        cyc = [r for r in rows if lo <= r["frame"] < hi
               and r.get("key") in ("KITE", "HIT", "SHOT")]
        steps = [r for r in cyc if r.get("key") == "KITE"
                 and r.get("result") == "stepped-aside"]
        stuck = [r for r in cyc if r.get("key") == "KITE"
                 and r.get("result") == "stuck-no-step"]
        hits = [r for r in cyc if r.get("key") == "HIT"]
        holds = [r for r in cyc if r.get("key") == "SHOT"
                 and r.get("result") == "holding-shot"]
        step_d = ",".join(str(r["dist"] / 10) for r in steps[:6])
        hit_ctx = []
        for h in hits:
            p = prev_of(h)
            if p is None:
                hit_ctx.append("nothing before")
            else:
                hit_ctx.append("%s f%d dist=%.1f cd=%s" % (
                    p.get("result"), p["frame"], p["dist"] / 10,
                    p.get("cooldown")))
        hit_d = "; ".join("%s@%s (was: %s)" % (r["dist"] / 10, r["cooldown"], c)
                          for r, c in zip(hits, hit_ctx))
        flag = ""
        if hits and not steps and not stuck:
            flag = "  <-- STOOD through the whole cycle taking hits"
        if hits and steps and all(r["dist"] / 10 < 1.5 for r in hits):
            flag = "  <-- steps came one swing too late (hits at glue)"
        if not hits and steps:
            flag = "  ok: stepped clean, no damage"
        if not hits and not steps:
            flag = "  ok: held fire position, untouched"
        print("cycle f%-4d shot@%.1f | steps=%d [%s] stuck=%d holds=%d "
              "hits=%d [%s]%s" % (
                  s["frame"], s["dist"] / 10, len(steps), step_d,
                  len(stuck), len(holds), len(hits), hit_d, flag))


if __name__ == "__main__":
    main(sys.argv[1])
