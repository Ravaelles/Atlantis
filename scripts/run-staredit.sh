#!/usr/bin/env bash
# Run the StarCraft scenario editor (StarEdit.exe) under Wine.
#
# Usage:
#   bash scripts/run-staredit.sh [map-file]       # opens the editor, optionally with a map
#
# The editor needs the game directory (MPQs, EditLocal.dll): it runs from
# the same Wine game root the bot games use ($WINEPREFIX/drive_c/sc, which
# links /sc-ai/starcraft). Unlike bot games this is interactive - no
# HIDDEN/Xvfb mode: it needs your real X display (:0 by default).
# English-only output and comments (CONVENTIONS §1).

set -u

ATLANTIS_DIR="${ATLANTIS_DIR:-/sc-ai/Atlantis}"
WINE_ROOT="${WINEPREFIX:-$HOME/.wine}"
GAME_ROOT="$WINE_ROOT/drive_c/sc"
MAP="${1:-}"

if [ ! -f "$GAME_ROOT/StarEdit.exe" ] && [ ! -f /sc-ai/starcraft/StarEdit.exe ]; then
  echo "[staredit] FATAL: StarEdit.exe found neither in $GAME_ROOT nor in /sc-ai/starcraft" >&2
  exit 1
fi

cd "$GAME_ROOT" 2>/dev/null || cd /sc-ai/starcraft

export DISPLAY="${DISPLAY:-:0}"

if [ -n "$MAP" ]; then
  if [ ! -f "$MAP" ]; then
    echo "[staredit] FATAL: map not found: $MAP" >&2
    exit 1
  fi
  echo "[staredit] opening $MAP (display $DISPLAY)"
  exec env WINEDEBUG=-all wine StarEdit.exe "$MAP"
else
  echo "[staredit] opening empty editor (display $DISPLAY)"
  exec env WINEDEBUG=-all wine StarEdit.exe
fi
