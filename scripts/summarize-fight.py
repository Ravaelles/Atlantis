#!/usr/bin/env python3
"""Fight digest from a twin stream (EVENTS.jsonl).

Reads the structured twins of one game and prints what happened plus
which domain invariants broke. The invariants come from understanding
the problem, not from thresholds tuned to pass:
  combat: a faster Dragoon kiting a Zealot takes at most 2 hits;
          a 30-frame cooldown means a shot roughly every 30 frames.
Usage:
  scripts/summarize-fight.py out/wine/EVENTS.jsonl
  scripts/summarize-fight.py /tmp/opencode/wine-*-EVENTS.jsonl
"""
import json
import sys
from collections import Counter

HIT_BUDGET = 2          # a kiting Dragoon is faster than a Zealot: 2 hits max
SHOT_COOLDOWN = 30      # Dragoon Phase Disruptor cooldown, engine-measured


def load(path):
    rows = []
    with open(path) as f:
        for line in f:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def gaps(frames):
    return [b - a for a, b in zip(frames, frames[1:])]


def main(path):
    rows = load(path)
    keys = Counter(r.get("key") for r in rows)
    print("rows: %d %s" % (len(rows), dict(keys.most_common())))

    end = [r for r in rows if r.get("key") == "GAME_END"]
    if end:
        e = end[-1]
        print("game end: frame=%s won=%s supply=%s/%s bank=%s/%s" % (
            e.get("frame"), e.get("won"), e.get("supplyUsed"),
            e.get("supplyTotal"), e.get("minerals"), e.get("gas")))

    shots = sorted(r["frame"] for r in rows
                   if r.get("key") == "SHOT" and r.get("result") == "shot-fired")
    holds = sum(1 for r in rows
                if r.get("key") == "SHOT" and r.get("result") == "holding-shot")
    fight_frames = [r["frame"] for r in rows if r.get("key") in ("HIT", "KITE")
                    or (r.get("key") == "SHOT")]
    kill_frame = max(fight_frames) if fight_frames else 0
    if shots:
        g = gaps(shots)
        med = sorted(g)[len(g) // 2] if g else 0
        dists = [r["dist"] / 10 for r in rows
                 if r.get("key") == "SHOT" and r.get("result") == "shot-fired"]
        print("shots: %d fired, %d holds; median gap %s frames (weapon: %d); "
              "fire dist min/avg %.1f/%.1f tiles; kill time ~%d frames" % (
                  len(shots), holds, med, SHOT_COOLDOWN,
                  min(dists), sum(dists) / len(dists), shots[-1] - shots[0]))

    hits = [r for r in rows if r.get("key") == "HIT"]
    if hits:
        lost = sum(r.get("lost", 0) for r in hits)
        print("hits taken: %d, damage %d (hp+shields 180)" % (len(hits), lost))
        for r in hits:
            print("  f%-5d dist=%.1f cd=%-3d moving=%s lost=%s" % (
                r["frame"], r["dist"] / 10, r.get("cooldown"),
                r.get("moving"), r.get("lost")))

    kite = [r for r in rows if r.get("key") == "KITE"]
    if kite:
        res = Counter(r.get("result") for r in kite)
        print("kite: %s" % dict(res))

    # --- invariants: flags, not verdicts ---
    flags = []
    if len(hits) > HIT_BUDGET:
        flags.append("HITS_TAKEN %d > %d (kiting unit must not stand in range)"
                     % (len(hits), HIT_BUDGET))
    if len(shots) >= 3:
        med = sorted(gaps(shots))[len(gaps(shots)) // 2]
        if med > 2 * SHOT_COOLDOWN:
            flags.append("SHOT_CADENCE median gap %d > 2x cooldown (half the "
                         "cycles produce no shot - fleeing instead of cycling)"
                         % med)
    stuck = sum(1 for r in kite if r.get("result") == "stuck-no-step")
    stepped = sum(1 for r in kite if r.get("result") == "stepped-aside")
    if stepped and stuck / stepped > 0.5:
        flags.append("CORNERED %.0f%% stuck (%d/%d) - escapes walk into walls"
                     % (100 * stuck / stepped, stuck, stepped))
    if flags:
        print("INVARIANTS BROKEN:")
        for f in flags:
            print("  ! " + f)
    else:
        print("invariants hold")


if __name__ == "__main__":
    main(sys.argv[1])
