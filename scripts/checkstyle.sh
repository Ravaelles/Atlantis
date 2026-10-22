#!/usr/bin/env bash
# Java style gate for Atlantis (the ESLint equivalent: Checkstyle).
#
# Usage:
#   bash scripts/checkstyle.sh            # check all of src/
#   bash scripts/checkstyle.sh <path...>  # check only these (must be under src/)
#   bash scripts/checkstyle.sh --apply [<path...>]  # auto-fix: insert the
#     missing blank lines after non-final returns, then re-check. Checkstyle
#     itself can only report (it has no --fix); --apply is our own fixer and
#     only ever ADDS blank lines (semantics-preserving). It fixes exactly
#     what the return-blank-line rules report - nothing else is touched.
#
# Scope is src/ ONLY, always: production code and tests. src-old/ and every
# other directory are never touched (owner rule).
#
# This is an ON-DEMAND script, not part of scripts/run-tests.sh: run it before
# committing style-sensitive work, not on every change.
#
# How it works: Checkstyle has no single-jar distribution anymore, so the
# pinned version plus its runtime dependencies are resolved once with Maven
# into ~/.cache/atlantis/checkstyle-<version>/ (outside the repo - a helper
# tool is a means, not a deliverable). Later runs reuse the cache and need no
# network. The game code itself is unaffected: Checkstyle only READS sources
# (run under the machine JDK, whatever it is; the game jar stays --release 8).
#
# Exit codes: 0 = clean, 1 = violations found, 2 = tooling problem.
set -uo pipefail

CHECKSTYLE_VERSION="10.23.0"
CACHE_DIR="${HOME}/.cache/atlantis/checkstyle-${CHECKSTYLE_VERSION}"
LIB_DIR="${CACHE_DIR}/lib"
MAIN_JAR="${LIB_DIR}/checkstyle-${CHECKSTYLE_VERSION}.jar"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ATLANTIS_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
CONFIG="${ATLANTIS_DIR}/tools/checkstyle.xml"
SRC_DIR="${ATLANTIS_DIR}/src"

# --- Flags ---------------------------------------------------------------------
# --apply: after reporting, fix exactly the reported return-blank-line
# violations by inserting the missing blank lines, then re-check. Checkstyle
# itself cannot modify files (report-only by design), so the fixing is ours:
# blank-line insertion is semantics-preserving, and each site is re-validated
# (real 'return' keyword outside strings/comments) before touching the file.
APPLY=0
ARGS=()
for t in "$@"; do
  case "$t" in
    --apply) APPLY=1 ;;
    *) ARGS+=("$t") ;;
  esac
done
if [ "${#ARGS[@]}" -gt 0 ]; then
  set -- "${ARGS[@]}"
else
  set --
fi

say()  { echo "[checkstyle] $*"; }
fail() { echo "[checkstyle] FATAL: $*" >&2; exit 2; }

[ -f "$CONFIG" ] || fail "config not found: $CONFIG"
[ -d "$SRC_DIR" ] || fail "src/ not found: $SRC_DIR"

# --- Resolve targets (src/ only, *.java files only) -----------------------------
# Checkstyle is handed explicit .java files, never a raw directory: src/ also
# holds extensionless data fixtures (cherryvis examples) that must not be
# scanned - and must not be "fixed" either, tests may read their exact bytes.
targets=()
if [ "$#" -eq 0 ]; then
  while IFS= read -r -d '' f; do targets+=("$f"); done \
    < <(find "$SRC_DIR" -name '*.java' -print0)
else
  for t in "$@"; do
    case "$t" in
      /*) abs="$t" ;;
      *)  abs="$(cd "$ATLANTIS_DIR" && cd "$(dirname "$t")" && pwd)/$(basename "$t")" ;;
    esac
    case "$abs" in
      "$SRC_DIR"/*) ;;
      *) fail "refusing path outside src/: $t" ;;
    esac
    [ -e "$abs" ] || fail "no such path: $t"
    if [ -d "$abs" ]; then
      while IFS= read -r -d '' f; do targets+=("$f"); done \
        < <(find "$abs" -name '*.java' -print0)
    elif [[ "$abs" == *.java ]]; then
      targets+=("$abs")
    else
      fail "not a .java file (never touch fixtures): $t"
    fi
  done
fi
[ "${#targets[@]}" -gt 0 ] || fail "no .java files selected"

# --- Fetch Checkstyle once (Maven resolves the runtime deps) -----------------
if [ ! -f "$MAIN_JAR" ]; then
  command -v mvn >/dev/null 2>&1 || fail "maven not found (needed once to fetch Checkstyle ${CHECKSTYLE_VERSION})"
  say "fetching Checkstyle ${CHECKSTYLE_VERSION} to ${CACHE_DIR} (one-time, needs network)"
  fetch_pom_dir="$(mktemp -d)"
  trap 'rm -rf "$fetch_pom_dir"' EXIT
  cat > "$fetch_pom_dir/pom.xml" <<EOF
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>atlantis.tooling</groupId>
  <artifactId>checkstyle-fetch</artifactId>
  <version>1</version>
  <packaging>pom</packaging>
  <dependencies>
    <dependency>
      <groupId>com.puppycrawl.tools</groupId>
      <artifactId>checkstyle</artifactId>
      <version>${CHECKSTYLE_VERSION}</version>
    </dependency>
  </dependencies>
</project>
EOF
  mkdir -p "$LIB_DIR"
  timeout 100 mvn -q -B -f "$fetch_pom_dir/pom.xml" \
    dependency:copy-dependencies -DoutputDirectory="$LIB_DIR" -DincludeScope=runtime \
    || fail "maven fetch failed"
  [ -f "$MAIN_JAR" ] || fail "fetch finished but ${MAIN_JAR} is missing"
  say "fetched $(ls "$LIB_DIR"/*.jar | wc -l) jars"
fi

# --- Run ----------------------------------------------------------------------
say "checking ${targets[*]} ($CONFIG)"
LOG="$(mktemp /tmp/atlantis-checkstyle-XXXXXX.log)"
timeout 110 java -cp "${LIB_DIR}/*" com.puppycrawl.tools.checkstyle.Main \
  -c "$CONFIG" "${targets[@]}" >"$LOG" 2>&1
rc=$?
cat "$LOG"
if [ "$rc" -eq 0 ]; then
  rm -f "$LOG"
  say "OK: no violations"
  exit 0
fi
if ! grep -q "Audit done" "$LOG"; then
  rm -f "$LOG"
  fail "checkstyle run failed (exit $rc) - refusing to apply fixes on a broken run"
fi
if [ "$APPLY" -eq 0 ]; then
  rm -f "$LOG"
  say "violations found (exit $rc)"
  say "note: checkstyle only reports - it never modifies files; re-run with the paths you touched (bare run covers the whole tree, including foreign files); use --apply to auto-insert the missing blank lines after non-final returns"
  exit 1
fi

# --- Apply: fix exactly the reported return-blank-line violations ------------
# Checkstyle cannot fix anything itself, so --apply is our own fixer: for
# every finding of the two return-blank-line rules it inserts the missing
# blank line. Inserting blank lines is semantics-preserving by construction,
# and each site is re-validated (a real 'return' keyword outside strings and
# comments) before the file is touched. Nothing else is ever modified.
say "applying return-blank-line fixes..."
python3 - "$LOG" <<'PYEOF'
import re
import sys

log_path = sys.argv[1]
finding = re.compile(r'^\[ERROR\] (.+?\.java):(\d+):.*Return must be followed by a blank line')
sites = {}
with open(log_path, encoding='utf-8', errors='replace') as f:
    for line in f:
        m = finding.search(line)
        if m:
            sites.setdefault(m.group(1), set()).add(int(m.group(2)))

if not sites:
    print('  [apply] no return-blank-line findings - nothing to do')
    sys.exit(0)


def code_spans(text):
    """Map each line number (1-based) to code ranges, skipping // comments,
    /* */ blocks, "..." strings and '...' char literals (Java 8: no text blocks)."""
    spans = {}
    i, n = 0, len(text)
    line, seg_start = 1, 0
    in_block, in_string, in_char = False, False, False

    def close_seg(end):
        spans.setdefault(line, []).append((seg_start, end))

    while i < n:
        c = text[i]
        nxt = text[i + 1] if i + 1 < n else ''
        if c == '\n':
            if not (in_block or in_string or in_char):
                close_seg(i)
            line += 1
            seg_start = i + 1
            i += 1
            continue
        if in_string:
            if c == '\\':
                i += 2
                continue
            if c == '"':
                in_string = False
                seg_start = i + 1
            i += 1
            continue
        if in_char:
            if c == '\\':
                i += 2
                continue
            if c == "'":
                in_char = False
                seg_start = i + 1
            i += 1
            continue
        if in_block:
            if c == '*' and nxt == '/':
                in_block = False
                i += 2
                seg_start = i
                continue
            i += 1
            continue
        if c == '/' and nxt == '/':
            close_seg(i)
            while i < n and text[i] != '\n':
                i += 1
            seg_start = i
            continue
        if c == '/' and nxt == '*':
            close_seg(i)
            in_block = True
            i += 2
            continue
        if c == '"':
            close_seg(i)
            in_string = True
            i += 1
            continue
        if c == "'":
            close_seg(i)
            in_char = True
            i += 1
            continue
        i += 1
    if not (in_block or in_string or in_char):
        close_seg(n)
    return spans


def has_real_return(text, spans, lineno):
    for start, end in spans.get(lineno, []):
        seg = text[start:end]
        for m in re.finditer(r'\breturn\b', seg):
            rest = seg[m.end():]
            if re.match(r'[^;\n]*;', rest):
                return True
    return False


fixed_files, fixed_sites, skipped = 0, 0, 0
for path in sorted(sites):
    try:
        with open(path, 'rb') as f:
            raw = f.read()
    except OSError as e:
        print('  [apply] SKIP {0}: cannot read ({1})'.format(path, e))
        continue
    try:
        text = raw.decode('utf-8')
    except UnicodeDecodeError:
        print('  [apply] SKIP {0}: not UTF-8'.format(path))
        continue
    crlf = '\r\n' in text
    body = text.replace('\r\n', '\n')
    spans = code_spans(body)
    rows = body.split('\n')
    changed = 0
    for lineno in sorted(sites[path], reverse=True):
        if lineno < 1 or lineno > len(rows):
            print('  [apply] SKIP {0}:{1}: line out of range'.format(path, lineno))
            skipped += 1
            continue
        if not has_real_return(body, spans, lineno):
            print('  [apply] SKIP {0}:{1}: no real return outside strings/comments'.format(path, lineno))
            skipped += 1
            continue
        if lineno < len(rows) and rows[lineno].strip() == '':
            continue  # already blank below (idempotent)
        rows.insert(lineno, '')
        changed += 1
    if changed:
        out = ('\r\n' if crlf else '\n').join(rows)
        with open(path, 'wb') as f:
            f.write(out.encode('utf-8'))
        print('  [apply] {0}: {1} blank line(s) inserted'.format(path, changed))
        fixed_files += 1
        fixed_sites += changed
print('  [apply] done: {0} insertion(s) in {1} file(s), {2} skipped'.format(fixed_sites, fixed_files, skipped))
PYEOF
rm -f "$LOG"

# --- Re-check: the fixer is only done when checkstyle agrees -----------------
say "re-checking ${targets[*]}"
timeout 110 java -cp "${LIB_DIR}/*" com.puppycrawl.tools.checkstyle.Main \
  -c "$CONFIG" "${targets[@]}"
rc=$?
if [ "$rc" -eq 0 ]; then
  say "OK: no violations"
  exit 0
else
  say "violations remain after --apply (exit $rc) - fix the rest by hand"
  exit 1
fi
