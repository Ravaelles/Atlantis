#!/usr/bin/env bash
# Run the acceptance scope: world, squad and commander behaviour.
#
# `scripts/run-tests.sh` defaults to `--select-package tests.unit`, which is the
# fast inner loop. The acceptance package is ~115 tests that actually build a
# world and step frames - the part of the suite that catches harness and
# integration defects. It used to run only when somebody remembered to pass
# `--select-package tests`, and 44 failures sat in the tree unnoticed for that
# reason. Hence a script, not a flag to know about.
#
# Usage (from anywhere):
#   bash scripts/run-acceptance-tests.sh
#   bash scripts/run-acceptance-tests.sh --select-class tests.acceptance.Queue1Test
#
# Pass --everything to include tests.unit as well.
#
# See DOCS/TESTING.md for the current known-failing baseline.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ "${1:-}" = "--everything" ]; then
  shift
  exec bash scripts/run-tests.sh --select-package tests "$@"
fi

if [ "$#" -eq 0 ]; then
  set -- --select-package tests.acceptance
fi

exec bash scripts/run-tests.sh "$@"