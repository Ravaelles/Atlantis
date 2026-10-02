#!/usr/bin/env bash
# Build a game-ready Atlantis.jar from current sources.
#
# Why this script exists instead of hand-rolled zip commands: the fat jar
# must satisfy four invariants, each of which silently broke a game run
# in the past (see _AI/REVIEW.md Stage C notes):
#   1. Every source must be compiled (a stale file list once skipped new
#      classes, shipping a franken-jar) -> the source list is regenerated
#      with find on every run, minus the one file that cannot compile
#      to Java 8 (see below).
#   2. Bytecode must be Java 8 (the container runs Corretto 8) -> --release 8.
#      That includes tests: the whole tree is compiled in one javac
#      invocation, so a test using a Java 9+ API (e.g. List.of) breaks the
#      game jar. The only exclusion is ATargetingTest, which needs the Nashorn
#      engine (gone since JDK 15).
#   3. No duplicate entries (zip appends shadow old classes) -> fresh zip.
#   4. Layering: freshly compiled classes win; JBWAPI-Rav's bwapi/bwem win
#      over the stale ones frozen in the old jar (classpath shadowing
#      otherwise crashes at runtime); deleted classes are dropped.
#
# Usage: bash scripts/build-bot-jar.sh <base-jar> <output-jar>
#   base-jar   : last known-good fat jar (provides manifest, resources, libs)
#   output-jar : rebuilt game-ready jar
set -euo pipefail

cd "$(dirname "$0")/.."

BASE_JAR="${1:?usage: build-bot-jar.sh <base-jar> <output-jar>}"
OUT_JAR="${2:?usage: build-bot-jar.sh <base-jar> <output-jar>}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

find src -name "*.java" \
    | grep -v "src/tests/unit/ATargetingTest.java" \
    | sort > "$WORK/sources.txt"

CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
mkdir -p "$WORK/classes"
# Fail loudly instead of shipping a franken-jar.
javac --release 8 -nowarn -cp "$CP" -d "$WORK/classes" @"$WORK/sources.txt"

python3 - "$BASE_JAR" "$OUT_JAR" "$WORK/classes" <<'EOF'
import sys, zipfile, os
base_jar, out_jar, classes = sys.argv[1], sys.argv[2], sys.argv[3]
rav = zipfile.ZipFile('lib/JBWAPI-Rav.jar', 'r')
rav_classes = {n: rav.read(n) for n in rav.namelist()
               if n.endswith('.class') and (n.startswith('bwapi/') or n.startswith('bwem/'))}
new_classes = {}
for root, _, files in os.walk(classes):
    for fn in files:
        if fn.endswith('.class'):
            full = os.path.join(root, fn)
            new_classes[os.path.relpath(full, classes)] = open(full, 'rb').read()
zin = zipfile.ZipFile(base_jar, 'r')
old_names = set(zin.namelist())
zout = zipfile.ZipFile(out_jar, 'w', zipfile.ZIP_DEFLATED)
for info in zin.infolist():
    arc = info.filename
    if 'architecture/helper/InstantiateManager' in arc:
        continue  # deleted in Stage C
    if arc in new_classes:
        zout.writestr(arc, new_classes[arc])
    elif arc in rav_classes:
        zout.writestr(arc, rav_classes[arc])
    else:
        zout.writestr(info, zin.read(arc))
for arc, data in new_classes.items():
    if arc not in old_names:
        zout.writestr(arc, data)
zin.close(); zout.close()
names = zipfile.ZipFile(out_jar).namelist()
assert len(names) == len(set(names)), "duplicate entries!"
print("OK:", out_jar, len(names), "entries, no duplicates")
EOF
