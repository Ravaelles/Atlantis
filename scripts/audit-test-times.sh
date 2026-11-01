#!/usr/bin/env bash
# Audit per-test-class wall time across the suite (owner order 2026-10-31:
# no test over 20 s, max 3 mega-tests at 40 s, enforcement that fails loudly).
#
# One compile, one JUnit run with --reports-dir, then a sorted per-class table
# parsed from the XML reports. Flags every class over budget and verifies the
# mega-test count so a fourth mega-test fails the gate instead of silently
# growing the suite.
#
# Usage:
#   bash scripts/audit-test-times.sh                  # default gate (fast)
#   bash scripts/audit-test-times.sh --full           # everything (unit+arch+acceptance+e2e)
#   bash scripts/audit-test-times.sh --domain production,placement
#   bash scripts/audit-test-times.sh --reports-dir /tmp/my-reports
#
# Mega-tests: classes tagged @Tag("mega-test"), budget 40 s, max 3 in the tree
# (ideally 2: LongGameToSeventyTest + SimOpeningToFiftyTest). Everything else:
# budget 20 s. The JUnit timeout (src/test/resources/junit-platform.properties
# + @Timeout on megas) kills violators; this script is the visible ledger.
set -uo pipefail

cd "$(dirname "$0")/.."

MODE="gate"
REPORTS_DIR=""
DOMAIN=""
EXTRA_SELECTORS=()
while [ $# -gt 0 ]; do
  case "$1" in
    --full) MODE="full"; shift ;;
    --gate) MODE="gate"; shift ;;
    --domain=*) DOMAIN="${1#--domain=}"; MODE="domain"; shift ;;
    --domain) DOMAIN="${2:-}"; MODE="domain"; shift 2 ;;
    --reports-dir=*) REPORTS_DIR="${1#--reports-dir=}"; shift ;;
    --reports-dir) REPORTS_DIR="${2:-}"; shift 2 ;;
    *) EXTRA_SELECTORS+=("$1"); shift ;;
  esac
done
if [ "$MODE" = "domain" ] && [ -z "$DOMAIN" ]; then
  echo "[audit] --domain needs a value" >&2; exit 2
fi
if [ -n "${REPORTS_DIR:-}" ] && [ "$REPORTS_DIR" = "--full" -o "$REPORTS_DIR" = "--gate" ]; then
  echo "[audit] --reports-dir needs a value" >&2; exit 2
fi

OUT="out/production/Atlantis"
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
MEM="-Xmx6g"

mkdir -p "$OUT"
# Stale-class purge: see run-tests.sh (phantom tests from deleted sources).
find "$OUT" -name "*.class" -delete
if [ -z "$REPORTS_DIR" ]; then
  REPORTS_DIR="$(mktemp -d /tmp/atlantis-audit-XXXXXX)"
  CLEANUP_REPORTS=1
else
  mkdir -p "$REPORTS_DIR"
  CLEANUP_REPORTS=0
fi

echo "[audit] compiling $(find src -name '*.java' | wc -l) sources..."
javac --release 8 -nowarn -cp "$CP" -d "$OUT" $(find src -name '*.java')
# Same enforcement as the other runners: the JUnit default timeout lives
# in config and must be on the classpath root (owner order 2026-10-31).
cp config/junit-platform.properties "$OUT"/

# --- selectors mirror scripts/run-tests.sh so the audit measures the same gate
UNIT_PKGS=(tests.unit tests.unit.cache tests.unit.decision tests.unit.orders tests.unit.sim tests.unit.world)
JUNIT_ARGS=()
case "$MODE" in
  gate)
    for pkg in "${UNIT_PKGS[@]}" tests.architecture; do
      JUNIT_ARGS+=(--select-package "$pkg")
    done
    JUNIT_ARGS+=(--exclude-tag full-tier --exclude-tag production --exclude-tag placement)
    ;;
  full)
    JUNIT_ARGS=(--select-package tests.unit --select-package tests.architecture --select-package tests.acceptance --select-package tests.e2e)
    ;;
  domain)
    for pkg in "${UNIT_PKGS[@]}"; do
      JUNIT_ARGS+=(--select-package "$pkg")
    done
    IFS=',' read -ra DOMAINS <<< "$DOMAIN"
    for d in "${DOMAINS[@]}"; do JUNIT_ARGS+=(--include-tag "$d"); done
    ;;
esac
JUNIT_ARGS+=("${EXTRA_SELECTORS[@]}")

echo "[audit] mode=$MODE reports=$REPORTS_DIR"
echo "[audit] running: ${JUNIT_ARGS[*]}"
START=$(date +%s)
set +e
if [ "$MODE" = "full" ]; then
  # Bundled scopes first (fast), then e2e isolated: one JVM per class
  # (owner order 2026-11-01) - the sims accumulate harness state in a
  # shared JVM (measured 5-11x slowdown + order flips), so a shared run
  # measures garbage. Each class gets its own reports subdir (the
  # launcher overwrites one XML set per run).
  java $MEM -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
    --select-package tests.unit --select-package tests.architecture --select-package tests.acceptance \
    --details=summary --disable-ansi-colors \
    --reports-dir="$REPORTS_DIR" 2>&1 | tail -8
  STATUS=${PIPESTATUS[0]}
  for f in $(find src/tests/e2e -name "*Test.java" | sort); do
    cls=$(echo "$f" | sed 's|^src/||; s|\.java$||; s|/|.|g')
    short=$(basename "$cls")
    cstart=$(date +%s)
    java $MEM -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
      --select-class "$cls" --details=summary --disable-ansi-colors \
      --reports-dir="$REPORTS_DIR/e2e-$short" 2>&1 | tail -3
    cs=${PIPESTATUS[0]}
    [ "$cs" -ne 0 ] && STATUS=$cs
    echo "[audit] e2e $short: $(( $(date +%s) - cstart ))s wall (exit $cs)"
  done
else
  java $MEM -cp "$OUT:.:$CP" org.junit.platform.console.ConsoleLauncher \
    "${JUNIT_ARGS[@]}" --details=summary --disable-ansi-colors \
    --reports-dir="$REPORTS_DIR" 2>&1 | tail -15
fi
STATUS=${PIPESTATUS[0]}
set -e
SECONDS=$(( $(date +%s) - START ))
echo "[audit] suite wall: ${SECONDS}s (exit $STATUS)"

python3 - "$REPORTS_DIR" <<'PYEOF'
import sys, os, xml.etree.ElementTree as ET
from collections import defaultdict
d = sys.argv[1]
per_class = defaultdict(float)
per_class_tests = defaultdict(int)
failures = []
# Recursive: --full writes bundled XMLs at the top plus one subdir per
# isolated e2e class (the launcher overwrites one XML set per run).
xmls = []
for root, _, files in os.walk(d):
    for fn in files:
        if fn.endswith(".xml"):
            xmls.append(os.path.join(root, fn))
for f in xmls:
    try:
        r = ET.parse(f).getroot()
    except Exception as e:
        print(f"  ! unparsable {os.path.basename(f)}: {e}")
        continue
    # ConsoleLauncher writes one root testsuite with flat testcase children;
    # group by classname (the per-class wall time), not by the root suite.
    for tc in r.iter("testcase"):
        name = tc.get("classname") or "?"
        try: t = float(tc.get("time", 0) or 0)
        except: t = 0.0
        per_class[name] += t
        per_class_tests[name] += 1
        if tc.find("failure") is not None or tc.find("error") is not None:
            failures.append((name, tc.get("name")))
rows = sorted(per_class.items(), key=lambda kv: -kv[1])
print(f"\n{'seconds':>8} {'tests':>6}  class")
print("-" * 80)
over20, over40 = [], []
for name, t in rows[:40]:
    flag = ""
    if t > 40: flag = "  <-- OVER 40s (mega budget blown)"; over40.append(name)
    elif t > 20: flag = "  <-- OVER 20s"; over20.append(name)
    print(f"{t:8.2f} {per_class_tests[name]:6d}  {name}{flag}")
print("-" * 80)
print(f"classes: {len(rows)}, total test time: {sum(per_class.values()):.1f}s")
if over40: print(f"OVER-40 ({len(over40)}): {', '.join(over40)}")
if over20: print(f"OVER-20 ({len(over20)}): {', '.join(over20)}")
if failures:
    print(f"failures/errors: {len(failures)}")
    for cn, tn in failures[:10]:
        print(f"  FAIL {cn}#{tn}")
PYEOF

# --- mega-test count: at most 3 @Tag("mega-test") in the tree
MEGAS=$(grep -rl '@Tag("mega-test")' src/tests --include="*.java" 2>/dev/null | sort)
MEGA_N=$(echo "$MEGAS" | grep -c . || true)
echo ""
echo "[audit] mega-tests (@Tag mega-test): $MEGA_N / max 3"
echo "$MEGAS" | sed 's/^/[audit]   /'
if [ "$MEGA_N" -gt 3 ]; then
  echo "[audit] FAIL: more than 3 mega-tests - split or delete, never add a fourth" >&2
  exit 1
fi

# --- budget verdict (non-zero so CI fails loudly)
VIOLATIONS=$(python3 - "$REPORTS_DIR" <<'PYEOF'
import sys, os, glob, xml.etree.ElementTree as ET
from collections import defaultdict
d = sys.argv[1]
per_class = defaultdict(float)
megafiles = set()
import subprocess
out = subprocess.run(["grep", "-rl", '@Tag("mega-test")', "src/tests", "--include=*.java"],
                     capture_output=True, text=True).stdout.splitlines()
megas = set()
for f in out:
    base = os.path.basename(f).replace(".java", "")
    # map file to fully-qualified class by package line
    try:
        for line in open(f):
            if line.startswith("package "):
                pkg = line.split()[1].rstrip(";")
                megas.add(pkg + "." + base)
                break
    except: pass
xmls = []
for root, _, files in os.walk(d):
    for fn in files:
        if fn.endswith(".xml"):
            xmls.append(os.path.join(root, fn))
for f in xmls:
    try: r = ET.parse(f).getroot()
    except: continue
    for tc in r.iter("testcase"):
        try: t = float(tc.get("time", 0) or 0)
        except: t = 0.0
        name = tc.get("classname") or "?"
        per_class[name] += t
bad = 0
for name, t in per_class.items():
    budget = 40.0 if name in megas else 20.0
    if t > budget: bad += 1
print(bad)
PYEOF
)
echo "[audit] classes over budget: $VIOLATIONS"
if [ "${VIOLATIONS:-0}" -gt 0 ]; then
  echo "[audit] FAIL: $VIOLATIONS class(es) over budget (20 s, 40 s mega-only)" >&2
  exit 1
fi

if [ "$CLEANUP_REPORTS" = "1" ]; then
  echo "[audit] reports kept at $REPORTS_DIR (temp, not cleaned so you can inspect)"
fi
echo "[audit] PASS: every class within budget, mega count $MEGA_N/3"
