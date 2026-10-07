#!/usr/bin/env bash
# Run the OpenBW E2E tier: Atlantis (a Java bot) playing a real headless game
# against the engine's own DemoAI, inside the StardustDevEnvironment harness.
#
# This is Stage 3 of _AI/IDEA-E2E-TESTS.md: the harness normally links C++
# AIModules, and Atlantis is `java -jar`, so the harness gained a
# `tests-atlantis` target whose ProcessAIModule spawns the bot and lets it
# attach through the BWAPI client protocol. The result is the only tier that
# runs the whole bot on real engine physics - economy, production, combat, map
# analysis together - instead of the stub world the unit tests use.
#
# Usage:
#   bash scripts/run-openbw-e2e.sh                 # build the jar + the runner, play
#   bash scripts/run-openbw-e2e.sh --list          # just list the scenarios
#   bash scripts/run-openbw-e2e.sh --no-build      # reuse the existing jar
#
# Requirements: the StardustDevEnvironment build tree (cmake --build there), a
# JDK, and the bot jar. Not part of the fast loop: a real game takes minutes,
# which is why this is owner-only like scripts/run-full-tests.sh.
#
# CONVENTIONS §13: every command here is bounded (this script's whole run is
# wrapped in a 15-minute timeout by the caller, and the runner itself is capped
# lower).
set -euo pipefail

ATLANTIS_DIR="/sc-ai/Atlantis"
HARNESS_DIR="/sc-ai/StardustDevEnvironment"
BUILD_DIR="$HARNESS_DIR/build"
RUNNER="$BUILD_DIR/test/tests-atlantis"
JAR="$ATLANTIS_DIR/bots/AtlantisP/AI/Atlantis.jar"
BOT_DIR="$ATLANTIS_DIR/bots/AtlantisP/AI"

BUILD=1
for arg in "$@"; do
  case "$arg" in
    --list) RUNNER_ARG="--gtest_list_tests" ;;
    --no-build) BUILD=0 ;;
  esac
done

if [ "$BUILD" -eq 1 ]; then
  echo "[openbw-e2e] building the bot jar"
  timeout 600 bash "$ATLANTIS_DIR/scripts/build-bot-jar.sh" "$JAR" >/dev/null

  echo "[openbw-e2e] building the tests-atlantis runner"
  timeout 900 cmake --build "$BUILD_DIR" --target tests-atlantis -j "$(nproc)" >/dev/null
fi

if [ ! -x "$RUNNER" ]; then
  echo "[openbw-e2e] runner not found at $RUNNER" >&2
  echo "[openbw-e2e] configure/build the harness first: cmake -S $HARNESS_DIR -B $BUILD_DIR" >&2
  exit 2
fi
if [ ! -f "$JAR" ]; then
  echo "[openbw-e2e] bot jar not found at $JAR (drop --no-build to build it)" >&2
  exit 2
fi

# Point the spawned bot at this checkout. ATLANTIS_DIR is where it runs, so its
# relative map/build-order lookups resolve the same way they do in a game.
export ATLANTIS_JAR="$JAR"
export ATLANTIS_DIR="$BOT_DIR"

echo "[openbw-e2e] running: ${RUNNER_ARG:-all scenarios}"
echo "[openbw-e2e] bot: $ATLANTIS_JAR (cwd $ATLANTIS_DIR)"

# The harness needs the game data (StarDat/BrooDat/Patch_rt .mpq) and its maps/
# tree in the working directory - that is exactly why the Stardust tests run
# from build/test. Not the build root: measured, that fails with
# "file_reader: failed to open ./Patch_rt.mpq for reading" before a frame runs.
RUN_DIR="$BUILD_DIR/test"
if [ ! -f "$RUN_DIR/Patch_rt.mpq" ]; then
  echo "[openbw-e2e] game data not found in $RUN_DIR (expected *.mpq there)" >&2
  exit 2
fi

cd "$RUN_DIR"
# The harness plays one real game per scenario; 15 minutes is the outer bound
# required by CONVENTIONS §13, and a scenario that needs more is a bug.
timeout 900 "$RUNNER" ${RUNNER_ARG:-} --gtest_color=no
exit $?
