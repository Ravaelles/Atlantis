#!/usr/bin/env bash
# Compile the whole project from source and run the FAST test scopes only.
#
# There is no build system in this repo (IntelliJ + vendored jars in lib/), so
# this script compiles every source file and runs JUnit via the standalone
# console launcher.
#
# Speed policy (owner's ruling 2026-10-06, re-measured 2026-10-20):
# this script is the model-facing inner loop and must finish well under
# 40 s total. Measured (wall): compile 4.4 s, tests.unit 7.7 s
# (combat 0.7 s, production 2.6 s, placement/map 0.5 s, misc 2.4 s),
# tests.architecture ~1 s, e2e smokes 0.5 s. Total ~13-14 s.
# The 2.2 s QuickEconomySmokeTest (1 test) lives in the full tier only;
# the other 5 smoke classes cost 0.5 s together and stay as the
# one-of-each-kind tripwires. The unit scope is NOT split into
# stable/churn tiers: production/placement/map are mid-migration
# (#144-#147) and the split would unplug proof from the files being
# rewritten - revisit when they settle; the combat half costs 0.7 s,
# so keeping it is free. The slow scopes (acceptance, full e2e) are refused
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
    --select-class tests.e2e.ProductionEngineSmokeTest
    --select-class tests.e2e.WorkerProductionTest
    --select-class tests.e2e.OpeningDoesNotHoardMineralsTest
    --select-class tests.e2e.WorkerDefenceTest
    --select-class tests.e2e.WorkerSwarmTest
    --exclude-tag full-tier
  )
  # NOTE: --exclude-tag applies only to this default set. An explicit
  # selector (--select-class/--select-package on the command line) runs
  # exactly what was asked, full-tier tags included.
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
# --release 8, for the same reason build-bot-jar.sh uses it: this tree compiles
# into $OUT, which is the directory the IDE runs main.Main from. Without the flag
# the local javac decides the class-file version, and on any machine whose default
# JDK is newer than 8 the IDE output is that newer version - so the Wine bot's
# Java 8 JVM dies on UnsupportedClassVersionError the moment JNativeHook's dispatch
# thread reaches a class (measured 2026-10-08: atlantis/keyboard/KeyRelay, class
# file 61.0 against a runtime that accepts up to 52.0, keyboard dead).
javac --release 8 -nowarn -cp "$CP" -d "$OUT" $(find src -name '*.java')

# The architecture scope is split out so it can be ADVISORY without weakening
# anything else (owner's ruling, 2026-10-11): "we should be moving towards fixing
# it, but it should not break whole builds - that slows the work down; it is a hint,
# not a requirement."
#
# The split is the important part. Running everything in ONE pass and downgrading
# any failure that mentions `tests.architecture` would also silence a REAL unit-test
# regression in the same invocation - a red unit test is not a hint, it is a broken
# build. So: the non-architecture scope keeps its exit code untouched, and only the
# architecture pass is advisory.
#
# What this does NOT do: hide a violation. The rule still runs, still prints the
# dependency that broke it, and `scripts/run-architecture-tests.sh` stays the
# authoritative gate for the owner. CONVENTIONS §5 is unchanged - never grow the
# frozen store, fix the dependency.
# Filter the PAIR, not the value: `--select-package tests.architecture` is a flag plus
# its argument, and separating them leaves a dangling `--select-package` that swallows
# the next flag (measured: ConsoleLauncher answered "Expected parameter for option
# '--select-package' but found '--select-class'" and printed its help instead of
# running). Walk the args and drop the flag together with its architecture value.
RUN_ARGS=()
ARCH_ARGS=()
i=0
while [ "$i" -lt "${#JUNIT_ARGS[@]}" ]; do
  arg="${JUNIT_ARGS[$i]}"
  case "$arg" in
    --select-package|--select-class|--select-method)
      next="${JUNIT_ARGS[$((i + 1))]:-}"
      case "$next" in
        tests.architecture|tests.architecture.*)
          ARCH_ARGS+=("$arg" "$next") ;;
        *)
          RUN_ARGS+=("$arg" "$next") ;;
      esac
      i=$((i + 2))
      ;;
    *)
      RUN_ARGS+=("$arg")
      i=$((i + 1))
      ;;
  esac
done

# `--select-package tests.architecture` in the default set is the common case; a run
# that only asked for architecture has nothing else to run.
ARCH_ADVISORY=0
[ ${#ARCH_ARGS[@]} -gt 0 ] && ARCH_ADVISORY=1

if [ ${#RUN_ARGS[@]} -gt 0 ]; then
  echo "[tests] running: ${RUN_ARGS[*]}"
  status=0
  output=$(java -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
    "${RUN_ARGS[@]}" --details=summary --disable-ansi-colors 2>&1) || status=$?
  echo "$output"
  if [ "$status" -ne 0 ]; then exit "$status"; fi
  # A scope that prints no JUnit summary ran zero tests: the JVM died
  # before the launcher could report (measured: a missing ENV file makes
  # Env.readEnvFile call AGame.exit -> System.exit(0) in the first world
  # setUp, so the "green" run executed nothing and exited 0). A missing
  # summary is a failure, never a pass.
  if ! echo "$output" | grep -qE '^\[ +[0-9]+ tests (found|successful)'; then
    echo "[tests] NO JUNIT SUMMARY - the JVM exited before reporting (0 tests executed?)" >&2
    exit 1
  fi
fi

if [ "$ARCH_ADVISORY" -eq 1 ]; then
  echo "[tests] running (advisory): ${ARCH_ARGS[*]}"
  if java -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
       "${ARCH_ARGS[@]}" --details=summary --disable-ansi-colors; then
    echo "[tests] architecture: clean"
  else
    # Plain stdout - NOT `tee /dev/stderr`. The first version piped the message through
    # tee, and under `>log 2>&1` the warning text vanished entirely (measured: only the
    # separator lines survived, so the one thing the warning exists to say was lost).
    echo ""
    echo "[tests] ============================================================"
    echo "[tests] ARCHITECTURE WARNING (advisory - this did NOT fail the build)"
    echo "[tests] A boundary rule fired. Treat it as a hint to fix the dependency, not"
    echo "[tests] a blocker. See the failing rule above and _AI/CONVENTIONS.md §5:"
    echo "[tests] fix the dependency - never grow _AI/architecture/archunit-store/."
    echo "[tests] Authoritative gate: bash scripts/run-architecture-tests.sh"
    echo "[tests] ============================================================"
    echo ""
  fi
fi

exit 0
