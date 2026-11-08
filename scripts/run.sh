#!/usr/bin/env bash
# One command for a watched Wine game: map + races from flags, no jar
# rebuild when the jar is fresh.
#
# Usage: ./scripts/run.sh --map=<path> [options]
#   Example: ./scripts/run.sh --race=Protoss --map=ums/rav/protoss/15Drag_v_16Drag.scm --visible
#   Maps:    bash scripts/list-maps.sh [filter]
#
# Required parameters:
#   --map=<path>        map file, relative to the game's maps/ folder
#                       e.g. ums/rav/protoss/15Drag_v_16Drag.scm
#                       (a leading maps/ is stripped; find names with list-maps.sh)
# Optional parameters (sane defaults apply, --help lists the rest):
#   --race, --enemy-race, --visible / --headless,
#   --jar, --build / --no-build, --timeout, --env
#
# This is a thin wrapper over scripts/run-wine-full.sh (the full
# client-first pipeline: wine JVM, ChaosLauncher, bwapi.ini, HELLO check,
# teardown). The client owns our race (Main.ourRace()), so --race must
# match it; the enemy race rides a generated --env= file.
set -u

REPO="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO"

usage() {
  sed -n '2,17p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

MAP=""
RACE=""
ENEMY_RACE=""
VISIBILITY=""
JAR="/sc-ai/BOTS/AtlantisP/AI/Atlantis.jar"
BUILD=0
TIMEOUT=0
ENV_FILE=""
PASSTHROUGH=()
for arg in "$@"; do
  case "$arg" in
    --map=*) MAP="${arg#--map=}" ;;
    --race=*) RACE="${arg#--race=}" ;;
    --enemy-race=*) ENEMY_RACE="${arg#--enemy-race=}" ;;
    --visible) VISIBILITY="--visible" ;;
    --headless|--hidden) VISIBILITY="--headless" ;;
    --jar=*) JAR="${arg#--jar=}" ;;
    --build) BUILD=1 ;;
    --no-build) BUILD=0 ;;
    --timeout=*) TIMEOUT="${arg#--timeout=}" ;;
    --env=*) ENV_FILE="${arg#--env=}" ;;
    -h|--help) usage; echo ""; echo "Anything else is passed to scripts/run-wine-full.sh."; exit 0 ;;
    *) PASSTHROUGH+=("$arg") ;;
  esac
done

if [ -z "$MAP" ]; then
  echo "[run] Missing required parameter --map (BWAPI would silently play the previous map)." >&2
  usage >&2
  exit 2
fi
# The game loads maps/<path> from bwapi.ini; the client resolves bare
# names under maps/ums and maps/sscai - one canonical form for both.
MAP="${MAP#maps/}"

# Our race is owned by the client (Main.ourRace()), which is also what
# run-wine-full.sh writes into bwapi.ini - a flag cannot change it, so a
# mismatch fails fast instead of silently playing the wrong race.
CLIENT_RACE="$(sed -n '/public static String ourRace/,/^    }/p' src/main/Main.java \
  | grep -vE '^\s*//' | grep -oE 'return "(Protoss|Terran|Zerg)"' | head -1 | sed -E 's/.*"(.*)".*/\1/')"
if [ -n "$RACE" ] && [ -n "$CLIENT_RACE" ] && [ "$RACE" != "$CLIENT_RACE" ]; then
  echo "[run] --race=$RACE does not match Main.ourRace()=$CLIENT_RACE; flip the source first." >&2
  exit 2
fi

# The enemy race rides an env file (Main.setEnemyRaceOverride via ENV key
# ENEMY_RACE): a generated file keeps the shared ENV untouched.
if [ -n "$ENEMY_RACE" ]; then
  if [ -n "$ENV_FILE" ]; then
    echo "[run] --enemy-race and --env together: --env wins, --enemy-race ignored." >&2
  else
    ENV_FILE="$(mktemp /tmp/opencode/atlantis-env.XXXXXX)"
    cp bwapi-data/AI/ENV "$ENV_FILE"
    grep -vE '^ENEMY_RACE=' "$ENV_FILE" > "$ENV_FILE.tmp" && mv "$ENV_FILE.tmp" "$ENV_FILE"
    echo "ENEMY_RACE=$ENEMY_RACE" >> "$ENV_FILE"
  fi
fi

# No rebuild when the jar is fresh (owner 2026-11-09: launching games must
# not recompile every time). BUILD=0 plays the jar on disk; --build
# forces a rebuild; a missing jar builds once. Stale-vs-sources only
# warns - the game plays what is there.
if [ ! -f "$JAR" ]; then
  echo "[run] No bot jar at $JAR - building it first (once)."
  bash scripts/build-bot-jar.sh || exit 1
elif [ "$BUILD" = "1" ]; then
  bash scripts/build-bot-jar.sh || exit 1
else
  if ! bash scripts/check-jar-freshness.sh "$JAR" --quiet; then
    echo "[run] WARNING: jar is stale vs the working tree (playing it anyway - pass --build to rebuild)."
  fi
fi

FULL_ARGS=("$MAP")
[ -n "$VISIBILITY" ] && FULL_ARGS+=("$VISIBILITY")
[ -n "$ENV_FILE" ] && FULL_ARGS+=("--env=$ENV_FILE")
FULL_ARGS+=("--force-this-bad-practice" "${PASSTHROUGH[@]}")

# run-wine-full.sh exits at HELLO (bot attached), not at game end: --timeout
# supervises the whole game, then quiets the box. Without it this returns
# once the bot is in the game and the game plays on.
if [ "$TIMEOUT" -gt 0 ] 2>/dev/null; then
  timeout "$TIMEOUT" env BUILD=0 JAR_OUT="$JAR" bash scripts/run-wine-full.sh "${FULL_ARGS[@]}"
  code=$?
  if [ "$code" -eq 124 ]; then
    echo "[run] timeout ${TIMEOUT}s hit - quieting the box."
    bash scripts/kill-leftovers.sh >/dev/null 2>&1 || true
  fi
  exit "$code"
fi

exec env BUILD=0 JAR_OUT="$JAR" bash scripts/run-wine-full.sh "${FULL_ARGS[@]}"
