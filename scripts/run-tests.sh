#!/usr/bin/env bash
# Compile the whole project from source and run the headless test suite.
#
# There is no build system in this repo (IntelliJ + vendored jars in lib/), so
# this script compiles every source file and runs JUnit via the standalone
# console launcher.
#
# Usage (from anywhere):
#   bash scripts/run-tests.sh                       # unit tests (default)
#   bash scripts/run-tests.sh --select-package tests # everything
#   bash scripts/run-tests.sh --select-class tests.architecture.ArchitectureBoundaryTest
#
# See DOCS/TESTING.md for the current known-failing baseline.
set -euo pipefail

cd "$(dirname "$0")/.."

OUT="out/production/Atlantis"
# Exclude lib-unused: it contains a conflicting older JBWAPI jar.
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"

mkdir -p "$OUT"
echo "[tests] compiling $(find src -name '*.java' | wc -l) sources..."
javac -nowarn -cp "$CP" -d "$OUT" $(find src -name '*.java')

if [ "$#" -eq 0 ]; then
  set -- --select-package tests.unit
fi

echo "[tests] running: $*"
java -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
  "$@" --details=summary --disable-ansi-colors
