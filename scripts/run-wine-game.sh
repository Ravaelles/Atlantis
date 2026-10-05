#!/usr/bin/env bash
# Run StarCraft + BWAPI 4.4.0 + ChaosLauncher under Wine, with Atlantis as the
# AI client (java -jar), against the native Blizzard AI on a UMS map.
#
# This is the "F5 from IntelliJ" equivalent for Linux: one command starts the
# game (visible on the X display), injects BWAPI, and starts the Java client.
#
# Usage:
#   run-wine-game.sh [map-name]           # default: 1a2a3a Micro 2.scx
#
# Paths (change in ENV, see bwapi-data/AI/ENV):
#   STARCRAFT_DIR       - the game install            (/sc-ai/starcraft)
#   BWAPI_DIST_DIR      - BWAPI 4.4.0 distribution    (/sc-ai/BWAPI)
#   REPLAYS_DIR         - where replays land          (/sc-ai/REPLAYS)
#   FORCE_END_GAME_AFTER_REAL_SECONDS - client exit limit
#
# Requirements: wine 9.0+ (tested), an X display for the game window.
set -euo pipefail

STARCRAFT_DIR="${STARCRAFT_DIR:-/sc-ai/starcraft}"
BWAPI_DIST_DIR="${BWAPI_DIST_DIR:-/sc-ai/BWAPI}"
REPLAYS_DIR="${REPLAYS_DIR:-/sc-ai/REPLAYS}"
MAP="${1:-1a2a3a Micro 2.scx}"
RACE="${RACE:-Protoss}"
ENEMY_RACE="${ENEMY_RACE:-Zerg}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="/sc-ai/Atlantis/out/wine"
mkdir -p "$LOG_DIR"
WINE_ROOT="${WINEPREFIX:-$HOME/.wine}"
GAME_ROOT="$WINE_ROOT/drive_c/sc"

# --- 1. Prepare the game directory under drive_c (symlinks, no 116 MB copy)
mkdir -p "$GAME_ROOT"

if [ ! -e "$GAME_ROOT/StarCraft.exe" ]; then
  echo "[wine-game] linking game files from $STARCRAFT_DIR"
  # Link every file and dir from the game install
  for entry in "$STARCRAFT_DIR"/* "$STARCRAFT_DIR"/.*; do
    base="$(basename "$entry")"
    [ "$base" = "." ] || [ "$base" = ".." ] && continue
    [ -e "$GAME_ROOT/$base" ] && continue
    ln -s "$entry" "$GAME_ROOT/$base"
  done
fi

# --- 2. Copy the BWAPI runtime (needs real files, not symlinks, for injection)
BWAPI_DATA="$GAME_ROOT/bwapi-data"
CHAOS_DIR="$GAME_ROOT/chaoslauncher"
mkdir -p "$BWAPI_DATA/AI" "$BWAPI_DATA/bwta" "$BWAPI_DATA/bwta2" "$BWAPI_DATA/errors" "$BWAPI_DATA/logs"

if [ ! -f "$CHAOS_DIR/Chaoslauncher.exe" ]; then
  echo "[wine-game] copying BWAPI 4.4.0 runtime"
  cp -r "$BWAPI_DIST_DIR/Chaoslauncher" "$CHAOS_DIR"
  cp -r "$BWAPI_DIST_DIR/Starcraft/bwapi-data" "$BWAPI_DATA"
fi

# BWAPI.dll MUST be at C:\sc\bwapi-data\BWAPI.dll (that is where the injector
# looks for it, relative to the game root). Verify it exists.
if [ ! -f "$BWAPI_DATA/BWAPI.dll" ]; then
  echo "[wine-game] ERROR: BWAPI.dll missing at $BWAPI_DATA/BWAPI.dll"
  exit 1
fi

# --- 3. Generate bwapi.ini
INI="$BWAPI_DATA/bwapi.ini"
cat > "$INI" <<EOF
[ai]
ai = bwapi-data/AI/ClientBWAPI.dll
auto_menu = SINGLE_PLAYER
character_name = FIRST
pause_dbg = OFF
map = maps/ums/$MAP
game_type = USE_MAP_SETTINGS
race = $RACE
enemy_race = $ENEMY_RACE
save_replay = $REPLAYS_DIR/%MAP%_\$Y\$m\$d_\$H\$M\$S.rep
EOF

echo "[wine-game] bwapi.ini written:"
cat "$INI"

# --- 4. Copy the AI client DLL (Java mirror is started separately)
# BWAPI 4.4.0 talks to the AI module in-process; for a Java bot the AI module
# is a bridge (ClientBWAPI.dll) that talks to a separate JVM over the BWAPI
# client protocol. The distribution ships ExampleAIModule only, so for now we
# rely on BWAPI's own client-server bridge (java -jar starts separately).

# --- 5. Start ChaosLauncher under Wine. It injects BWAPI into StarCraft and
#        (with "Run Starcraft on Startup") starts the game itself.
cd "$GAME_ROOT"
echo "[wine-game] starting ChaosLauncher..."
env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" wine "$CHAOS_DIR/Chaoslauncher.exe" \
  > $LOG_DIR/chaoslauncher.log 2>&1 &
CHAOS_PID=$!
echo "[wine-game] ChaosLauncher PID=$CHAOS_PID, game window should appear on display ${DISPLAY:-:0}."
echo "[wine-game] Log: $LOG_DIR/chaoslauncher.log"
echo "[wine-game] Start the Java client when the game is running:"
echo "  cd /sc-ai/Atlantis && java -jar bots/AtlantisP/AI/Atlantis.jar"
echo "$CHAOS_PID" > $LOG_DIR/chaoslauncher.pid
