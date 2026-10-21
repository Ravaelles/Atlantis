#!/usr/bin/env python3
"""Attribute BOARD_FED rows to legacy pump activity (NEXT.md step 1).

Usage:
    python3 scripts/attribute-board-fed.py out/wine_runs/<run>/EVENTS.jsonl [--army-window 1500] [--supply-window 600]
    python3 scripts/attribute-board-fed.py by-author out/wine_runs/<run>/EVENTS.jsonl

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
import re
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
    if args and args[0] == "by-author" and len(args) > 1:
        return by_author(args[1])
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


def by_author(path):
    # NEXT #144 evidence: who authored the demand, BO rows ("At N ...")
    # vs dynamic re-requests (trailing "*", see ProductionOrder.toString).
    # Same twin, no new rows - just the author split per building type.
    import collections

    rows = load(path)
    split = collections.defaultdict(collections.Counter)
    for r in rows:
        if r.get("key") != "CONSTRUCTION_REQUESTED":
            continue
        # Trailing "*" directly after the type name marks dynamic
        # re-requests (ProductionOrder.toString); plain "At N ..." is BO.
        order = str(r.get("order", ""))
        m = re.match(r"At \d+ (\S+?)(\*)?\s", order)
        if m:
            author = "dynamic" if m.group(2) else "BO"
        else:
            author = "other:" + order[:20]
        split[str(r.get("type", "?"))][author] += 1
    for building in sorted(split):
        total = sum(split[building].values())
        dyn = split[building].get("dynamic", 0)
        print("%s: %d demand rows, %d%% dynamic (%s)" % (
            building, total, round(100.0 * dyn / total),
            dict(split[building])))
    return 0


if __name__ == "__main__":
    sys.exit(main())
