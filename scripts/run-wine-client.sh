#!/usr/bin/env bash
# Run the Atlantis bot JVM UNDER WINE, so it shares the Windows-side IPC
# namespace with BWAPI inside StarCraft (also under Wine).
#
# Why this exists (measured 2026-10-06): BWAPI under Wine creates Windows
# named sections (via wineserver). A NATIVE Linux JVM cannot see them, so the
# bot never attaches even though the game runs and looks fine. Running the
# JVM itself under Wine puts the client in the same namespace - the only
# known way to make real StarCraft + a Java BWAPI client work on Linux.
#
# Usage: scripts/run-wine-client.sh [race-jar]
#   default jar: bots/AtlantisP/AI/Atlantis.jar
#
# Prerequisite: the game is already running (start it with
# scripts/run-wine-game.sh or from ChaosLauncher by hand).
set -euo pipefail

WINE_ROOT="${WINEPREFIX:-$HOME/.wine}"
JAVA_EXE="$WINE_ROOT/drive_c/Java/bin/java.exe"
JAR="${1:-/sc-ai/Atlantis/bots/AtlantisP/AI/Atlantis.jar}"

if [ ! -f "$JAVA_EXE" ]; then
  echo "[wine-client] Windows Java not found at $JAVA_EXE"
  echo "[wine-client] Install it once with winetricks or the Temurin 8 MSI:"
  echo "[wine-client]   wine msiexec /i temurin8.msi ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /quiet INSTALLDIR=C:\\Java"
  exit 1
fi

if ! pgrep -x StarCraft.exe >/dev/null; then
  echo "[wine-client] StarCraft is NOT running. Start the game first:"
  echo "[wine-client]   scripts/run-wine-game.sh"
  exit 1
fi

echo "[wine-client] starting the bot JVM under Wine (jar: $JAR)"
exec env WINEDEBUG=-all DISPLAY="${DISPLAY:-:0}" \
  wine "$JAVA_EXE" -jar "$(winepath -w "$JAR")"
