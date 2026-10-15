#!/usr/bin/env python3
"""Query the bot's structured event log (EVENTS.jsonl) without grepping.

One row per diagnostic event: {"frame":F,"time":"m:ss","key":"WORKER_GATE",...}.
Keys are LogKey families; parameters carry the variance.

Usage:
  scripts/query-events.py [--file PATH] [--key KEY ...] [--since F] [--until F]
                          [--count] [--cols a,b,c]

Defaults to the newest E2E_LOGS/*/EVENTS.jsonl. --count prints rows per key.
"""
import argparse
import glob
import json
import os
import sys


def newest(path):
    if path:
        return path
    hits = sorted(glob.glob('/sc-ai/BOTS/AtlantisOpenBW/E2E_LOGS/*/EVENTS.jsonl'))
    if not hits:
        sys.exit('no EVENTS.jsonl under E2E_LOGS (run a game first)')
    return hits[-1]


def frame_of(value):
    try:
        return int(value)
    except (TypeError, ValueError):
        return None


def main():
    arg = argparse.ArgumentParser()
    arg.add_argument('--file', default=None)
    arg.add_argument('--key', action='append', default=[])
    arg.add_argument('--since', default=None, type=int)
    arg.add_argument('--until', default=None, type=int)
    arg.add_argument('--cols', default=None)
    arg.add_argument('--count', action='store_true')
    ns = arg.parse_args()

    path = newest(ns.file)
    rows = []
    with open(path) as handle:
        for line in handle:
            line = line.strip()
            if not line:
                continue
            try:
                rows.append(json.loads(line))
            except ValueError:
                continue

    if ns.key:
        wanted = set(ns.key)
        rows = [r for r in rows if r.get('key') in wanted]
    if ns.since is not None:
        rows = [r for r in rows if frame_of(r.get('frame')) is not None
                and frame_of(r.get('frame')) >= ns.since]
    if ns.until is not None:
        rows = [r for r in rows if frame_of(r.get('frame')) is not None
                and frame_of(r.get('frame')) <= ns.until]

    if ns.count:
        totals = {}
        for row in rows:
            key = str(row.get('key'))
            totals[key] = totals.get(key, 0) + 1
        for key in sorted(totals):
            print('%6d  %s' % (totals[key], key))
        return

    if ns.cols:
        cols = [c.strip() for c in ns.cols.split(',')]
    elif rows:
        cols = ['frame', 'time', 'key']
        seen = set(cols)
        for row in rows:
            for key in row:
                if key not in seen:
                    seen.add(key)
                    cols.append(key)
    else:
        cols = []

    print('# %s (%d rows)' % (os.path.basename(os.path.dirname(path))
          if path.endswith('.jsonl') else path, len(rows)))
    print('\t'.join(cols))
    for row in rows:
        print('\t'.join(str(row.get(c, '')) for c in cols))


if __name__ == '__main__':
    main()
