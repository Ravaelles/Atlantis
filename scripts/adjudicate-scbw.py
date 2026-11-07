#!/usr/bin/env python3
"""Adjudicate an scbw game by final supply when the engine has no winner.

A crashed game records no winner and saves no replay; for measurement
purposes a supply lead beats a crash row. Reads <game>/logs_{0,1}/
frames.csv (final supply_used) and result.json, writes
<game>/ADJUDICATION.json. Never touches result.json or any log: the
engine verdict stays the truth, this file is labelled adjudication.

Policy (owner 2026-11-07): higher final supply wins; a tie or an
unreadable log records our defeat (players[1] is the enemy in every
_*.sh launcher) rather than a crash row.

Exit 0 always: a diagnostic, never fatal to the caller.
"""
import csv
import json
import os
import sys


def final_supply(frames_path):
    try:
        with open(frames_path) as f:
            rows = list(csv.DictReader(f))
    except (OSError, csv.Error):
        return None
    for row in reversed(rows):
        try:
            return int(row["supply_used"])
        except (KeyError, ValueError, TypeError):
            continue
    return None


def main(game_dir):
    result_path = os.path.join(game_dir, "result.json")
    try:
        with open(result_path) as f:
            result = json.load(f)
    except (OSError, ValueError) as e:
        print(f"[adjudicate] no result.json in {game_dir}: {e}")
        return 0

    game = os.path.basename(game_dir.rstrip("/"))
    engine_winner = result.get("winner")

    adj = {
        "game": game,
        "adjudicated": engine_winner is None,
        "engine": {
            "is_crashed": result.get("is_crashed"),
            "winner": engine_winner,
        },
    }

    if engine_winner is not None:
        adj["method"] = "engine"
        adj["winner"] = engine_winner
        adj["reason"] = "engine decided; recorded, not adjudicated"
    else:
        bots = result.get("bots", ["player_0", "player_1"])
        supplies = [
            final_supply(os.path.join(game_dir, "logs_0", "frames.csv")),
            final_supply(os.path.join(game_dir, "logs_1", "frames.csv")),
        ]
        adj["method"] = "final-supply"
        adj["players"] = [
            {
                "bot": bots[i] if i < len(bots) else "player_%d" % i,
                "final_supply": supplies[i],
            }
            for i in (0, 1)
        ]
        if (
            supplies[0] is not None
            and supplies[1] is not None
            and supplies[0] != supplies[1]
        ):
            w = 0 if supplies[0] > supplies[1] else 1
            adj["winner"] = adj["players"][w]["bot"]
            adj["reason"] = (
                "higher final supply %s vs %s" % (supplies[w], supplies[1 - w])
            )
        else:
            adj["winner"] = adj["players"][1]["bot"]
            adj["reason"] = (
                "supply undecided (%s vs %s): recorded as defeat per policy"
                % (supplies[0], supplies[1])
            )

    out = os.path.join(game_dir, "ADJUDICATION.json")
    try:
        with open(out, "w") as f:
            json.dump(adj, f, indent=2)
    except OSError as e:
        print("[adjudicate] cannot write %s: %s" % (out, e))
        return 0
    print("[adjudicate] %s: winner=%s (%s)" % (game, adj["winner"], adj["reason"]))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "."))
