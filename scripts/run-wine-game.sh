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

# StarCraft's own window inside the Wine desktop (see [window] in bwapi.ini).
# windowed = OFF makes the game fill the whole Wine desktop - the desktop
# itself is the window the host sees, so the game looks fullscreen while Wine
# stays in a window on the host desktop.
WINDOWED="${WINDOWED:-OFF}"
WINE_WINDOW_WIDTH="${WINE_WINDOW_WIDTH:-1600}"
WINE_WINDOW_HEIGHT="${WINE_WINDOW_HEIGHT:-1000}"
# Empty = center on the current X screen. Set both to place it explicitly.
WINE_WINDOW_X="${WINE_WINDOW_X:-}"
WINE_WINDOW_Y="${WINE_WINDOW_Y:-}"

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
#
# GAME TYPE follows the map folder, and this is not cosmetic (measured
# 2026-10-07, owner report: "the game stops at the map selection screen"):
#
#   sscai/...  -> MELEE               a real melee map; USE_MAP_SETTINGS leaves
#                                     StarCraft waiting on the player at the map
#                                     screen, so the bot never gets a game
#   ums/...    -> USE_MAP_SETTINGS    a UMS scenario carries its own rules;
#                                     MELEE would ignore them
#
# MELEE is also what makes the game start by itself: with USE_MAP_SETTINGS on a
# non-UMS map StarCraft treats the map's own settings as incomplete and waits.
case "$MAP" in
  sscai/*|*/sscai/*) GAME_TYPE="MELEE" ;;
  *)                 GAME_TYPE="USE_MAP_SETTINGS" ;;
esac
MAP_PATH="maps/$MAP"

echo "[wine-game] map=$MAP_PATH game_type=$GAME_TYPE"

# Section layout matters (measured 2026-10-05): auto_menu, map, race,
# enemy_race, game_type and save_replay live under [auto_menu], NOT under [ai].
# With everything crammed into [ai] BWAPI ignored the auto-start entirely and
# dropped the player into the StarCraft menu - the game never started and the
# Java client printed "Game table mapping not found" forever.
#
# [ai] ai = BWAPI.dll on purpose: that DLL is the shared-memory SERVER
# (its exports contain "Local\bwapi_shared_memory_*"), which is exactly what a
# Java bot attaches to. It is not an in-process AI module, and it is not
# ClientBWAPI.dll.
INI="$BWAPI_DATA/bwapi.ini"
cat > "$INI" <<EOF
[ai]
; BWAPI.dll is the shared-memory server a Java bot attaches to (its exports
; contain "Local\\bwapi_shared_memory_*"). Not ClientBWAPI.dll, not an
; in-process AI module.
ai = bwapi-data/BWAPI.dll

[auto_menu]
; SINGLE_PLAYER is what actually starts the game; OFF drops into the menu.
auto_menu = SINGLE_PLAYER
character_name = FIRST
pause_dbg = OFF
map = $MAP_PATH
game_type = $GAME_TYPE
race = $RACE
enemy_count = 1
enemy_race = $ENEMY_RACE
save_replay = $REPLAYS_DIR/%MAP%_\$Y\$m\$d_\$H\$M\$S.rep

[config]
; Wine-specific knob, documented in the 4.4.0 template: shared_memory = ON
; is the BWAPI server itself. Left ON (that is what a Java client needs);
; flip to OFF only if the server misbehaves under Wine.
shared_memory = ON

[window]
; StarCraft's own window inside the Wine desktop. This is the size the game
; renders at - separate from the wine explorer desktop size.
windowed = $WINDOWED
left = 0
top = 0
width = $WINE_WINDOW_WIDTH
height = $WINE_WINDOW_HEIGHT
EOF

echo "[wine-game] bwapi.ini written:"
cat "$INI"

# --- 4. Wine virtual desktop via the registry (NOT wine explorer /desktop=).
#
# Measured 2026-10-05: wine explorer /desktop=scgame,WxH creates a window that
# Mutter maximizes to the full screen, and neither wmctrl nor xdotool can
# resize it afterwards. The registry desktop creates an ordinary 1600x1000
# window that stays put. Wine reads it at wineserver start, so kill the server
# first.
wineserver -k 2>/dev/null || true
sleep 1
wine reg add 'HKCU\Software\Wine\Explorer' /v Desktop /d Default /f >/dev/null 2>&1
wine reg add 'HKCU\Software\Wine\Explorer\Desktops' /v Default \
  /d "${WINE_WINDOW_WIDTH}x${WINE_WINDOW_HEIGHT}" /f >/dev/null 2>&1

# W-MODE owns the game window on Wine (measured 2026-10-06, both states tested
# by hand): enabled it gives a visible window and, with DblSizeMode=1 in
# wmode.ini, a large one; disabled it gives StarCraft's native 640x480 inside
# the desktop - unusably small. So the plugin is ENABLED and the virtual
# desktop is turned off instead - the two are alternatives for the same job.
# The BWAPI injector stays on - without it there is no bot at all.
wine reg delete 'HKCU\Software\Wine\Explorer' /v Desktop /f >/dev/null 2>&1
wine reg add 'HKCU\Software\Chaoslauncher\PluginsEnabled' \
  /v 'W-MODE 1.02' /d 1 /f >/dev/null 2>&1

# W-MODE reads its geometry from C:\sc\wmode.ini - write it so the game comes
# up doubled and where WINE_WINDOW_X/Y say, not with leftovers from a manual run.
cat > "$GAME_ROOT/wmode.ini" <<EOF2
[W-MODE]
WindowClientX=${WINE_WINDOW_X:-0}
WindowClientY=${WINE_WINDOW_Y:-0}
WindowClientXDblSized=-2147483648
WindowClientYDblSized=-2147483648
DblSizeMode=1
EnableWindowMove=1
AlwaysOnTop=0
DisableControls=0
EOF2

# Wine gives no option for the desktop window POSITION, so place it with wmctrl
# once the window exists (it appears a few seconds after ChaosLauncher starts).
# Empty X/Y centers on the current X screen.
position_window() {
  if ! command -v wmctrl >/dev/null 2>&1; then
    return 0
  fi

  local x y
  if [ -n "$WINE_WINDOW_X" ] && [ -n "$WINE_WINDOW_Y" ]; then
    x="$WINE_WINDOW_X"; y="$WINE_WINDOW_Y"
  else
    local sr sw sh
    sr="$(xdotool getdisplaygeometry 2>/dev/null || echo '1920 1080')"
    sw="${sr% *}"; sh="${sr#* }"
    x=$(( (sw - WINE_WINDOW_WIDTH) / 2 ))
    y=$(( (sh - WINE_WINDOW_HEIGHT) / 2 ))
  fi

  for _ in $(seq 1 80); do
    if wmctrl -x -l | grep -q 'Default - Wine desktop'; then
      wmctrl -x -r 'Default - Wine desktop' -e "0,$x,$y,-1,-1"
      echo "[wine-game] Wine desktop placed at ($x,$y)."
      return 0
    fi
    sleep 0.1
  done
  echo "[wine-game] Wine desktop window not seen; leaving placement to the WM."
}

# --- 5. Start ChaosLauncher under Wine. It injects BWAPI into StarCraft and
#        (with "Run Starcraft on Startup") starts the game itself.
cd "$GAME_ROOT"
echo "[wine-game] starting ChaosLauncher in a ${WINE_WINDOW_WIDTH}x${WINE_WINDOW_HEIGHT} Wine desktop..."
env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" wine "$CHAOS_DIR/Chaoslauncher.exe" \
  > $LOG_DIR/chaoslauncher.log 2>&1 &
CHAOS_PID=$!
position_window &
echo "[wine-game] ChaosLauncher PID=$CHAOS_PID, game window should appear on display ${DISPLAY:-:0}."
echo "[wine-game] Log: $LOG_DIR/chaoslauncher.log"
echo "[wine-game] Start the Java client when the game is running:"
echo "  cd /sc-ai/Atlantis && java -jar bots/AtlantisP/AI/Atlantis.jar"
echo "$CHAOS_PID" > $LOG_DIR/chaoslauncher.pid
