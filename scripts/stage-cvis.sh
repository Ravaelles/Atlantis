#!/bin/bash
# Stage a CherryVis trace next to its replay - the layout the viewer opens.
#
# The bot writes its trace as a directory literally named
# `cherryvis_dir.rep.cvis/` (ACherryVisConfig) in whatever directory it
# ran in (`bwapi-data/write/`, `write_0/`, the run dir...). The CherryVis
# server does NOT pair that with anything: it lists `*.rep` files and pairs
# each `<name>.rep` with a SIBLING `<name>.rep.cvis/` directory. So a fresh
# trace is invisible to the viewer until it is renamed after a replay.
#
#   bash scripts/stage-cvis.sh <dir> [<dir> ...]
#
# Per directory, newest first: every trace is copied to the newest `.rep`
# that has no `.rep.cvis` yet, one trace per replay ("newest wins" is the
# rule run-openbw-e2e.sh already uses - the engine rewrites the configured
# replay name, so pairing must follow the file on disk). A directory with
# `.rep` files but no usable trace is reported and exits 1: nothing there
# is openable. Extra, older `.rep` files are left alone - their traces are
# simply not in this tree.
#
# Source traces are left in place (a run dir is evidence; copying keeps
# it) and an already-paired `.rep` is skipped, so the script is
# idempotent - re-running stages only what is new. Pure file work: no
# game, no suite, safe to run any time (the one-quiet-box rule is about
# games and suites, not about copying files).
#
# To actually watch: the `cvis` container must be running and the replay
# pattern must be recursive - see _AI/CHERRYVIS-DOCKER.md and
# DOCS/CherryVis.md (`/my-replays/**`).

set -euo pipefail

TRACE_NAME="cherryvis_dir.rep.cvis"

if [ "$#" -eq 0 ]; then
    echo "usage: $0 <dir> [<dir> ...]   (searched recursively for $TRACE_NAME and *.rep)" >&2
    exit 2
fi

# Identity of a trace: size + mtime of its payload. `cp -a` preserves both,
# so a staged copy carries the source's identity - that is what makes a
# re-run see "this trace is already paired" instead of pairing the same
# trace with yet another, older replay.
trace_id() {
    local dir="$1" f
    for f in "$dir/trace.json" "$dir/trace.json.zst"; do
        if [ -f "$f" ]; then
            stat -c '%s %Y' "$f"
            return 0
        fi
    done
    return 1
}

staged=0
untraceable=0

for root in "$@"; do
    if [ ! -d "$root" ]; then
        echo "[stage-cvis] not a directory: $root" >&2
        exit 2
    fi

    # Traces already paired somewhere under this root (the source directory
    # itself is excluded - it ends in .rep.cvis too, but it is the origin).
    declare -A paired=()
    while IFS= read -r cvis; do
        [ -n "$cvis" ] || continue
        if id="$(trace_id "$cvis")"; then paired["$id"]=1; fi
    done < <(find "$root" -type d -name '*.rep.cvis' ! -name "$TRACE_NAME" 2>/dev/null)

    mapfile -t traces < <(find "$root" -type d -name "$TRACE_NAME" -printf '%T@ %p\n' 2>/dev/null | sort -rn | cut -d' ' -f2-)

    usable=()
    for trace in ${traces[@]+"${traces[@]}"}; do
        if id="$(trace_id "$trace")"; then
            if [ -n "${paired[$id]:-}" ]; then
                echo "[stage-cvis] already staged: $trace"
            else
                usable+=("$trace")
            fi
        else
            echo "[stage-cvis] skipping empty trace: $trace (no trace.json)" >&2
        fi
    done

    mapfile -t reps < <(find "$root" -type f -name "*.rep" -printf '%T@ %p\n' 2>/dev/null | sort -rn | cut -d' ' -f2-)

    unpaired=()
    for rep in ${reps[@]+"${reps[@]}"}; do
        [ -e "$rep.cvis" ] || unpaired+=("$rep")   # already staged replays are history, not misses
    done

    if [ "${#unpaired[@]}" -gt 0 ] && [ "${#usable[@]}" -eq 0 ]; then
        # Older replays whose trace was never in this tree are history, not a
        # failure - only a root that produced nothing usable is reported.
        if [ "${#traces[@]}" -eq 0 ]; then
            echo "[stage-cvis] no trace under $root (was GENERATE_CHERRYVIS_REPLAY=true seen by the bot?)" >&2
            untraceable=$((untraceable + 1))
        elif [ "${#paired[@]}" -eq 0 ]; then
            echo "[stage-cvis] no usable trace under $root (trace dirs without trace.json)" >&2
            untraceable=$((untraceable + 1))
        fi
        continue
    fi

    for rep in ${unpaired[@]+"${unpaired[@]}"}; do
        [ "${#usable[@]}" -gt 0 ] || break      # older replays keep their own (absent) history
        cp -a "${usable[0]}" "$rep.cvis"
        echo "[stage-cvis] staged: $rep.cvis  (from ${usable[0]})"
        usable=("${usable[@]:1}")
        staged=$((staged + 1))
    done
done

echo "[stage-cvis] staged=$staged directories_without_trace=$untraceable"
if [ "$staged" -eq 0 ] && [ "$untraceable" -gt 0 ]; then
    exit 1
fi

if [ "$staged" -gt 0 ]; then
    echo "[stage-cvis] watch it in the \`cvis\` container (recursive pattern:" \
        "'/my-replays/**/*.rep' - see _AI/CHERRYVIS-DOCKER.md)"
fi
