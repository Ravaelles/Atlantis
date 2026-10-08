#!/usr/bin/env bash
# Refuse to play a jar that does not match the working tree.
#
# Why this exists: "the game ran an old jar" cost real sessions. The code was
# changed, the play used a previous build, and the bug being hunted was already
# fixed - so the search went into places the bug no longer lived (owner's report,
# 2026-10-08). A timestamp check only catches the files someone remembered to
# list; this compares a hash of the WHOLE source tree, which the jar carries as
# ATLANTIS_SOURCE_FINGERPRINT.
#
# Usage:
#   bash scripts/check-jar-freshness.sh <jar>          # exit 1 if stale/missing tag
#   bash scripts/check-jar-freshness.sh <jar> --quiet  # only the exit code
#
# Exit codes: 0 fresh, 1 stale or untagged, 2 cannot check (no jar/unzip).
set -uo pipefail

cd "$(dirname "$0")/.."

JAR="${1:?usage: check-jar-freshness.sh <jar> [--quiet]}"
QUIET=0
[ "${2:-}" = "--quiet" ] && QUIET=1

say() { [ "$QUIET" -eq 1 ] || echo "$*"; }

if [ ! -f "$JAR" ]; then
  say "[jar-freshness] no jar at $JAR (the runner builds one)"
  exit 2
fi

TAGGED="$(unzip -p "$JAR" ATLANTIS_SOURCE_FINGERPRINT 2>/dev/null | tr -d '[:space:]')"
if [ -z "$TAGGED" ]; then
  say "[jar-freshness] STALE: $JAR carries no source fingerprint."
  say "[jar-freshness] It was not built by scripts/build-bot-jar.sh (or predates the tag)."
  say "[jar-freshness] Rebuild: bash scripts/build-bot-jar.sh \"$JAR\""
  exit 1
fi

CURRENT="$(
  find src -name "*.java" \
    | grep -v "src/tests/unit/ATargetingTest.java" \
    | sort \
    | while read -r f; do printf '%s ' "$f"; md5sum "$f" | cut -d' ' -f1; done \
    | md5sum | cut -d' ' -f1
)"

if [ "$TAGGED" != "$CURRENT" ]; then
  say "[jar-freshness] STALE: $JAR was built from different sources."
  say "[jar-freshness]   jar tag:        $TAGGED"
  say "[jar-freshness]   working tree:   $CURRENT"
  say "[jar-freshness] A game started now would run code you are not editing - which is"
  say "[jar-freshness] how a session gets spent debugging a bug that is already fixed."
  say "[jar-freshness] Rebuild: bash scripts/build-bot-jar.sh \"$JAR\""
  exit 1
fi

say "[jar-freshness] OK: $JAR matches the working tree ($CURRENT)"
exit 0
