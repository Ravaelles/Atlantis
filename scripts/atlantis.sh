#!/usr/bin/env bash
# Play one Atlantis game through either backend from a single front door.
#
# Usage:
#   scripts/atlantis.sh --engine wine --map 1Drag_v_1Zeal.scm --headless [--env=FILE] [extra...]
#   scripts/atlantis.sh --engine openbw --map ums/rav/protoss/1Drag_v_1Zeal.scm [extra...]
#
#   --engine wine|openbw   Backend to play on. Default: wine.
#   --map PATH            Map to play. Wine resolves it by basename under the
#                         game root's maps/ tree (USE_MAP_SETTINGS unless it is
#                         an sscai/ melee map); OpenBW resolves it under the
#                         harness GAME_DIR (a bare name gets "maps/" prepended
#                         by build-openbw.sh). Required for OpenBW, optional
#                         for Wine (which falls back to its own default map).
#   Wine runs headless by default (Xvfb :99, invisible) with the repo's
#   unattended-speed env (bwapi-data/AI/ENV-WINE-FAST: WINE_SUPER_FAST_MODE,
#   speed 0, frameskip, GUI off - the speed lives in the env file, headless
#   is only the display). A short notice is printed every headless run.
#   --visible             Watch the game (Wine virtual desktop), like the old
#                         default: no fast env is forced, only an explicit
#                         --env= applies.
#   --headless|--hidden   Explicit headless (the default for Wine).
#   --env=FILE            Wine only: per-run env file (never a shared ENV -
#                         scripts must not edit those). Resolved against the
#                         repo root when relative. Overrides the headless
#                         default env; works in both display modes.
#   --dry-run             Print the dispatched command without running it.
#   -h|--help             This text.
#
# Tuning knobs ride the environment, not this script's flags:
#   OpenBW: TIMEOUT_SECONDS=12 (fast vehicle check), OPENBW_EXTRA_ENV
#           (newline-separated KEY=VALUE lines for the bot env).
#   Wine:   everything lives in the --env= file (see bwapi-data/AI/ENV-WINE-FAST
#           for the unattended-speed shape).
#
# Anything else is forwarded verbatim to the underlying script
# (run-wine-full.sh, which warns on extras, or build-openbw.sh, which
# fails on unknown flags) - beyond the Wine headless/fast-env defaults
# above, this script adds no behaviour of its own. One game at a time:
# the Wine runner holds its own flock; do not start a second game of
# either backend alongside.
#
# Exit code is the underlying script's exit code.
set -euo pipefail

ATLANTIS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

ENGINE="wine"
MAP=""
# Wine display mode: headless=1 unless --visible. Speed lives in the env
# file (WINE_SUPER_FAST_MODE), headless is only the display - but the two
# ship together by default: unattended runs want both.
HEADLESS=1
HEADLESS_EXPLICIT=0
VISIBLE_FLAG=0
ENV_FILE=""
ENV_EXPLICIT=0
DRY_RUN=0
EXTRA=()

usage() {
  sed -n '2,32p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

while [ $# -gt 0 ]; do
  case "$1" in
    --engine=*) ENGINE="${1#--engine=}"; shift ;;
    --engine)
      [ $# -ge 2 ] || { echo "[atlantis] --engine requires wine|openbw" >&2; exit 2; }
      ENGINE="$2"; shift 2 ;;
    --map=*) MAP="${1#--map=}"; shift ;;
    --map)
      [ $# -ge 2 ] || { echo "[atlantis] --map requires a path" >&2; exit 2; }
      MAP="$2"; shift 2 ;;
    --env=*) ENV_FILE="${1#--env=}"; ENV_EXPLICIT=1; shift ;;
    --headless|--hidden) HEADLESS=1; HEADLESS_EXPLICIT=1; shift ;;
    --visible) HEADLESS=0; VISIBLE_FLAG=1; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    -h|--help) usage; exit 0 ;;
    --*) EXTRA+=("$1"); shift ;;
    *)
      if [ -z "$MAP" ]; then MAP="$1"; else EXTRA+=("$1"); fi
      shift ;;
  esac
done

case "$ENGINE" in
  wine) ;;
  openbw) ;;
  *) echo "[atlantis] unknown --engine: $ENGINE (want wine|openbw)" >&2; exit 2 ;;
esac

run() {
  if [ "$DRY_RUN" -eq 1 ]; then
    printf '[atlantis] would run:'; printf ' %q' "$@"; echo
  else
    "$@"
  fi
}

if [ "$ENGINE" = "wine" ]; then
  # Unattended default: headless display plus the repo's fast env, unless
  # the caller asked otherwise. --visible alone restores the old watched
  # default (no forced env); an explicit --env= always wins.
  if [ "$VISIBLE_FLAG" -eq 0 ] && [ "$ENV_EXPLICIT" -eq 0 ]; then
    ENV_FILE="bwapi-data/AI/ENV-WINE-FAST"
  fi
  if [ "$HEADLESS" -eq 1 ]; then
    echo "Running headless... If you want to see StarCraft, pass --visible"
  fi
  CMD=(bash "$ATLANTIS_DIR/scripts/run-wine-full.sh")
  [ -n "$MAP" ] && CMD+=("$MAP")
  [ -n "$ENV_FILE" ] && CMD+=("--env=$ENV_FILE")
  [ "$HEADLESS" -eq 1 ] && CMD+=(--headless)
  CMD+=("${EXTRA[@]+"${EXTRA[@]}"}")
  run "${CMD[@]}"
  exit $?
fi

# OpenBW: build-openbw.sh --game is the proven vehicle (rebuild when stale,
# real 1v1 with verdict line). --headless is meaningless here (always
# headless) and build-openbw.sh would reject it, so drop it loudly -
# but only when the caller really passed it (headless is the Wine
# default, not an OpenBW request).
if [ "$HEADLESS_EXPLICIT" -eq 1 ]; then
  echo "[atlantis] note: --headless is Wine-only, OpenBW is headless by design - dropping it" >&2
fi
if [ -n "$ENV_FILE" ]; then
  echo "[atlantis] note: --env= is Wine-only; OpenBW tunes via TIMEOUT_SECONDS=/OPENBW_EXTRA_ENV= - dropping it" >&2
fi
if [ -z "$MAP" ]; then
  echo "[atlantis] --map is required with --engine openbw" >&2
  exit 2
fi
CMD=(bash "$ATLANTIS_DIR/scripts/build-openbw.sh" --game "$MAP")
CMD+=("${EXTRA[@]+"${EXTRA[@]}"}")
run "${CMD[@]}"
exit $?
