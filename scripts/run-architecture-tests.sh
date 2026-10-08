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
#  - ArchUnit imports the classes from the compiled output, so the *whole* tree
#    has to be compiled first. Running this after `rm -rf out` used to fail all
#    seven rules with "failed to check any classes" - which reads like a broken
#    architecture and is really just a missing directory. Hence: compile the
#    production sources here instead of assuming someone ran the tests first.
set -euo pipefail

cd "$(dirname "$0")/.."

OUT="out/production/Atlantis"
LIB_CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
CP="$OUT:$LIB_CP"

if [ ! -d "$OUT/atlantis" ]; then
    echo "[arch] no compiled classes in $OUT - compiling the production tree..."
    mkdir -p "$OUT"
    find src -name '*.java' \
        | grep -v 'src/tests/unit/ATargetingTest.java' \
        | sort > out/arch-sources.txt
    # --release 8: $OUT is shared with the IDE run (main.Main is launched from it),
    # so a newer local default JDK must not write newer class files here.
    javac --release 8 -nowarn -cp "$LIB_CP" -d "$OUT" @out/arch-sources.txt
fi

echo "[arch] compiling boundary test..."
mkdir -p "$OUT"
javac --release 8 -nowarn -cp "$CP" -d "$OUT" src/tests/architecture/ArchitectureBoundaryTest.java

echo "[arch] running boundary test..."
java -cp ".:$CP" org.junit.platform.console.ConsoleLauncher \
  --select-class tests.architecture.ArchitectureBoundaryTest \
  --details=summary