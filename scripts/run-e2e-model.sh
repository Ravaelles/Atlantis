#!/bin/bash
# The single entry point the model uses for every E2E iteration (owner's request,
# 2026-10-12): it rebuilds the jar, then runs the OpenBW E2E runner with
# --save-replay ALWAYS, so the owner can watch what the bot actually did.
#
# Usage:
#   bash scripts/run-e2e-model.sh [--short]           # quick opening tier
#   bash scripts/run-e2e-model.sh                     # full-length tier
#   MAP="maps/cog/(3)TauCross1.1.scx" bash scripts/run-e2e-model.sh --short
#
# Everything is passed through to run-openbw-e2e.sh, so --short/--long and env
# overrides (BUILDER_LOG=1, TIMEOUT_SECONDS=...) work exactly as before.
set -euo pipefail

cd "$(dirname "$0")/.."

MAP="${MAP:-maps/cog/(3)TauCross1.1.scx}"
TIER="${TIER:---short}"

echo "[model-e2e] 1/2 building bot jar..."
timeout 60 bash scripts/build-bot-jar.sh > /dev/null

echo "[model-e2e] 2/2 running OpenBW E2E ($MAP, $TIER, --save-replay)..."
timeout 60 bash scripts/run-openbw-e2e.sh "$MAP" Protoss Zerg "$TIER" --save-replay
