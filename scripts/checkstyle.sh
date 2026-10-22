#!/usr/bin/env bash
# Java style gate for Atlantis (the ESLint equivalent: Checkstyle).
#
# Usage:
#   bash scripts/checkstyle.sh            # check all of src/
#   bash scripts/checkstyle.sh <path...>  # check only these (must be under src/)
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
timeout 110 java -cp "${LIB_DIR}/*" com.puppycrawl.tools.checkstyle.Main \
  -c "$CONFIG" "${targets[@]}"
rc=$?
if [ "$rc" -eq 0 ]; then
  say "OK: no violations"
  exit 0
else
  say "violations found (exit $rc)"
  say "note: checkstyle only reports - it never modifies files; re-run with the paths you touched (bare run covers the whole tree, including foreign files)"
  exit 1
fi
