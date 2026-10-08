#!/usr/bin/env bash
# Run every test scope in one compile, with per-scope timings.
#
# Why this exists: `scripts/run-tests.sh` is the fast inner loop and must stay
# that way - by default it compiles the tree and runs `tests.unit` only (2.6 s of
# tests plus ~4 s of compile, measured 2026-10-04). The slow scopes are separate
# commands on purpose:
#
#   tests.unit         2.6 s     the fast loop
#   tests.architecture 2.0 s     seven boundary rules
#   tests.acceptance   12.2 s    world/squad/commander behaviour
#   tests.e2e          ~100 s    the stub-world rush scenarios (a 900-frame game
#                                 per test; two of the four stop early)
#
# So the whole suite is ~2.5 minutes, and 96% of that is the scenario tier. This
# script is the "run everything" answer: one compile instead of five, one number
# per scope so the slow one is visible rather than mysterious, and a non-zero exit
# if any scope failed.
#
# Usage (from anywhere):
#   bash scripts/run-full-tests.sh                 # everything
#   bash scripts/run-full-tests.sh --skip-scenarios  # everything but tests.e2e
#
# Per-scope alternative: scripts/run-tests.sh, run-acceptance-tests.sh,
# run-scenario-tests.sh, run-architecture-tests.sh.
set -uo pipefail

cd "$(dirname "$0")/.."

OUT="out/production/Atlantis"
# Exclude lib-unused: it contains a conflicting older JBWAPI jar.
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"

SCOPES=(
  "unit:tests.unit"
  "architecture:tests.architecture"
  "acceptance:tests.acceptance"
  "scenarios:tests.e2e"
)
if [ "${1:-}" = "--skip-scenarios" ]; then
  shift
  SCOPES=("${SCOPES[@]/scenarios:tests.e2e/}")
fi

mkdir -p "$OUT"
echo "[tests] compiling $(find src -name '*.java' | wc -l) sources..."
# --release 8: see run-tests.sh. $OUT is shared with the IDE run, so the class
# files must be Java 8 on every machine regardless of the local default JDK.
if ! javac --release 8 -nowarn -cp "$CP" -d "$OUT" $(find src -name '*.java'); then
  echo "[tests] COMPILE FAILED - no scope was run" >&2
  exit 1
fi

failed=()
echo
printf '%-14s %-8s %s\n' "scope" "seconds" "result"
echo "---------------------------------------------"

for entry in "${SCOPES[@]}"; do
  [ -n "$entry" ] || continue
  name="${entry%%:*}"
  package="${entry#*:}"

  start=$(date +%s)
  output=$(java -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
    --select-package "$package" --details=summary --disable-ansi-colors --disable-banner "$@" 2>&1)
  status=$?
  seconds=$(( $(date +%s) - start ))

  passed=$(echo "$output" | grep -oE '^\[ +[0-9]+ tests successful' | grep -oE '[0-9]+')
  broken=$(echo "$output" | grep -oE '^\[ +[0-9]+ tests failed' | grep -oE '[0-9]+')

  if [ "$status" -eq 0 ] && [ "${broken:-0}" = "0" ]; then
    printf '%-14s %-8s %s\n' "$name" "${seconds}s" "${passed:-?} passing"
  else
    printf '%-14s %-8s %s\n' "$name" "${seconds}s" "FAILED: ${broken:-?} failing, ${passed:-?} passing"
    failed+=("$name")
    echo "$output" | grep -E "MethodSource|=> " | head -n 20
  fi
done

echo "---------------------------------------------"
if [ ${#failed[@]} -gt 0 ]; then
  echo "[tests] FAILED scopes: ${failed[*]}"
  exit 1
fi

echo "[tests] all scopes green"