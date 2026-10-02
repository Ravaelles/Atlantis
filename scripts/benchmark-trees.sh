#!/usr/bin/env bash
# Deterministic micro-benchmark: cost of one frame of combat decisions.
# See src/tests/benchmark/TreeConstructionBenchmark.java.
# Compares across revisions (e.g. before/after Stage C); never gate CI
# on absolute numbers (Mockito-inflated stub world).
set -euo pipefail

cd "$(dirname "$0")/.."

OUT="out/benchmark"
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"

rm -rf "$OUT"
mkdir -p "$OUT"
javac -nowarn -cp "$CP" -d "$OUT" src/tests/benchmark/TreeConstructionBenchmark.java -sourcepath src
java -cp "$OUT:$CP" tests.benchmark.TreeConstructionBenchmark
