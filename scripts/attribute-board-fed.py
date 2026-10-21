#!/usr/bin/env python3
"""Attribute BOARD_FED rows to legacy pump activity (NEXT.md step 1).

Usage:
    python3 scripts/attribute-board-fed.py out/wine_runs/<run>/EVENTS.jsonl [--army-window 1500] [--supply-window 600]

For every BOARD_FED row the script counts legacy pump rows in a window
around it and prints rescue vs routine-shaped, using the file's own
rules: a TRAIN_ATTEMPT counts as present even when blocked (a blocked
pump is still rescue, not routine). Pumps silent => the feeder covered
a hole routinely => the mapped legacy producer is nominated for
per-gate deletion (proof game still required after the cut).

Pump proxies (documented approximations, same as the hand verdicts):
  sustained-*  DRAGOON_TRAIN rows + TRAIN_ATTEMPT rows in +/-army_window
               (attempts count even when blocked - a blocked pump is
               still rescue, not routine)
  supply-pylon Pylon rows in +/-supply_window: CONSTRUCTION_REQUESTED +
               BUILDING_STARTED (delivered work) + ORDER_NOT_READY
               (demand alive but refused - the building analogue of a
               blocked TRAIN_ATTEMPT)
  gateway      same three row kinds for Gateway
  assimilator  same three row kinds for Assimilator
  core         same three row kinds for Cybernetics
"""
import json
import sys

BUILDING_FEDS = {
    "supply-pylon": ("Pylon", "BO supply rows + dynamic supply pull-forward"),
    "gateway": ("Gateway", "BO gateway rows + dynamic gateway pump"),
    "assimilator": ("Assimilator", "gas demand (V2 gas order + legacy gas pump)"),
    "core": ("Cybernetics", "core demand (BO + dynamic, single-instance gate)"),
}


def load(path):
    rows = []
    with open(path, errors="replace") as f:
        for line in f:
            line = line.strip()
            if not line.startswith("{"):
                continue
            try:
                rows.append(json.loads(line))
            except Exception:
                continue
    return rows


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    army_window = 1500
    supply_window = 600
    for a in sys.argv[1:]:
        if a.startswith("--army-window"):
            army_window = int(a.split("=", 1)[1])
        if a.startswith("--supply-window"):
            supply_window = int(a.split("=", 1)[1])
    if not args:
        print(__doc__)
        return 2
    rows = load(args[0])
    fed = [r for r in rows if r.get("key") == "BOARD_FED"]
    if not fed:
        print("no BOARD_FED rows")
        return 0
    nominations = set()
    for r in fed:
        frame = r.get("frame", 0)
        tag = str(r.get("fed", "?"))
        if tag.startswith("sustained-"):
            unit = tag.split("-", 1)[1]
            pump = sum(
                1 for q in rows
                if abs(q.get("frame", -10 ** 9) - frame) <= army_window
                and q.get("key") in ("DRAGOON_TRAIN", "TRAIN_ATTEMPT")
            )
            producer = "dynamic %s pump" % unit
        elif tag in BUILDING_FEDS:
            needle, producer = BUILDING_FEDS[tag]
            pump = sum(
                1 for q in rows
                if abs(q.get("frame", -10 ** 9) - frame) <= supply_window
                and q.get("key")
                in ("CONSTRUCTION_REQUESTED", "BUILDING_STARTED", "ORDER_NOT_READY")
                and needle.lower() in str(q.get("type", "")).lower()
            )
        else:
            pump = -1
            producer = "unknown producer (manual read)"
        if pump < 0:
            verdict = "MANUAL (no proxy for %s)" % tag
        elif pump > 0:
            verdict = "rescue (pumps present: %d)" % pump
        else:
            verdict = "ROUTINE-SHAPED (pumps silent) -> nominates %s" % producer
            nominations.add(producer)
        print("frame=%s fed=%s verdict=%s" % (frame, tag, verdict))
    print("---")
    print("boardfed=%d routine-shaped=%d" % (len(fed), len(nominations)))
    for producer in sorted(nominations):
        print("NOMINATE: %s (proof game required after the cut)" % producer)
    return 0


if __name__ == "__main__":
    sys.exit(main())
