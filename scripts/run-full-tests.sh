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
# Test JVM heap (owner order 2026-11-01): the full suite allocates sim
# worlds by the thousand; the default heap GC-thrashes late in the run
# (e2e last = slowest). Size once, everywhere.
MEM="-Xmx6g"

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
# Stale-class purge: see run-tests.sh (phantom tests from deleted sources).
find "$OUT" -name "*.class" -delete
echo "[tests] compiling $(find src -name '*.java' | wc -l) sources..."
# --release 8: see run-tests.sh. $OUT is shared with the IDE run, so the class
# files must be Java 8 on every machine regardless of the local default JDK.
if ! javac --release 8 -nowarn -cp "$CP" -d "$OUT" $(find src -name '*.java'); then
  echo "[tests] COMPILE FAILED - no scope was run" >&2
  exit 1
fi
# Test-budget enforcement (owner order 2026-10-31): see run-tests.sh.
cp config/junit-platform.properties "$OUT"/

failed=()
echo
printf '%-14s %-8s %s\n' "scope" "seconds" "result"
echo "---------------------------------------------"

for entry in "${SCOPES[@]}"; do
  [ -n "$entry" ] || continue
  name="${entry%%:*}"
  package="${entry#*:}"

  # E2E runs isolated, one JVM per class (owner order 2026-11-01): the
  # sims accumulate harness state in a shared JVM (measured 5-11x
  # slowdown + order flips in the full run; solo times stay in budget),
  # so one shared run measures garbage, not the tests. Fresh JVM per
  # class = true times; the ~2 s fork cost is nothing next to sims.
  if [ "$name" = "scenarios" ]; then
    iso_ok=0; iso_fail=0; iso_failed_classes=()
    for f in $(find "src/${package//.//}" -name "*Test.java" | sort); do
      cls=$(echo "$f" | sed 's|^src/||; s|\.java$||; s|/|.|g')
      start=$(date +%s)
      output=$(java $MEM -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
        --select-class "$cls" --details=summary --disable-ansi-colors --disable-banner "$@" 2>&1)
      status=$?
      seconds=$(( $(date +%s) - start ))
      passed=$(echo "$output" | grep -oE '^\[ +[0-9]+ tests successful' | grep -oE '[0-9]+')
      broken=$(echo "$output" | grep -oE '^\[ +[0-9]+ tests failed' | grep -oE '[0-9]+')
      if [ "$status" -eq 0 ] && [ "${broken:-0}" = "0" ] && [ -n "${passed:-}" ]; then
        printf '  %-30s %-8s %s\n' "$cls" "${seconds}s" "${passed} passing"
        iso_ok=$((iso_ok + passed))
      else
        printf '  %-30s %-8s %s\n' "$cls" "${seconds}s" "FAILED: ${broken:-?} failing"
        iso_fail=$((iso_fail + ${broken:-1}))
        iso_failed_classes+=("$cls")
        echo "$output" | grep -E "MethodSource|=> " | head -n 8
      fi
    done
    if [ "$iso_fail" = "0" ]; then
      printf '%-14s %-8s %s\n' "$name" "" "${iso_ok} passing (isolated)"
    else
      printf '%-14s %-8s %s\n' "$name" "" "FAILED: ${iso_fail} failing"
      failed+=("$name")
    fi
    continue
  fi

  start=$(date +%s)
  output=$(java $MEM -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
    --select-package "$package" --details=summary --disable-ansi-colors --disable-banner "$@" 2>&1)
  status=$?
  seconds=$(( $(date +%s) - start ))

  passed=$(echo "$output" | grep -oE '^\[ +[0-9]+ tests successful' | grep -oE '[0-9]+')
  broken=$(echo "$output" | grep -oE '^\[ +[0-9]+ tests failed' | grep -oE '[0-9]+')

  if [ "$status" -eq 0 ] && [ "${broken:-0}" = "0" ] && [ -n "${passed:-}" ]; then
    printf '%-14s %-8s %s\n' "$name" "${seconds}s" "${passed} passing"
  else
    if [ -z "${passed:-}" ] && [ -z "${broken:-}" ]; then
      # Same hole as run-tests.sh had: the JVM can die before JUnit prints
      # its summary (measured: missing ENV -> System.exit(0) in setUp),
      # and status 0 with no counters used to report "? passing" green.
      printf '%-14s %-8s %s\n' "$name" "${seconds}s" "FAILED: no JUnit summary (JVM died before reporting?)"
    else
      printf '%-14s %-8s %s\n' "$name" "${seconds}s" "FAILED: ${broken:-?} failing, ${passed:-?} passing"
    fi
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