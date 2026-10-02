#!/usr/bin/env bash
# Stage B — run the architecture boundary tests headlessly.
#
# There is no build system in this repo (IntelliJ + vendored jars in lib/), so
# this script compiles and runs the ArchUnit boundary test directly.
#
# Run from the project root:  bash scripts/run-architecture-tests.sh
#
# Notes:
#  - The frozen baseline store is _AI/architecture/archunit-store (see
#    archunit.properties). It is expected to be non-empty while we pay down the
#    backlog; it must not grow with new violations.
set -euo pipefail

cd "$(dirname "$0")/.."

OUT="out/production/Atlantis"
CP="$OUT:$(find lib -name '*.jar' | tr '\n' ':')"

echo "[arch] compiling boundary test..."
javac -cp "$CP" -d "$OUT" src/tests/architecture/ArchitectureBoundaryTest.java

echo "[arch] running boundary test..."
java -cp ".:$CP" org.junit.platform.console.ConsoleLauncher \
  --select-class tests.architecture.ArchitectureBoundaryTest \
  --details=tree
