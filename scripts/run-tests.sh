#!/usr/bin/env bash
# Compile the whole project from source and run the FAST test scopes only.
#
# There is no build system in this repo (IntelliJ + vendored jars in lib/), so
# this script compiles every source file and runs JUnit via the standalone
# console launcher.
#
# Speed policy (owner's ruling 2026-10-06): this script is the model-facing
# inner loop and must finish well under 40 s total. Measured per scope:
#   compile            ~8 s
#   tests.unit          2 s
#   tests.architecture  1 s
#   tests.acceptance   12 s   <- slow, owner-only (run-full-tests.sh)
#   tests.e2e          64 s   <- slow, owner-only (run-full-tests.sh)
# So the default is unit + architecture (~11 s). The slow scopes are refused
# here on purpose: they belong to scripts/run-full-tests.sh, which the owner
# runs manually. Pass --allow-slow to override (e.g. for a one-off debug).
#
# Usage (from anywhere):
#   bash scripts/run-tests.sh                       # unit + architecture
#   bash scripts/run-tests.sh --select-package tests.unit
#   bash scripts/run-tests.sh --allow-slow --select-package tests
#   bash scripts/run-tests.sh --select-class tests.architecture.ArchitectureBoundaryTest
#
# See DOCS/TESTING.md for the current known-failing baseline.
set -euo pipefail

cd "$(dirname "$0")/.."

# --- argument preprocessing: pull --allow-slow out of the junit args
ALLOW_SLOW=0
JUNIT_ARGS=()
for arg in "$@"; do
  if [ "$arg" = "--allow-slow" ]; then ALLOW_SLOW=1; else JUNIT_ARGS+=("$arg"); fi
done

# --- fast default when nothing else was asked for
if [ ${#JUNIT_ARGS[@]} -eq 0 ]; then
  JUNIT_ARGS=(
    --select-package tests.unit
    --select-package tests.architecture
    --select-class tests.e2e.QuickEconomySmokeTest
  )
fi

if [ "$ALLOW_SLOW" -eq 0 ]; then
  for arg in "${JUNIT_ARGS[@]}"; do
    case "$arg" in
      *tests.e2e|*tests.acceptance)
        echo "[tests] REFUSED: '$arg' is a slow scope and would blow the 40 s budget." >&2
        echo "[tests] Slow scopes are owner-only: bash scripts/run-full-tests.sh" >&2
        echo "[tests] (or re-run with --allow-slow if you really must this once)." >&2
        exit 2
        ;;
    esac
  done
  # The bare "tests" root package would include the slow scopes too.
  for arg in "${JUNIT_ARGS[@]}"; do
    if [ "$arg" = "tests" ]; then
      echo "[tests] REFUSED: '--select-package tests' includes the slow scopes" >&2
      echo "[tests] (acceptance 12 s + e2e 64 s). Default to unit+architecture," >&2
      echo "[tests] or use scripts/run-full-tests.sh (owner-only), or --allow-slow." >&2
      exit 2
    fi
  done
fi

OUT="out/production/Atlantis"
# Exclude lib-unused: it contains a conflicting older JBWAPI jar.
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"

mkdir -p "$OUT"
echo "[tests] compiling $(find src -name '*.java' | wc -l) sources..."
javac -nowarn -cp "$CP" -d "$OUT" $(find src -name '*.java')

echo "[tests] running: ${JUNIT_ARGS[*]}"
java -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
  "${JUNIT_ARGS[@]}" --details=summary --disable-ansi-colors
