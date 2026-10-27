#!/usr/bin/env bash
# CherryVis viewer: ensure it runs now and comes back after a reboot.
#
# Usage: bash scripts/cherryvis-start.sh [--pattern GLOB] [--rebuild]
#
# Idempotent - safe to run any time. It fixes the states that cost real
# debugging before: a stale container squatting the "cvis" name, a
# container started with a debug command instead of the server, and a
# published-but-unmapped port. Boot persistence comes from the docker
# daemon (enabled below) plus "--restart unless-stopped" on the container:
# no cron, no systemd unit, nothing else to install.
set -euo pipefail

NAME="cvis"
IMAGE="cvis"
PORT="8770"
REPLAYS_DIR="/sc-ai/REPLAYS"
PROJECT_DIR="/sc-ai/CherryVis/cherryvis-docker"
PATTERN="/my-replays/*.rep"
REBUILD="0"

while [ $# -gt 0 ]; do
    case "$1" in
        --pattern) PATTERN="$2"; shift 2;;
        --rebuild) REBUILD="1"; shift;;
        -h|--help)
            echo "Usage: $0 [--pattern GLOB] [--rebuild]";
            echo "Ensures the CherryVis viewer runs now and after reboot.";
            exit 0;;
        *) echo "Unknown flag: $1 (see --help)" >&2; exit 2;;
    esac
done

# 1. Docker daemon must start on boot, or no restart policy can fire.
if command -v systemctl >/dev/null 2>&1; then
    if [ "$(systemctl is-enabled docker 2>/dev/null || echo disabled)" != "enabled" ]; then
        echo "[cherryvis] enabling docker daemon autostart (may ask for sudo)..."
        if ! sudo systemctl enable docker; then
            echo "[cherryvis] WARNING: could not enable docker autostart." \
                "After a reboot run once: sudo systemctl enable --now docker"
        fi
    fi
fi

# 2. Image: build only when missing (or on explicit --rebuild - it takes minutes).
if [ "$REBUILD" = "1" ] || ! docker image inspect "$IMAGE" >/dev/null 2>&1; then
    echo "[cherryvis] building image '$IMAGE' from $PROJECT_DIR (one time, takes minutes)..."
    (cd "$PROJECT_DIR" && docker build -t "$IMAGE" .)
fi

# 3. Container: running+correct wins, everything else gets recreated.
if docker ps --format '{{.Names}}' | grep -qx "$NAME"; then
    echo "[cherryvis] container '$NAME' already running - ensuring restart policy."
    docker update --restart unless-stopped "$NAME" >/dev/null
else
    if docker ps -a --format '{{.Names}}' | grep -qx "$NAME"; then
        echo "[cherryvis] removing stale container '$NAME'."
        docker rm -f "$NAME" >/dev/null
    fi
    echo "[cherryvis] starting '$NAME' (pattern: $PATTERN)..."
    # shellcheck disable=SC2086
    docker run -d --name "$NAME" --restart unless-stopped \
        -p ${PORT}:${PORT} -v "${REPLAYS_DIR}:/my-replays" \
        "$IMAGE" ./server.py --host 0.0.0.0:${PORT} --replays_pattern "$PATTERN" >/dev/null
fi

# 4. Verify the server answers instead of claiming it (Django needs a
# few seconds to boot - poll instead of sleeping a fixed window).
echo -n "[cherryvis] waiting for the server"
for _ in $(seq 1 30); do
    # -f: a 404 is a failure, not an answer (a transient 404 once
    # passed this gate and printed a false-green check).
    if curl -sf -o /dev/null "localhost:${PORT}/static/replays/cdb/cvis_dbg_matching.js"; then
        echo " - up."
        break
    fi
    echo -n "."
    sleep 1
done
# Final check, with retries: a single transient 404 must not report
# a healthy server as down (measured twice on a live server).
CODE="000"
for _ in 1 2 3; do
    CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        "localhost:${PORT}/static/replays/cdb/cvis_dbg_matching.js" || true)
    [ "$CODE" = "200" ] && break
    sleep 2
done
echo "static-check http=${CODE}"
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' | head -3
echo "Open: http://localhost:${PORT}/?replays.pattern=%2Fmy-replays%2F*.rep"
